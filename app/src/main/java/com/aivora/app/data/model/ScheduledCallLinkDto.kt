package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ScheduledCallLinkDto(
    @SerialName("code") val code: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("opens_at") val opensAt: String? = null,
    @SerialName("closes_at") val closesAt: String? = null,
    @SerialName("revoked") val revoked: String? = null,
)
