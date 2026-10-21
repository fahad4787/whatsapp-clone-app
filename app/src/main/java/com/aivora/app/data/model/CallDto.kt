package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallDto(
    @SerialName("id") val id: String? = null,
    @SerialName("kind") val kind: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("initiator_id") val initiatorId: String? = null,
    @SerialName("room_name") val roomName: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("drop_in") val dropIn: Boolean = false,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("answered_at") val answeredAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
    @SerialName("participants") val participants: List<CallParticipantDto>? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("caller") val caller: CallerDto? = null,
    @SerialName("group_title") val groupTitle: String? = null,
    @SerialName("removed") val removed: Boolean = false,
    @SerialName("silenced") val silenced: Boolean = false,
)
