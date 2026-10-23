package com.aivora.app.data

import com.aivora.app.data.local.LocalLinkPreview
import com.aivora.app.data.local.LocalMediaKind
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.LocalReaction
import com.aivora.app.data.local.LocalReplyPreview
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.data.model.MessageDto

fun MessageDto.toLocalMessage(myUserId: String): LocalMessage {
    val created = createdAt.orEmpty()
    val time = when {
        created.length >= 16 -> created.substring(11, 16)
        created.isNotBlank() -> created
        else -> "Now"
    }
    val deleted = !deletedForEveryoneAt.isNullOrBlank()
    val media = if (deleted) null else attachment?.url?.takeIf { it.isNotBlank() }
    val kind = when (type?.lowercase()) {
        "image" -> LocalMediaKind.Image
        "audio" -> LocalMediaKind.Audio
        "video" -> LocalMediaKind.Video
        "file" -> LocalMediaKind.File
        else -> when {
            media == null -> null
            attachment?.mime?.startsWith("image/") == true -> LocalMediaKind.Image
            attachment?.mime?.startsWith("audio/") == true -> LocalMediaKind.Audio
            attachment?.mime?.startsWith("video/") == true -> LocalMediaKind.Video
            media != null -> LocalMediaKind.File
            else -> null
        }
    }
    val voiceSecs = when {
        deleted -> null
        kind == LocalMediaKind.Audio -> attachment?.duration?.takeIf { it > 0 } ?: 1
        else -> null
    }
    val preview = if (deleted) {
        null
    } else {
        this.preview?.url?.takeIf { it.isNotBlank() }?.let { url ->
            LocalLinkPreview(
                url = url,
                title = this.preview?.title,
                description = this.preview?.description,
                site = this.preview?.site,
                imageUrl = this.preview?.imageUrl,
            )
        }
    }
    val reply = replyTo?.let { r ->
        LocalReplyPreview(
            id = r.id.orEmpty(),
            body = when {
                r.deleted -> "Original message was deleted"
                !r.text.isNullOrBlank() -> r.text!!
                else -> "Message"
            },
            deleted = r.deleted,
        )
    }
    val fallbackBody = when (kind) {
        LocalMediaKind.Image -> "Photo"
        LocalMediaKind.Audio -> "Voice message"
        LocalMediaKind.Video -> "Video"
        LocalMediaKind.File -> attachment?.name?.takeIf { it.isNotBlank() } ?: "Document"
        null -> ""
    }
    return LocalMessage(
        id = id.orEmpty(),
        mine = senderId == myUserId,
        body = when {
            deleted -> "This message was deleted"
            !text.isNullOrBlank() -> text!!
            else -> fallbackBody
        },
        time = time,
        mediaUri = media,
        mediaKind = kind,
        fileName = attachment?.name,
        linkPreview = preview,
        voiceDurationSec = voiceSecs,
        status = when (status?.lowercase()) {
            "read" -> MessageDeliveryStatus.Read
            "delivered" -> MessageDeliveryStatus.Delivered
            "sent" -> MessageDeliveryStatus.Sent
            else -> MessageDeliveryStatus.Sent
        },
        deletedForEveryone = deleted,
        replyTo = reply,
        reactions = reactions.orEmpty().mapNotNull { r ->
            val emoji = r.emoji?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            LocalReaction(
                emoji = emoji,
                count = r.count,
                mine = r.userIds.contains(myUserId),
            )
        },
        starred = starred,
        forwarded = forwarded,
        conversationId = conversationId,
        createdAtIso = createdAt,
    )
}
