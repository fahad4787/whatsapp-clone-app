package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallInviteStatusDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("status") val status: String? = null,
)
