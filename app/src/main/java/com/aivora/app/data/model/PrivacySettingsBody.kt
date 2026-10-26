package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettingsBody(
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("online") val online: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("status_ids") val statusIds: List<String>? = null,
)
