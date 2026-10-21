package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LiveLocationShareDto(
    @SerialName("message_id") val messageId: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("device_id") val deviceId: String? = null,
)
