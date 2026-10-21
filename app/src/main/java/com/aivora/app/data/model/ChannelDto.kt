package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChannelDto(
    @SerialName("id") val id: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("listed") val listed: Boolean = false,
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("last_post_at") val lastPostAt: String? = null,
    @SerialName("following") val following: Boolean = false,
    @SerialName("muted") val muted: Boolean = false,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    @SerialName("url") val url: String? = null,
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("last_post_preview") val lastPostPreview: String? = null,
)
