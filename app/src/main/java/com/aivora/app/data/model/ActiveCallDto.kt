package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ActiveCallDto(
    @SerialName("id") val id: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("drop_in") val dropIn: Boolean = false,
    @SerialName("participant_count") val participantCount: Int = 0,
    @SerialName("participant_ids") val participantIds: List<String> = emptyList(),
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("audio") val audio: Boolean = false,
    @SerialName("active") val active: Boolean = false,
)
