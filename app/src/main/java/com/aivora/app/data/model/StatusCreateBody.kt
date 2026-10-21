package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusCreateBody(
    @SerialName("kind") val kind: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("background") val background: String? = null,
    @SerialName("attachment") val attachment: AttachmentDto? = null,
    @SerialName("audience") val audience: String? = null,
    @SerialName("audience_ids") val audienceIds: List<String> = emptyList(),
)
