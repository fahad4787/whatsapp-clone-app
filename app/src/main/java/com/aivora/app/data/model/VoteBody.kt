package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class VoteBody(
    @SerialName("option_ids") val optionIds: List<String> = emptyList(),
)
