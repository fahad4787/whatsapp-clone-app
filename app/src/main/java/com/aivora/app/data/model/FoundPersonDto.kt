package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class FoundPersonDto(
    @SerialName("id") val id: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("about") val about: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
