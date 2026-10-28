package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduledCallListDto(
    @SerialName("items") val items: List<ScheduledCallDto> = emptyList(),
)
