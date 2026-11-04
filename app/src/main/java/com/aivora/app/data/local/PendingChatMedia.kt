package com.aivora.app.data.local

/**
 * One-shot handoff when system camera returns for a chat — ChatScreen
 * consumes into the compose preview (caption + view once) before send.
 */
object PendingChatMedia {
    data class Item(
        val chatId: String,
        val uri: String,
        val preferViewOnce: Boolean = false,
    )

    @Volatile
    private var pending: Item? = null

    fun set(chatId: String, uri: String, preferViewOnce: Boolean = false) {
        pending = Item(chatId = chatId, uri = uri, preferViewOnce = preferViewOnce)
    }

    fun consume(chatId: String): Item? {
        val item = pending ?: return null
        if (item.chatId != chatId) return null
        pending = null
        return item
    }
}
