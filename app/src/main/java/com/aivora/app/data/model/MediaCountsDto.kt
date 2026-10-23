package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaCountsDto(
    @SerialName("images") val images: Int = 0,
    @SerialName("videos") val videos: Int = 0,
    @SerialName("docs") val docs: Int = 0,
    @SerialName("audio") val audio: Int = 0,
    @SerialName("total") val total: Int = 0,
)
