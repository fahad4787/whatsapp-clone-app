package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ScheduledCallDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    @SerialName("reminder_minutes") val reminderMinutes: Int = 0,
    @SerialName("organizer") val organizer: UserDto? = null,
    @SerialName("participants") val participants: List<CallParticipantDto>? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("call_link") val callLink: CallLinkDto? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("organizer_id") val organizerId: String? = null,
    @SerialName("invite_message_id") val inviteMessageId: String? = null,
    @SerialName("my_status") val myStatus: String? = null,
    @SerialName("audio") val audio: Boolean = false,
    @SerialName("scheduled") val scheduled: Boolean = false,
)
