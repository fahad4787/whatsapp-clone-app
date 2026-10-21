package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallStartBody(
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("user_ids") val userIds: List<String> = emptyList(),
)
