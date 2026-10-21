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
)
