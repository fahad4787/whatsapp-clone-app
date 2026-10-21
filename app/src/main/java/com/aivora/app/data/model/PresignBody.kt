package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PresignBody(
    @SerialName("mime") val mime: String? = null,
    @SerialName("size") val size: Int = 0,
)
