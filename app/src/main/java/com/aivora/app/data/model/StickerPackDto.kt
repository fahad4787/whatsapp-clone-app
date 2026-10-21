package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StickerPackDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("publisher") val publisher: String? = null,
    @SerialName("featured") val featured: Boolean = false,
    @SerialName("stickers") val stickers: List<StickerDto>? = null,
)
