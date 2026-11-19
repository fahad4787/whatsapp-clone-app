package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelListDto(
    @SerialName("items") val items: List<ChannelDto> = emptyList(),
)

@Serializable
data class ChannelPostListDto(
    @SerialName("items") val items: List<ChannelPostDto> = emptyList(),
)

@Serializable
data class CommunityListDto(
    @SerialName("items") val items: List<CommunityDto> = emptyList(),
)
