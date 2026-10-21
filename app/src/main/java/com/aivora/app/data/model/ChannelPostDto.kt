package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChannelPostDto(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("attachment") val attachment: AttachmentDto? = null,
    @SerialName("poll") val poll: PollDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("deleted") val deleted: Boolean = false,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("reactions") val reactions: List<ReactionSummaryDto>? = null,
    @SerialName("my_reaction") val myReaction: String? = null,
)
