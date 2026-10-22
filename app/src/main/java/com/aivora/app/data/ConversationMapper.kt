package com.aivora.app.data

import com.aivora.app.data.local.LocalChat
import com.aivora.app.data.model.ConversationDto

fun ConversationDto.toLocalChat(): LocalChat {
    val title = when {
        !this.title.isNullOrBlank() -> this.title!!
        peer?.displayName?.isNotBlank() == true -> peer!!.displayName!!
        peer?.phoneDisplay?.isNotBlank() == true -> peer!!.phoneDisplay!!
        peer?.phone?.isNotBlank() == true -> peer!!.phone!!
        else -> "Chat"
    }
    val preview = lastMessage?.text?.takeIf { it.isNotBlank() } ?: "Say hello"
    return LocalChat(
        id = id.orEmpty(),
        title = title,
        preview = preview,
        time = "",
        unread = unreadCount,
        isGroup = type.equals("group", ignoreCase = true),
        archived = archived,
    )
}
