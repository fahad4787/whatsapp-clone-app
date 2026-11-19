package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorageDto(
    @SerialName("used_bytes") val usedBytes: Long = 0,
    @SerialName("media_bytes") val mediaBytes: Long = 0,
    @SerialName("chats") val chats: List<StorageChatDto> = emptyList(),
)
