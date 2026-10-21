package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LinkStartedDto(
    @SerialName("code") val code: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("secret") val secret: String? = null,
)
