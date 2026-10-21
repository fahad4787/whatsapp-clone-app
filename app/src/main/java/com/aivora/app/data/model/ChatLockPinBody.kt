package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChatLockPinBody(
    @SerialName("pin") val pin: String? = null,
    @SerialName("current") val current: String? = null,
)
