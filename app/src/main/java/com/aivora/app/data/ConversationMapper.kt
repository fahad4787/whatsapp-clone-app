package com.aivora.app.data

import com.aivora.app.data.local.LocalChat
import com.aivora.app.data.model.ConversationDto

fun ConversationDto.toLocalChat(): LocalChat {
    members?.forEach { m ->
        SenderNames.put(m.user?.id, m.user?.displayName ?: m.user?.phoneDisplay)
    }
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
        favorite = favorite,
        archived = archived,
        pinned = pinned,
        locked = locked,
        mutedUntil = mutedUntil,
        blockedByMe = blockedByMe,
        blockedMe = blockedMe,
        peerUserId = peer?.id,
        peerPhone = peer?.phoneDisplay?.takeIf { it.isNotBlank() }
            ?: peer?.phone?.takeIf { it.isNotBlank() },
        myRole = myRole,
        description = description,
        avatarUrl = avatarUrl ?: avatarThumb,
        onlyAdminsCanSend = onlyAdminsCanSend,
        onlyAdminsCanEditInfo = onlyAdminsCanEditInfo,
        disappearingSeconds = disappearingSeconds,
        wallpaper = wallpaper,
        peerLastSeenAt = peer?.lastSeenAt,
        peerPresenceVisible = peer?.presenceVisible ?: true,
    )
}
