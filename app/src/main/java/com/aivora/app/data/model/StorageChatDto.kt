package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Chat storage row — accepts Wave 24 server shape + APK legacy fields. */
@Serializable
data class StorageChatDto(
    @SerialName("id") val id: String? = null,
    @SerialName("conversation_id") val conversationId: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("count") val count: Int = 0,
    @SerialName("bytes") val bytes: Long = 0,
) {
    fun chatId(): String? = conversationId ?: id
}
