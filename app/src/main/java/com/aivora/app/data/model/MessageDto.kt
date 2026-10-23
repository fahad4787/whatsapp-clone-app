package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class MessageDto(
    @SerialName("id") val id: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("attachment") val attachment: AttachmentDto? = null,
    @SerialName("preview") val preview: LinkPreviewDto? = null,
    @SerialName("poll") val poll: PollDto? = null,
    @SerialName("event") val event: EventDto? = null,
    @SerialName("contact") val contact: ContactCardDto? = null,
    @SerialName("location") val location: LocationBody? = null,
    @SerialName("mentions") val mentions: List<String> = emptyList(),
    @SerialName("view_once") val viewOnce: Boolean = false,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("reply_to") val replyTo: ReplyPreviewDto? = null,
    @SerialName("status_reply") val statusReply: StatusReplyDto? = null,
    @SerialName("forwarded") val forwarded: Boolean = false,
    @SerialName("frequently_forwarded") val frequentlyForwarded: Boolean = false,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("deleted_for_everyone_at") val deletedForEveryoneAt: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("reactions") val reactions: List<ReactionSummaryDto>? = null,
    @SerialName("starred") val starred: Boolean = false,
    @SerialName("silenced") val silenced: Boolean = false,
    @SerialName("system") val system: Boolean = false,
)
