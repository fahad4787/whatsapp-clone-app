package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageReceiptDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("at") val at: String? = null,
)

@Serializable
data class MessageInfoDto(
    @SerialName("message") val message: MessageDto? = null,
    @SerialName("delivered") val delivered: List<MessageReceiptDto> = emptyList(),
    @SerialName("read") val read: List<MessageReceiptDto> = emptyList(),
)
