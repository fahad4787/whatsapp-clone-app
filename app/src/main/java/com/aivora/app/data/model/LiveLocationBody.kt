package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LiveLocationBody(
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("accuracy") val accuracy: Double? = null,
    @SerialName("duration_seconds") val durationSeconds: Int = 0,
)
