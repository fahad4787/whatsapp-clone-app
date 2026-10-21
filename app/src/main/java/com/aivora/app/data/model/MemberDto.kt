package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class MemberDto(
    @SerialName("user") val user: UserDto? = null,
    @SerialName("role") val role: String? = null,
    @SerialName("joined_at") val joinedAt: String? = null,
    @SerialName("member") val member: String? = null,
)
