package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PollCreateBody(
    @SerialName("question") val question: String? = null,
    @SerialName("options") val options: List<String> = emptyList(),
    @SerialName("multiple") val multiple: Boolean = false,
)
