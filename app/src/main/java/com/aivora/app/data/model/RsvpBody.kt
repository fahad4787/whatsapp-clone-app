package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RsvpBody(
    @SerialName("answer") val answer: String,
    @SerialName("plus_one") val plusOne: Boolean? = null,
)
