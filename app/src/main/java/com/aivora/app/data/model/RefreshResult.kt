package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class RefreshResult(
    @SerialName("access") val access: String? = null,
    @SerialName("refresh") val refresh: String? = null,
    /** Present when OTP succeeded but two-step PIN is still required. */
    @SerialName("two_step_required") val twoStepRequired: Boolean = false,
    @SerialName("challenge") val challenge: String? = null,
)
