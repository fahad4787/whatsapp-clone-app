package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StickerDto(
    @SerialName("id") val id: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("emoji") val emoji: String? = null,
    @SerialName("animated") val animated: Boolean = false,
    @SerialName("label") val label: String? = null,
)
