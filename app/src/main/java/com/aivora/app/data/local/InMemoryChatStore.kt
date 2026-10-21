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

    private val messageFlows = ConcurrentHashMap<String, MutableStateFlow<List<LocalMessage>>>()

    fun messages(chatId: String): StateFlow<List<LocalMessage>> =
        messageFlow(chatId).asStateFlow()

    fun chat(chatId: String): LocalChat =
        _chats.value.find { it.id == chatId } ?: SeedData.chat(chatId)

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
            MutableStateFlow(SeedData.messagesFor(chatId))
        }

    private fun newId(): String = "local-${UUID.randomUUID().toString().take(8)}"
}
