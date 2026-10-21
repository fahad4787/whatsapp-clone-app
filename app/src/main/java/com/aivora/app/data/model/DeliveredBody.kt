package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class DeliveredBody(
    @SerialName("message_ids") val messageIds: List<String> = emptyList(),
)
