package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusListDto(
    @SerialName("items") val items: List<StatusRowDto> = emptyList(),
)

@Serializable
data class StatusViewerListDto(
    @SerialName("items") val items: List<StatusViewerDto> = emptyList(),
)

@Serializable
data class StatusReactionBody(
    @SerialName("reaction") val reaction: String? = null,
)
