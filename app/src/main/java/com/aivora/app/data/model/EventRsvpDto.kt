package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class EventRsvpDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("plus_one") val plusOne: Boolean = false,
)
