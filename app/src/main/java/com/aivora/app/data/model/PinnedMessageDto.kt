package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PinnedMessageDto(
    @SerialName("id") val id: String? = null,
    @SerialName("message") val message: MessageDto? = null,
    @SerialName("pinned_by") val pinnedBy: UserDto? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
)
