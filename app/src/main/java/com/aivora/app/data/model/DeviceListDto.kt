package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceListDto(
    @SerialName("items") val items: List<DeviceDto> = emptyList(),
)
