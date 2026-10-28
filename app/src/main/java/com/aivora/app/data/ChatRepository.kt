package com.aivora.app.data

import android.content.Context
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.ChatListCache
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.MessageCache
import com.aivora.app.data.local.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Thin façade over shared API + in-memory store + disk caches.
 * Screens should prefer this over constructing new Retrofit clients.
 */
object ChatRepository {
    fun api(): AivoraApi = NetworkModule.api()

    suspend fun refreshConversations(context: Context): Boolean {
        return runCatching {
            val remote = api().getConversations().items.map { it.toLocalChat() }
            InMemoryChatStore.replaceFromRemote(remote)
            ChatListCache.save(context, remote)
            true
        }.getOrDefault(false)
    }

    /** Fetch thread into memory + disk. Does not mark read (safe for background warm). */
    suspend fun pullMessages(context: Context, chatId: String, myUserId: String): Boolean {
        val epoch = InMemoryChatStore.messageEpoch(chatId)
        return runCatching {
            val items = api().getConversationsIdMessages(chatId, limit = 60).items
                .map { it.toLocalMessage(myUserId) }
            // Merge into existing window — never wipe a painted list (chat-open jank).
            InMemoryChatStore.applyRemotePage(chatId, items, expectedEpoch = epoch)
            InMemoryChatStore.persistMessages(context, chatId)
            true
        }.getOrDefault(false)
    }

    /** Pull only — mark-read is owned by the open chat UI (avoids double POST). */
    suspend fun refreshMessages(context: Context, chatId: String, myUserId: String): Boolean =
        pullMessages(context, chatId, myUserId)

    fun hydrateMessagesFromDisk(context: Context, chatId: String) {
        // Skip when this thread is already warm (reopen path).
        if (InMemoryChatStore.messages(chatId).value.isNotEmpty()) return
        InMemoryChatStore.hydrateMessages(chatId, MessageCache.load(context, chatId))
    }

    /** Never call from composition — JSON parse stays off Main. */
    suspend fun hydrateMessagesFromDiskAsync(context: Context, chatId: String) =
        withContext(Dispatchers.IO) {
            hydrateMessagesFromDisk(context, chatId)
        }

    /**
     * Load a page older than the oldest in-memory message. Returns how many were prepended.
     */
    suspend fun loadOlderMessages(chatId: String, myUserId: String): Int {
        val oldest = InMemoryChatStore.messages(chatId).value.firstOrNull() ?: return 0
        val before = oldest.createdAtIso?.takeIf { it.isNotBlank() } ?: return 0
        return runCatching {
            val items = api().getConversationsIdMessages(chatId, limit = 40, before = before).items
                .map { it.toLocalMessage(myUserId) }
            InMemoryChatStore.prependOlderMessages(chatId, items)
        }.getOrDefault(0)
    }

    fun myUserId(context: Context): String? = SessionStore.userId(context)

    /** Paint any on-disk threads into memory (cheap) so open is instant when revisiting. */
    fun hydrateRecentFromDisk(context: Context, limit: Int = 20) {
        InMemoryChatStore.chats.value.take(limit).forEach { chat ->
            if (InMemoryChatStore.messages(chat.id).value.isEmpty()) {
                hydrateMessagesFromDisk(context, chat.id)
            }
        }
    }

    /** PERF C2: same as [hydrateRecentFromDisk] but never on Main. */
    suspend fun hydrateRecentFromDiskAsync(context: Context, limit: Int = 20) =
        withContext(Dispatchers.IO) {
            hydrateRecentFromDisk(context, limit)
        }

    /**
     * Background-fill disk+memory for top chats that have never been cached —
     * so the first open of a thread is not an empty/black wait on Render latency.
     */
    suspend fun warmRecentThreads(context: Context, limit: Int = 12) = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        val uid = SessionStore.userId(app) ?: return@withContext
        val targets = InMemoryChatStore.chats.value
            .asSequence()
            .filter { !MessageCache.has(app, it.id) }
            .take(limit)
            .toList()
        if (targets.isEmpty()) return@withContext
        val gate = Semaphore(2)
        coroutineScope {
            targets.map { chat ->
                async {
                    gate.withPermit {
                        if (MessageCache.has(app, chat.id)) return@withPermit
                        if (InMemoryChatStore.messages(chat.id).value.isNotEmpty()) {
                            InMemoryChatStore.persistMessages(app, chat.id)
                            return@withPermit
                        }
                        pullMessages(app, chat.id, uid)
                    }
                }
            }.awaitAll()
        }
    }
}
