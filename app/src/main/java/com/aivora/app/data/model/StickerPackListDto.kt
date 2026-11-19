package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StickerPackListDto(
    @SerialName("items") val items: List<StickerPackDto> = emptyList(),
)
