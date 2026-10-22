package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeUpdateBody(
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("about") val about: String? = null,
)
