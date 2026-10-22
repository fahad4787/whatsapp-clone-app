package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationListDto(
    @SerialName("items") val items: List<ConversationDto> = emptyList(),
)

@Serializable
data class PeopleSearchDto(
    @SerialName("items") val items: List<UserDto> = emptyList(),
)
