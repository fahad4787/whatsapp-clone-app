package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PinResetConfirmBody(
    @SerialName("challenge") val challenge: String? = null,
    @SerialName("code") val code: String? = null,
)
