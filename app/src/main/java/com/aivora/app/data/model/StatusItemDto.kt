package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("background") val background: String? = null,
    @SerialName("attachment") val attachment: AttachmentDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("seen") val seen: Boolean = false,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("my_reaction") val myReaction: String? = null,
)
