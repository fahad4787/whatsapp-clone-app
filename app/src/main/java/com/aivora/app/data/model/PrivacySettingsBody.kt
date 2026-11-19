package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrivacySettingsBody(
    @SerialName("last_seen") val lastSeen: String? = null,
    @SerialName("online") val online: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("status_ids") val statusIds: List<String>? = null,
    @SerialName("profile_photo") val profilePhoto: String? = null,
    @SerialName("about") val about: String? = null,
    @SerialName("groups") val groups: String? = null,
    @SerialName("silence_unknown_callers") val silenceUnknownCallers: Boolean? = null,
    @SerialName("links") val links: String? = null,
    @SerialName("read_receipts") val readReceipts: Boolean? = null,
)
