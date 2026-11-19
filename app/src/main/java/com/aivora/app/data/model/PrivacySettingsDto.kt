package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Privacy from GET/PUT /api/me/privacy. */
@Serializable
data class PrivacySettingsDto(
    @SerialName("last_seen") val lastSeen: String = "everyone",
    @SerialName("online") val online: String = "everyone",
    @SerialName("status") val status: String = "contacts",
    @SerialName("status_ids") val statusIds: List<String> = emptyList(),
    @SerialName("profile_photo") val profilePhoto: String = "everyone",
    @SerialName("about") val about: String = "everyone",
    @SerialName("groups") val groups: String = "everyone",
    @SerialName("silence_unknown_callers") val silenceUnknownCallers: Boolean = false,
    @SerialName("links") val links: String = "contacts",
    @SerialName("read_receipts") val readReceipts: Boolean = true,
)
