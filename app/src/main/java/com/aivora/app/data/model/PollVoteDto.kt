package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PollVoteDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("voted_at") val votedAt: String? = null,
)
