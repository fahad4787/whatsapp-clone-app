package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusReplyDto(
    @SerialName("status_id") val statusId: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("background") val background: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
)
