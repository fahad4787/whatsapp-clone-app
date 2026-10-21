package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PasskeyRegisterBody(
    @SerialName("state") val state: String? = null,
    @SerialName("credential") val credential: String? = null,
    @SerialName("name") val name: String? = null,
)
