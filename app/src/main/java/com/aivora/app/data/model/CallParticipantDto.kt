package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallParticipantDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("joined_at") val joinedAt: String? = null,
    @SerialName("left_at") val leftAt: String? = null,
    @SerialName("silenced") val silenced: Boolean = false,
    @SerialName("ringing") val ringing: Boolean = false,
)
