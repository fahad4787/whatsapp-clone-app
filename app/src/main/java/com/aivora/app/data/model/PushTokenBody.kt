package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PushTokenBody(
    @SerialName("push_token") val pushToken: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
)
