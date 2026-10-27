package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CallLinkCreateBody(
    @SerialName("media") val media: String = "audio",
    @SerialName("title") val title: String? = null,
)
