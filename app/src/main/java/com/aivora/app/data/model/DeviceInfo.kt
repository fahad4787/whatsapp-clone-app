package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class DeviceInfo(
    @SerialName("platform") val platform: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
)
