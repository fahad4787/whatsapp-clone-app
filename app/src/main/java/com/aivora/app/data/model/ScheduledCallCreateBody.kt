package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledCallCreateBody(
    @SerialName("title") val title: String,
    @SerialName("media") val media: String = "audio",
    @SerialName("starts_at") val startsAt: String,
    @SerialName("duration_minutes") val durationMinutes: Int = 30,
    @SerialName("reminder_minutes") val reminderMinutes: Int = 10,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("user_ids") val userIds: List<String> = emptyList(),
)

@Serializable
data class ScheduledCallRespondBody(
    @SerialName("action") val action: String,
)

@Serializable
data class ScheduledCallUpdateBody(
    @SerialName("title") val title: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("reminder_minutes") val reminderMinutes: Int? = null,
)
