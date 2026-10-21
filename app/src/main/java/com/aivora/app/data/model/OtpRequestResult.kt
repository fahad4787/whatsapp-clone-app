package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class OtpRequestResult(
    @SerialName("phone") val phone: String? = null,
    @SerialName("expires_in") val expiresIn: Int = 0,
    @SerialName("resend_in") val resendIn: Int = 0,
)
