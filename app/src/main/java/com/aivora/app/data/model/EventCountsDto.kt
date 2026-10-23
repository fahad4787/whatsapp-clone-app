package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventCountsDto(
    @SerialName("going") val going: Int = 0,
    @SerialName("maybe") val maybe: Int = 0,
    @SerialName("not_going") val notGoing: Int = 0,
)
