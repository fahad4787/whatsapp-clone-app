package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Process-lifetime chat/message source of truth.
 * Disk snapshots via [ChatListCache] / [MessageCache] for instant reopen.
 */
object InMemoryChatStore {
    /**
     * Scroll window — large enough for history; LazyColumn + slim bubbles keep fling ok (F12).
     * Disk [MessageCache.MAX_MESSAGES] can stay larger for reopen paint.
     */
    private const val MAX_IN_MEMORY_MESSAGES = 60

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val persistJobs = ConcurrentHashMap<String, Job>()

    /** Empty until remote load or [useOfflineSeed] — avoids flashing SeedData after login. */
    private val _chats = MutableStateFlow<List<LocalChat>>(emptyList())
    val chats: StateFlow<List<LocalChat>> = _chats.asStateFlow()
    @Volatile var usingRemote: Boolean = false
        private set

    /** Chat currently open on screen — incoming messages there do not bump unread. */
    @Volatile private var viewingChatId: String? = null

    private val messageFlows = ConcurrentHashMap<String, MutableStateFlow<List<LocalMessage>>>()
    /** Bumped on clear/delete so in-flight message polls cannot restore wiped history. */
    private val messageEpoch = ConcurrentHashMap<String, AtomicInteger>()

    fun setViewingChat(chatId: String?) {
        viewingChatId = chatId
        if (chatId != null) markRead(chatId)
    }

    fun isViewing(chatId: String): Boolean = viewingChatId == chatId

    fun messageEpoch(chatId: String): Int =
        messageEpoch.getOrPut(chatId) { AtomicInteger(0) }.get()

    private fun bumpEpoch(chatId: String): Int =
        messageEpoch.getOrPut(chatId) { AtomicInteger(0) }.incrementAndGet()

    /** Demo list only when logged out / no API session. */
    fun useOfflineSeed() {
        if (usingRemote) return
        if (_chats.value.isEmpty()) {
            _chats.value = SeedData.chats
        }
    }

    fun replaceFromRemote(remote: List<LocalChat>) {
        usingRemote = true
        // Drop any leftover seed chats/messages.
        messageFlows.keys.retainAll(remote.map { it.id }.toSet())
        _chats.value = remote
        remote.forEach { messageFlows.getOrPut(it.id) { MutableStateFlow(emptyList()) } }
    }

    /** Instant paint from disk before network returns (session cold start). */
    fun hydrateFromCache(cached: List<LocalChat>) {
        if (cached.isEmpty() || _chats.value.isNotEmpty()) return
        usingRemote = true
        _chats.value = cached
        cached.forEach { messageFlows.getOrPut(it.id) { MutableStateFlow(emptyList()) } }
    }

    /** Paint cached messages when opening a chat that has an empty in-memory list. */
    fun hydrateMessages(chatId: String, cached: List<LocalMessage>) {
        if (cached.isEmpty()) return
        val flow = messageFlow(chatId)
        if (flow.value.isNotEmpty()) return
        // distinctBy keep last — duplicate ids crash LazyColumn keys.
        flow.value = cached.distinctBy { it.id }
    }

    fun persistMessages(context: Context, chatId: String) {
        val app = context.applicationContext
        persistJobs[chatId]?.cancel()
        persistJobs[chatId] = ioScope.launch {
            delay(350) // coalesce rapid WS upserts
            val msgs = messageFlows[chatId]?.value ?: return@launch
            MessageCache.save(app, chatId, msgs)
            ChatListCache.save(app, _chats.value)
        }
    }

    /**
     * @param promote when true (new activity), move row to top. Open-chat refresh must pass
     * false — otherwise the 2nd chat jumps to #1 under the transition for a second.
     */
    fun upsertChat(chat: LocalChat, promote: Boolean = true) {
        _chats.update { list ->
            if (promote) {
                listOf(chat) + list.filterNot { c -> c.id == chat.id }
            } else {
                var found = false
                val mapped = list.map { c ->
                    if (c.id == chat.id) {
                        found = true
                        chat
                    } else {
                        c
                    }
                }
                if (found) mapped else list + chat
            }
        }
        messageFlows.getOrPut(chat.id) { MutableStateFlow(emptyList()) }
    }

    /** Stable per-chat flow — do not wrap in a new asStateFlow() each call (breaks collect). */
    fun messages(chatId: String): StateFlow<List<LocalMessage>> = messageFlow(chatId)

    fun chat(chatId: String): LocalChat =
        _chats.value.find { it.id == chatId }
            ?: if (usingRemote) LocalChat(chatId, "Chat", "Say hello", "Now")
            else SeedData.chat(chatId)

    /** Emits only when this chat's row changes — avoids full-list recomposition of open chat. */
    fun chatFlow(chatId: String): Flow<LocalChat> =
        _chats
            .map { list ->
                list.find { it.id == chatId }
                    ?: if (usingRemote) LocalChat(chatId, "Chat", "Say hello", "Now")
                    else SeedData.chat(chatId)
            }
            .distinctUntilChanged()

    fun sendText(
        chatId: String,
        body: String,
        mine: Boolean = true,
        replyTo: LocalReplyPreview? = null,
    ) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = trimmed,
                time = "Now",
                replyTo = replyTo,
            ),
            preview = trimmed,
        )
    }

    fun sendMedia(
        chatId: String,
        uri: String,
        caption: String = "Photo",
        mine: Boolean = true,
        kind: LocalMediaKind = LocalMediaKind.Image,
        fileName: String? = null,
        durationSec: Int? = null,
    ) {
        val body = caption.ifBlank {
            when (kind) {
                LocalMediaKind.Image -> "Photo"
                LocalMediaKind.Video -> "Video"
                LocalMediaKind.File -> fileName ?: "Document"
                LocalMediaKind.Audio -> "Voice message"
            }
        }
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = body,
                time = "Now",
                mediaUri = uri,
                mediaKind = kind,
                fileName = fileName,
                voiceDurationSec = if (kind == LocalMediaKind.Audio) durationSec?.coerceAtLeast(1) else null,
            ),
            preview = body,
        )
    }

    fun sendVoice(chatId: String, durationSec: Int = 8, mine: Boolean = true, uri: String? = null) {
        val secs = durationSec.coerceAtLeast(1)
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = "Voice message",
                time = "Now",
                mediaUri = uri,
                mediaKind = LocalMediaKind.Audio,
                voiceDurationSec = secs,
            ),
            preview = "Voice message",
        )
    }

    /** Ensure a 1:1 chat exists for [title]; returns chat id. */
    fun openOrCreateDirect(title: String): String {
        val existing = _chats.value.find { it.title.equals(title, ignoreCase = true) }
        if (existing != null) return existing.id

        val id = "c-${kotlin.math.abs(title.hashCode())}"
        val chat = LocalChat(
            id = id,
            title = title,
            preview = "Say hello",
            time = "Now",
        )
        _chats.update { listOf(chat) + it.filterNot { c -> c.id == id } }
        messageFlow(id)
        return id
    }

    fun markRead(chatId: String) {
        _chats.update { list ->
            list.map { if (it.id == chatId) it.copy(unread = 0) else it }
        }
    }

    fun clearChat(chatId: String, context: Context? = null) {
        bumpEpoch(chatId)
        messageFlow(chatId).value = emptyList()
        context?.let { MessageCache.clearChat(it, chatId) }
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(preview = "No messages", time = "Now", unread = 0)
                else it
            }
        }
    }

    /** Remove the conversation from the home list (after clear / leave). */
    fun removeChat(chatId: String, context: Context? = null) {
        bumpEpoch(chatId)
        messageFlows.remove(chatId)
        context?.let { MessageCache.clearChat(it, chatId) }
        _chats.update { list -> list.filterNot { it.id == chatId } }
    }

    fun removeMessage(chatId: String, messageId: String) {
        bumpEpoch(chatId)
        messageFlow(chatId).update { list -> list.filterNot { it.id == messageId } }
        val last = messageFlow(chatId).value.lastOrNull()
        _chats.update { list ->
            list.map {
                if (it.id != chatId) it
                else it.copy(
                    preview = last?.body ?: "No messages",
                    time = last?.time ?: it.time,
                )
            }
        }
    }

    fun applyReceipt(chatId: String, messageIds: List<String>?, status: MessageDeliveryStatus) {
        val ids = messageIds?.filter { it.isNotBlank() }.orEmpty()
        messageFlow(chatId).update { list ->
            list.map { msg ->
                if (!msg.mine) return@map msg
                val matches = if (ids.isEmpty()) true else msg.id in ids
                if (!matches) return@map msg
                val next = maxStatus(msg.status, status)
                if (next == msg.status) msg else msg.copy(status = next)
            }
        }
    }

    private fun maxStatus(a: MessageDeliveryStatus, b: MessageDeliveryStatus): MessageDeliveryStatus {
        val order = listOf(
            MessageDeliveryStatus.Sending,
            MessageDeliveryStatus.Sent,
            MessageDeliveryStatus.Delivered,
            MessageDeliveryStatus.Read,
        )
        return if (order.indexOf(b) >= order.indexOf(a)) b else a
    }

    /**
     * Prepend older history (scroll-up). Keeps newest messages; soft-cap at 2× window.
     * @return number of newly inserted messages
     */
    fun prependOlderMessages(chatId: String, older: List<LocalMessage>): Int {
        if (older.isEmpty()) return 0
        val flow = messageFlow(chatId)
        var added = 0
        flow.update { current ->
            val have = current.map { it.id }.toHashSet()
            val unique = older.filter { it.id !in have }
            added = unique.size
            if (unique.isEmpty()) return@update current
            val merged = unique + current
            // Soft cap while paging older — never 2× window (that killed low-end fling).
            // Keep headroom so scroll-up history is not immediately trimmed away.
            val softMax = MAX_IN_MEMORY_MESSAGES + 40
            if (merged.size > softMax) merged.takeLast(softMax) else merged
        }
        return added
    }

    /**
     * Replace the message list for a chat (e.g. after a remote fetch/poll).
     * Pass [expectedEpoch] from [messageEpoch] before the network call so a
     * clear/delete that happened meanwhile is not overwritten.
     */
    fun replaceMessages(chatId: String, messages: List<LocalMessage>, expectedEpoch: Int? = null) {
        if (expectedEpoch != null && expectedEpoch != messageEpoch(chatId)) return
        val flow = messageFlow(chatId)
        // Skip identical snapshots — avoids janky full recomposition while scrolling.
        val deduped = messages.distinctBy { it.id }
        val capped = if (deduped.size > MAX_IN_MEMORY_MESSAGES) {
            deduped.takeLast(MAX_IN_MEMORY_MESSAGES)
        } else {
            deduped
        }
        if (sameMessageSnapshot(flow.value, capped)) return
        flow.value = capped
        publishPreview(chatId, capped.lastOrNull())
    }

    /**
     * Apply a latest-page remote fetch without wiping older in-memory rows
     * (scroll-up history) or reshuffling a already-painted LazyColumn.
     * Remote wins on id conflicts; local-only older messages are kept.
     */
    fun applyRemotePage(chatId: String, remote: List<LocalMessage>, expectedEpoch: Int? = null) {
        if (expectedEpoch != null && expectedEpoch != messageEpoch(chatId)) return
        val flow = messageFlow(chatId)
        val local = flow.value
        val dedupedRemote = remote.distinctBy { it.id }
        if (local.isEmpty()) {
            replaceMessages(chatId, dedupedRemote, expectedEpoch = null)
            return
        }
        if (dedupedRemote.isEmpty()) return

        val remoteById = LinkedHashMap<String, LocalMessage>(dedupedRemote.size)
        dedupedRemote.forEach { remoteById[it.id] = it }

        // Preserve local-only rows (older pages / optimistic sends not yet on server page).
        val localOnly = local.filter { it.id !in remoteById }
        val merged = (localOnly + dedupedRemote).distinctBy { it.id }
        val capped = if (merged.size > MAX_IN_MEMORY_MESSAGES) {
            merged.takeLast(MAX_IN_MEMORY_MESSAGES)
        } else {
            merged
        }
        if (sameMessageSnapshot(local, capped)) return
        flow.value = capped
        publishPreview(chatId, capped.lastOrNull())
    }

    private fun publishPreview(chatId: String, last: LocalMessage?) {
        // In-place preview update only — do not promote to top (that flash on open).
        _chats.update { list ->
            list.map { c ->
                if (c.id != chatId) c
                else if (last != null) {
                    c.copy(preview = last.body, time = last.time, unread = 0)
                } else {
                    c.copy(preview = "No messages", unread = 0)
                }
            }
        }
    }

    private fun sameMessageSnapshot(a: List<LocalMessage>, b: List<LocalMessage>): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) {
            val x = a[i]
            val y = b[i]
            if (x.id != y.id || x.status != y.status || x.body != y.body ||
                x.starred != y.starred || x.deletedForEveryone != y.deletedForEveryone ||
                x.reactions != y.reactions || x.editedAtIso != y.editedAtIso ||
                x.mediaUri != y.mediaUri || x.system != y.system || x.disappeared != y.disappeared ||
                x.poll != y.poll || x.contact != y.contact || x.location != y.location ||
                x.event != y.event || x.viewOnce != y.viewOnce
            ) {
                return false
            }
        }
        return true
    }

    /** Insert or replace one message by id (websocket push / react / star). */
    fun upsertMessage(chatId: String, message: LocalMessage) {
        val flow = messageFlow(chatId)
        val before = flow.value
        val existingIdx = before.indexOfFirst { it.id == message.id }
        val alreadyHad = existingIdx >= 0
        flow.update { list ->
            val existing = if (existingIdx >= 0) list[existingIdx] else null
            val merged = when {
                existing == null -> message
                message.mine -> message.copy(status = maxStatus(existing.status, message.status))
                else -> message
            }
            if (existingIdx >= 0) {
                // Keep order — reacting must not move the bubble to the bottom.
                list.toMutableList().also { it[existingIdx] = merged }
            } else {
                val next = list + merged
                if (next.size > MAX_IN_MEMORY_MESSAGES) next.takeLast(MAX_IN_MEMORY_MESSAGES) else next
            }
        }
        val after = flow.value
        val isLatest = after.lastOrNull()?.id == message.id
        _chats.update { list ->
            val current = list.find { it.id == chatId }
                ?: LocalChat(chatId, "Chat", message.body, message.time)
            val unread = when {
                message.mine || viewingChatId == chatId -> 0
                alreadyHad -> current.unread
                else -> current.unread + 1
            }
            // Only bump home preview for new / latest messages (not mid-thread reacts).
            val preview = if (!alreadyHad || isLatest) message.body else current.preview
            val time = if (!alreadyHad || isLatest) message.time else current.time
            listOf(
                current.copy(preview = preview, time = time, unread = unread),
            ) + list.filterNot { it.id == chatId }
        }
    }

    private fun append(chatId: String, message: LocalMessage, preview: String) {
        messageFlow(chatId).update { it + message }
        _chats.update { list ->
            val current = list.find { it.id == chatId } ?: SeedData.chat(chatId)
            val updated = current.copy(
                preview = if (message.mine) preview else preview,
                time = "Now",
                unread = if (message.mine) 0 else current.unread + 1,
            )
            listOf(updated) + list.filterNot { it.id == chatId }
        }
    }

    private fun messageFlow(chatId: String): MutableStateFlow<List<LocalMessage>> =
        messageFlows.getOrPut(chatId) {
            MutableStateFlow(if (usingRemote) emptyList() else SeedData.messagesFor(chatId))
        }

    private fun newId(): String = "local-${UUID.randomUUID().toString().take(8)}"
}
