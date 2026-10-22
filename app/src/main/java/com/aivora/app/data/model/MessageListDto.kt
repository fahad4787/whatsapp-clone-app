package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageListDto(
    @SerialName("items") val items: List<MessageDto> = emptyList(),
)
