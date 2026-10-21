package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ReportRequestBody(
    @SerialName("target_user_id") val targetUserId: String? = null,
    @SerialName("target_conversation_id") val targetConversationId: String? = null,
    @SerialName("reason") val reason: String? = null,
    @SerialName("message_ids") val messageIds: List<String> = emptyList(),
    @SerialName("block") val block: Boolean = false,
)
