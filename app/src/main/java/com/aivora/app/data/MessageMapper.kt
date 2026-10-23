package com.aivora.app.data

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
    return LocalMessage(
        id = id.orEmpty(),
        mine = senderId == myUserId,
        body = when {
            deleted -> "This message was deleted"
            !text.isNullOrBlank() -> text!!
            media != null -> "Photo"
            else -> ""
        },
        time = time,
        mediaUri = media,
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
    )
}
