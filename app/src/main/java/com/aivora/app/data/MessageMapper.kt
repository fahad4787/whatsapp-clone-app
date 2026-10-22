package com.aivora.app.data

import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.model.MessageDto

fun MessageDto.toLocalMessage(myUserId: String): LocalMessage {
    val created = createdAt.orEmpty()
    val time = when {
        created.length >= 16 -> created.substring(11, 16)
        created.isNotBlank() -> created
        else -> "Now"
    }
    val media = attachment?.url?.takeIf { it.isNotBlank() }
    return LocalMessage(
        id = id.orEmpty(),
        mine = senderId == myUserId,
        body = text.orEmpty().ifBlank { if (media != null) "Photo" else "" },
        time = time,
        mediaUri = media,
    )
}
