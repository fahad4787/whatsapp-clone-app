package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class SendMessageBody(
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("reply_to") val replyTo: ReplyPreviewDto? = null,
    @SerialName("reply_to_status") val replyToStatus: String? = null,
    @SerialName("attachment") val attachment: AttachmentDto? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("live_location") val liveLocation: String? = null,
    @SerialName("contacts") val contacts: String? = null,
    @SerialName("poll") val poll: PollDto? = null,
    @SerialName("event") val event: EventDto? = null,
    @SerialName("view_once") val viewOnce: Boolean = false,
    @SerialName("voice_note") val voiceNote: String? = null,
)
