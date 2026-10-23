package com.aivora.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Process-lifetime chat/message source of truth. Nothing is written to disk.
 */
object InMemoryChatStore {
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

    fun upsertChat(chat: LocalChat) {
        _chats.update { listOf(chat) + it.filterNot { c -> c.id == chat.id } }
        messageFlows.getOrPut(chat.id) { MutableStateFlow(emptyList()) }
    }

    fun messages(chatId: String): StateFlow<List<LocalMessage>> =
        messageFlow(chatId).asStateFlow()

    fun chat(chatId: String): LocalChat =
        _chats.value.find { it.id == chatId }
            ?: if (usingRemote) LocalChat(chatId, "Chat", "Say hello", "Now")
            else SeedData.chat(chatId)

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

    fun clearChat(chatId: String) {
        bumpEpoch(chatId)
        messageFlow(chatId).value = emptyList()
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(preview = "No messages", time = "Now", unread = 0)
                else it
            }
        }
    }

    /** Remove the conversation from the home list (after clear / leave). */
    fun removeChat(chatId: String) {
        bumpEpoch(chatId)
        messageFlows.remove(chatId)
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
     * Replace the message list for a chat (e.g. after a remote fetch/poll).
     * Pass [expectedEpoch] from [messageEpoch] before the network call so a
     * clear/delete that happened meanwhile is not overwritten.
     */
    fun replaceMessages(chatId: String, messages: List<LocalMessage>, expectedEpoch: Int? = null) {
        if (expectedEpoch != null && expectedEpoch != messageEpoch(chatId)) return
        messageFlow(chatId).value = messages
        val last = messages.lastOrNull()
        if (last != null) {
            _chats.update { list ->
                val current = list.find { it.id == chatId }
                    ?: LocalChat(chatId, "Chat", last.body, last.time)
                listOf(
                    current.copy(preview = last.body, time = last.time, unread = 0),
                ) + list.filterNot { it.id == chatId }
            }
        } else {
            _chats.update { list ->
                list.map {
                    if (it.id == chatId) it.copy(preview = "No messages", unread = 0) else it
                }
            }
        }
    }

    /** Insert or replace one message by id (websocket push). */
    fun upsertMessage(chatId: String, message: LocalMessage) {
        val alreadyHad = messageFlow(chatId).value.any { it.id == message.id }
        messageFlow(chatId).update { list ->
            val existing = list.find { it.id == message.id }
            val merged = if (existing != null && message.mine) {
                message.copy(status = maxStatus(existing.status, message.status))
            } else {
                message
            }
            list.filterNot { it.id == message.id } + merged
        }
        _chats.update { list ->
            val current = list.find { it.id == chatId }
                ?: LocalChat(chatId, "Chat", message.body, message.time)
            val unread = when {
                message.mine || viewingChatId == chatId -> 0
                alreadyHad -> current.unread
                else -> current.unread + 1
            }
            listOf(
                current.copy(
                    preview = message.body,
                    time = message.time,
                    unread = unread,
                ),
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
