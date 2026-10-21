package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallerDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
