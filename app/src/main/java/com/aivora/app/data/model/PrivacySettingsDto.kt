package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** last_seen: everyone|contacts|nobody; online: everyone|same_as_last_seen|nobody. */
@Serializable
data class PrivacySettingsDto(
    @SerialName("last_seen") val lastSeen: String = "everyone",
    @SerialName("online") val online: String = "everyone",
)
