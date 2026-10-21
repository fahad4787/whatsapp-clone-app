package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CommunityDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("announcement_id") val announcementId: String? = null,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("group_count") val groupCount: Int = 0,
    @SerialName("role") val role: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("groups") val groups: List<CommunityGroupDto>? = null,
)
