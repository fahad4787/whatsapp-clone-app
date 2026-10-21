package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallLinkDto(
    @SerialName("code") val code: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("scheduled_call_id") val scheduledCallId: String? = null,
    @SerialName("audio") val audio: Boolean = false,
)
