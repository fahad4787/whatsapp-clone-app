package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveLocationListDto(
    @SerialName("items") val items: List<LiveLocationShareDto> = emptyList(),
)

@Serializable
data class LiveLocationStopResult(
    @SerialName("ok") val ok: Boolean = false,
    @SerialName("stopped") val stopped: Int = 0,
)
