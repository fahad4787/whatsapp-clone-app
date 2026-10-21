package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallTokenResult(
    @SerialName("call") val call: CallDto? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("livekit_url") val livekitUrl: String? = null,
    @SerialName("invited") val invited: Boolean = false,
)
