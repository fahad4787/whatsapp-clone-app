package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CommunityGroupDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("visible") val visible: Boolean = false,
    @SerialName("joined") val joined: Boolean = false,
)
