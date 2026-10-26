package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** last_seen / online / status privacy from GET /api/me/privacy. */
@Serializable
data class PrivacySettingsDto(
    @SerialName("last_seen") val lastSeen: String = "everyone",
    @SerialName("online") val online: String = "everyone",
    @SerialName("status") val status: String = "contacts",
    @SerialName("status_ids") val statusIds: List<String> = emptyList(),
)
