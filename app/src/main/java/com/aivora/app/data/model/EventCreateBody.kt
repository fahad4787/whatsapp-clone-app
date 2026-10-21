package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class EventCreateBody(
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("with_call") val withCall: Boolean = false,
    @SerialName("call_media") val callMedia: String? = null,
    @SerialName("timezone") val timezone: String? = null,
)
