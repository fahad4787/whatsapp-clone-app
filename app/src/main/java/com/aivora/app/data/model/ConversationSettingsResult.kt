package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationSettingsResult(
    @SerialName("id") val id: String? = null,
    @SerialName("archived") val archived: Boolean = false,
    @SerialName("pinned") val pinned: Boolean = false,
    @SerialName("favorite") val favorite: Boolean = false,
    @SerialName("wallpaper") val wallpaper: String? = null,
    @SerialName("muted_until") val mutedUntil: String? = null,
    @SerialName("blocked_by_me") val blockedByMe: Boolean = false,
    @SerialName("blocked_me") val blockedMe: Boolean = false,
)
