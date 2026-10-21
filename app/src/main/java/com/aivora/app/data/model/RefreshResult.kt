package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class RefreshResult(
    @SerialName("access") val access: String? = null,
    @SerialName("refresh") val refresh: String? = null,
)
