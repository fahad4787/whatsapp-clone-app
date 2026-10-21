package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class EventCallDto(
    @SerialName("code") val code: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("audio") val audio: Boolean = false,
)
