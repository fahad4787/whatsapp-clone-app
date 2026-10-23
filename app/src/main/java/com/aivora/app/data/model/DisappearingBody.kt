package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DisappearingBody(
    @SerialName("seconds") val seconds: Int,
)
