package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LinkPreviewDto(
    @SerialName("url") val url: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("site") val site: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
)
