package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusAudienceBody(
    @SerialName("status_audience") val statusAudience: String? = null,
    @SerialName("status_audience_ids") val statusAudienceIds: List<String> = emptyList(),
)
