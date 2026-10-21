package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ScheduledParticipantDto(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("is_organizer") val isOrganizer: Boolean = false,
    @SerialName("invited") val invited: Boolean = false,
)
