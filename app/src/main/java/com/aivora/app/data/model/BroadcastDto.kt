package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class BroadcastDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("recipient_count") val recipientCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
)
