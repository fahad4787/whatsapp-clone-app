package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class LocationBody(
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("live") val live: Boolean = false,
    @SerialName("accuracy") val accuracy: Double? = null,
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("ended") val ended: Boolean = false,
    @SerialName("updated_at") val updatedAt: String? = null,
)
