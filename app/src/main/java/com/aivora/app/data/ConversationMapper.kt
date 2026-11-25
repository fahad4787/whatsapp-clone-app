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
    val myId = runCatching {
        com.aivora.app.data.local.SessionStore.userId(com.aivora.app.AivoraApp.instance)
    }.getOrNull()
    val lastMine = lastMessage != null && myId != null && lastMessage?.senderId == myId
    // Through the message mapper, so a photo or voice note is named instead of "Say hello".
    val last = myId?.let { lastMessage?.toLocalMessage(it) }
    // Call and timer notices are not messages I sent, so they carry no delivery tick.
    val lastStatus = if (lastMine && last?.system != true) last?.status else null
    val preview = (last?.body ?: lastMessage?.text)?.takeIf { it.isNotBlank() } ?: "Say hello"
    val group = type.equals("group", ignoreCase = true)
    val previewSender = if (group && last != null && !last.mine && !last.system) last.senderName else null
    return LocalChat(
        id = id.orEmpty(),
        title = title,
        preview = preview,
        time = chatListTime(lastMessageAt ?: lastMessage?.createdAt ?: updatedAt),
        unread = unreadCount,
        isGroup = group,
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
        peerAbout = peer?.about?.takeIf { it.isNotBlank() },
        myRole = myRole,
        description = description,
        avatarUrl = (avatarUrl ?: avatarThumb)?.takeIf { it.isNotBlank() }
            ?: (peer?.avatarUrl ?: peer?.avatarThumb)?.takeIf { it.isNotBlank() },
        onlyAdminsCanSend = onlyAdminsCanSend,
        onlyAdminsCanEditInfo = onlyAdminsCanEditInfo,
        disappearingSeconds = disappearingSeconds,
        wallpaper = wallpaper,
        peerLastSeenAt = peer?.lastSeenAt,
        peerPresenceVisible = peer?.presenceVisible ?: true,
        lastMine = lastMine,
        lastStatus = lastStatus,
        previewSender = previewSender,
    )
}

/** Chat-list timestamp: HH:mm today, "Yesterday", otherwise dd/MM/yy (device timezone). */
internal fun chatListTime(iso: String?): String = runCatching {
    if (iso.isNullOrBlank()) return@runCatching ""
    val z = java.time.Instant.parse(iso).atZone(java.time.ZoneId.systemDefault())
    val today = java.time.LocalDate.now(z.zone)
    when (z.toLocalDate()) {
        today -> com.aivora.app.data.ClockFormat.format(z)
        today.minusDays(1) -> "Yesterday"
        else -> java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy").format(z)
    }
}.getOrDefault("")
