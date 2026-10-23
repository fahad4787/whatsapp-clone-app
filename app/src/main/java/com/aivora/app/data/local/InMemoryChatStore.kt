package com.aivora.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Process-lifetime chat/message source of truth. Nothing is written to disk.
 */
object InMemoryChatStore {
    private val _chats = MutableStateFlow(SeedData.chats)
    val chats: StateFlow<List<LocalChat>> = _chats.asStateFlow()
    @Volatile var usingRemote: Boolean = false
        private set

    /** Chat currently open on screen — incoming messages there do not bump unread. */
    @Volatile private var viewingChatId: String? = null

    private val messageFlows = ConcurrentHashMap<String, MutableStateFlow<List<LocalMessage>>>()

    fun setViewingChat(chatId: String?) {
        viewingChatId = chatId
        if (chatId != null) markRead(chatId)
    }

    fun replaceFromRemote(remote: List<LocalChat>) {
        usingRemote = true
        // Server unread_count is authoritative (local WS bumps until the next refresh).
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

    fun sendText(chatId: String, body: String, mine: Boolean = true) {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = trimmed,
                time = "Now",
            ),
            preview = trimmed,
        )
    }

    fun sendMedia(chatId: String, uri: String, caption: String = "Photo", mine: Boolean = true) {
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = caption.ifBlank { "Photo" },
                time = "Now",
                mediaUri = uri,
            ),
            preview = "Photo",
        )
    }

    fun sendVoice(chatId: String, durationSec: Int = 8, mine: Boolean = true) {
        val secs = durationSec.coerceAtLeast(1)
        append(
            chatId = chatId,
            message = LocalMessage(
                id = newId(),
                mine = mine,
                body = "Voice message",
                time = "Now",
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
        messageFlow(chatId).value = emptyList()
        _chats.update { list ->
            list.map {
                if (it.id == chatId) it.copy(preview = "No messages", time = "Now", unread = 0)
                else it
            }
        }
    }

    /** Replace the message list for a chat (e.g. after a remote fetch/poll). */
    fun replaceMessages(chatId: String, messages: List<LocalMessage>) {
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
        }
    }

    /** Insert or replace one message by id (websocket push). */
    fun upsertMessage(chatId: String, message: LocalMessage) {
        messageFlow(chatId).update { list ->
            val without = list.filterNot { it.id == message.id }
            without + message
        }
        _chats.update { list ->
            val current = list.find { it.id == chatId }
                ?: LocalChat(chatId, "Chat", message.body, message.time)
            listOf(
                current.copy(
                    preview = message.body,
                    time = message.time,
                    unread = when {
                        message.mine || viewingChatId == chatId -> 0
                        else -> current.unread + 1
                    },
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
