package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ForwardBody(
    @SerialName("conversation_ids") val conversationIds: List<String> = emptyList(),
)
