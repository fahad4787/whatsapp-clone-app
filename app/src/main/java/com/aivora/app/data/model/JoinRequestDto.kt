package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class JoinRequestDto(
    @SerialName("id") val id: String? = null,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
