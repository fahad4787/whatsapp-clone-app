package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LinkCodeBody(
    @SerialName("code") val code: String? = null,
    @SerialName("secret") val secret: String? = null,
    @SerialName("remember") val remember: Boolean = false,
)
