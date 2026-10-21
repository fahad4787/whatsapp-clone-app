package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class SyncedUserDto(
    @SerialName("id") val id: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("phone_display") val phoneDisplay: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("about") val about: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("last_seen_at") val lastSeenAt: String? = null,
)
