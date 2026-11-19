package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceLinkStartBody(
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("platform") val platform: String? = null,
)

@Serializable
data class DeviceLinkStartResult(
    @SerialName("code") val code: String? = null,
    @SerialName("expires_in") val expiresIn: Int = 180,
    @SerialName("expires_at") val expiresAt: String? = null,
)

@Serializable
data class DeviceLinkCodeBody(
    @SerialName("code") val code: String,
)

@Serializable
data class DeviceLinkApproveResult(
    @SerialName("ok") val ok: Boolean = false,
    @SerialName("code") val code: String? = null,
    @SerialName("device_name") val deviceName: String? = null,
    @SerialName("platform") val platform: String? = null,
)

@Serializable
data class DeviceLinkPollResult(
    @SerialName("status") val status: String? = null,
    @SerialName("expires_in") val expiresIn: Int? = null,
    @SerialName("access") val access: String? = null,
    @SerialName("refresh") val refresh: String? = null,
    @SerialName("phone") val phone: String? = null,
)
