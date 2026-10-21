package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ReactionSummaryDto(
    @SerialName("emoji") val emoji: String? = null,
    @SerialName("count") val count: Int = 0,
    @SerialName("user_ids") val userIds: List<String> = emptyList(),
)
