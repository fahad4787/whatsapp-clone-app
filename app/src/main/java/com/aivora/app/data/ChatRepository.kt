package com.aivora.app.data

import android.content.Context
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.ChatListCache
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.MessageCache
import com.aivora.app.data.local.SessionStore

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

    suspend fun refreshMessages(context: Context, chatId: String, myUserId: String): Boolean {
        val epoch = InMemoryChatStore.messageEpoch(chatId)
        return runCatching {
            val items = api().getConversationsIdMessages(chatId, limit = 60).items
                .map { it.toLocalMessage(myUserId) }
            InMemoryChatStore.replaceMessages(chatId, items, expectedEpoch = epoch)
            InMemoryChatStore.persistMessages(context, chatId)
            runCatching { api().postConversationsIdRead(chatId) }
            true
        }.getOrDefault(false)
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

    fun hydrateMessagesFromDisk(context: Context, chatId: String) {
        InMemoryChatStore.hydrateMessages(chatId, MessageCache.load(context, chatId))
    }
}
