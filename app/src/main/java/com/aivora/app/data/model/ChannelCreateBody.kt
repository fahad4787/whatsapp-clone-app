package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChannelCreateBody(
    @SerialName("name") val name: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("listed") val listed: Boolean = false,
)
