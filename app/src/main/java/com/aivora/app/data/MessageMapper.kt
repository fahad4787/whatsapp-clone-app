package com.aivora.app.data

import com.aivora.app.data.local.LocalMessage
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
    )
}
