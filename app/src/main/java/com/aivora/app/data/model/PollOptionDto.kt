package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PollOptionDto(
    @SerialName("id") val id: String? = null,
    @SerialName("text") val text: String? = null,
    @SerialName("votes") val votes: Int = 0,
    @SerialName("voter_ids") val voterIds: List<String> = emptyList(),
    @SerialName("mine") val mine: Boolean = false,
    @SerialName("vote_times") val voteTimes: List<String>? = null,
)
