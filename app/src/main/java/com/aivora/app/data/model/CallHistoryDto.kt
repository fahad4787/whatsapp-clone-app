package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallHistoryListDto(
    @SerialName("items") val items: List<CallHistoryItemDto> = emptyList(),
)

@Serializable
data class CallHistoryItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("ended_at") val endedAt: String? = null,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
    @SerialName("peer_name") val peerName: String? = null,
    @SerialName("peer_user_id") val peerUserId: String? = null,
    @SerialName("peer_avatar_url") val peerAvatarUrl: String? = null,
    @SerialName("direction") val direction: String? = null,
    @SerialName("missed") val missed: Boolean = false,
    @SerialName("video") val video: Boolean = false,
)
