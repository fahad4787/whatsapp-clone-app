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
        time = chatListTime(lastMessageAt ?: lastMessage?.createdAt ?: updatedAt),
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

/** Chat-list timestamp: HH:mm today, "Yesterday", otherwise dd/MM/yy (device timezone). */
internal fun chatListTime(iso: String?): String = runCatching {
    if (iso.isNullOrBlank()) return@runCatching ""
    val z = java.time.Instant.parse(iso).atZone(java.time.ZoneId.systemDefault())
    val today = java.time.LocalDate.now(z.zone)
    when (z.toLocalDate()) {
        today -> java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(z)
        today.minusDays(1) -> "Yesterday"
        else -> java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy").format(z)
    }
}.getOrDefault("")
