package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class UsernameCheckDto(
    @SerialName("available") val available: Boolean = false,
    @SerialName("reason") val reason: String? = null,
)
