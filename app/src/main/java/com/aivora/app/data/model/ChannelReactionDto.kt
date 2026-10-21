package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChannelReactionDto(
    @SerialName("emoji") val emoji: String? = null,
    @SerialName("count") val count: Int = 0,
)
