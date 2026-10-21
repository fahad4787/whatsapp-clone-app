package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class EmailSignInVerifyBody(
    @SerialName("email") val email: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("device") val device: DeviceInfo? = null,
)
