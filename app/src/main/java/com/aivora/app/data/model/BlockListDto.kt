package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlockListDto(
    @SerialName("items") val items: List<BlockItemDto> = emptyList(),
)

@Serializable
data class BlockItemDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
