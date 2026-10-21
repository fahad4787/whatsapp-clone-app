package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PollDto(
    @SerialName("id") val id: String? = null,
    @SerialName("question") val question: String? = null,
    @SerialName("multiple") val multiple: Boolean = false,
    @SerialName("closed") val closed: Boolean = false,
    @SerialName("total_voters") val totalVoters: Int = 0,
    @SerialName("options") val options: List<String> = emptyList(),
)
