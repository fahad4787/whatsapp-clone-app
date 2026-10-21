package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StatusRowDto(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("statuses") val statuses: List<StatusItemDto>? = null,
    @SerialName("unseen") val unseen: String? = null,
    @SerialName("latest_at") val latestAt: String? = null,
)
