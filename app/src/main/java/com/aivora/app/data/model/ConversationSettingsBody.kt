package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConversationSettingsBody(
    @SerialName("archived") val archived: Boolean? = null,
    @SerialName("pinned") val pinned: Boolean? = null,
    @SerialName("favorite") val favorite: Boolean? = null,
    @SerialName("wallpaper") val wallpaper: String? = null,
    /** true = mute 8h, false = unmute. Prefer this over muted_until for kotlinx null omission. */
    @SerialName("muted") val muted: Boolean? = null,
    @SerialName("muted_until") val mutedUntil: String? = null,
)
