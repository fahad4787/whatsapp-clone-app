package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class DeviceDto(
    @SerialName("id") val id: String? = null,
    @SerialName("platform") val platform: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("app_version") val appVersion: String? = null,
    @SerialName("last_active_at") val lastActiveAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("is_current") val isCurrent: Boolean = false,
    @SerialName("has_push_token") val hasPushToken: Boolean = false,
)
