package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class OtpRequestBody(
    @SerialName("country") val country: String? = null,
    @SerialName("national_number") val nationalNumber: String? = null,
)
