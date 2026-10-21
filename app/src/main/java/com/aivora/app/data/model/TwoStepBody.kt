package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class TwoStepBody(
    @SerialName("challenge") val challenge: String? = null,
    @SerialName("pin") val pin: String? = null,
    @SerialName("device") val device: DeviceInfo? = null,
)
