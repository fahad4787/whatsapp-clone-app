package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CommunityCreateBody(
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
)
