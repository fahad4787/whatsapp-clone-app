package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PasskeySignInBody(
    @SerialName("state") val state: String? = null,
    @SerialName("credential") val credential: String? = null,
    @SerialName("device") val device: DeviceInfo? = null,
)
