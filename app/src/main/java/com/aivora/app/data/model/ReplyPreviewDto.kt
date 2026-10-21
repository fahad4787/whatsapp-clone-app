package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ReplyPreviewDto(
    @SerialName("id") val id: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("deleted") val deleted: Boolean = false,
)
