package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class TwoStepEnableBody(
    @SerialName("pin") val pin: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("current") val current: String? = null,
)
