package com.aivora.app.data.realtime

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * WebSocket: wss://wa.zirvik.com/ws/
 * Frame shapes recovered from APK realtime serializers.
 */
@Serializable
data class ClientFrame(
    val type: String,
    val data: JsonElement? = null,
)

@Serializable
data class ServerFrame(
    val type: String,
    val data: JsonElement? = null,
)

@Serializable
data class TypingData(
    @SerialName("conversation_id") val conversationId: String? = null,
    val typing: Boolean = false,
)

@Serializable
data class PresenceData(
    @SerialName("user_id") val userId: String? = null,
    val online: Boolean = false,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
)

@Serializable
data class ReceiptData(
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("message_id") val messageId: String? = null,
    val status: String? = null,
)

@Serializable
data class ConversationReadData(
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("message_id") val messageId: String? = null,
)

@Serializable
data class AuthOkData(
    @SerialName("user_id") val userId: String? = null,
)

@Serializable
data class AnsweredElsewhereData(
    @SerialName("call_id") val callId: String? = null,
)
