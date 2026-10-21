package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusViewerDto(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("seen_at") val seenAt: String? = null,
    @SerialName("reaction") val reaction: String? = null,
)
