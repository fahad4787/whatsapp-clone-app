package com.aivora.app.data

import com.aivora.app.data.local.LocalContact
import com.aivora.app.data.local.LocalEvent
import com.aivora.app.data.local.LocalLinkPreview
import com.aivora.app.data.local.LocalLocation
import com.aivora.app.data.local.LocalMediaKind
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.LocalPoll
import com.aivora.app.data.local.LocalPollOption
import com.aivora.app.data.local.LocalReaction
import com.aivora.app.data.local.LocalReplyPreview
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.data.local.looksLikeSystemNotice
import com.aivora.app.data.model.MessageDto

private const val DISAPPEARED_TEXT = "This message has disappeared"

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
    val localPoll = if (deleted) null else poll?.let { p ->
        LocalPoll(
            question = p.question.orEmpty(),
            multiple = p.multiple,
            closed = p.closed,
            totalVoters = p.totalVoters,
            options = p.options.map { o ->
                // Never trust DTO `mine` from WS (computed for the voter, not the viewer).
                LocalPollOption(
                    id = o.id.orEmpty(),
                    text = o.text.orEmpty(),
                    votes = o.votes,
                    mine = o.voterIds.contains(myUserId),
                )
            },
        )
    }
    val localContact = if (deleted) null else contact?.let { c ->
        LocalContact(name = c.name.orEmpty(), phones = c.phones, userId = c.userId)
    }
    val localLocation = if (deleted) null else location?.let { l ->
        val lat = l.lat
        val lng = l.lng
        if (lat != null && lng != null) {
            LocalLocation(lat = lat, lng = lng, name = l.name, address = l.address)
        } else {
            null
        }
    }
    val localEvent = if (deleted) null else event?.let { e ->
        val myAnswer = when {
            e.going.orEmpty().any { it.userId == myUserId } -> "going"
            e.maybe.orEmpty().any { it.userId == myUserId } -> "maybe"
            e.notGoing.orEmpty().any { it.userId == myUserId } -> "not_going"
            else -> null
        }
        LocalEvent(
            title = e.title.orEmpty(),
            description = e.description,
            location = e.location,
            startsAt = e.startsAt.orEmpty(),
            endsAt = e.endsAt,
            withCall = e.withCall,
            cancelled = e.cancelled,
            going = e.counts?.going ?: 0,
            maybe = e.counts?.maybe ?: 0,
            notGoing = e.counts?.notGoing ?: 0,
            myAnswer = myAnswer,
        )
    }
    val disappeared = !deleted && (
        text == DISAPPEARED_TEXT ||
            expiresAt?.let { exp ->
                runCatching { java.time.Instant.parse(exp).isBefore(java.time.Instant.now()) }.getOrDefault(false)
            } == true
        )
    val richBody = when {
        localPoll != null -> "Poll: ${localPoll.question}"
        localContact != null -> "Contact: ${localContact.name}"
        localLocation != null -> "Location"
        localEvent != null -> "Event: ${localEvent.title}"
        else -> null
    }
    return LocalMessage(
        id = id.orEmpty(),
        mine = senderId == myUserId,
        body = when {
            deleted -> "This message was deleted"
            disappeared -> DISAPPEARED_TEXT
            !text.isNullOrBlank() -> text!!
            richBody != null -> richBody
            viewOnce && media == null -> "Opened"
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
        poll = localPoll,
        contact = localContact,
        location = localLocation,
        event = localEvent,
        expiresAtIso = expiresAt,
        disappeared = disappeared,
        system = system ||
            type.equals("system", ignoreCase = true) ||
            looksLikeSystemNotice(text.orEmpty()),
        editedAtIso = editedAt,
        viewOnce = viewOnce,
    )
}
