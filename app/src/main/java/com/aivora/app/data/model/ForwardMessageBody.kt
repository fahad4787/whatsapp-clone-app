package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForwardMessageBody(
    @SerialName("conversation_id") val conversationId: String,
)
