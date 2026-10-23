package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoteBody(
    @SerialName("option_id") val optionId: String,
)
