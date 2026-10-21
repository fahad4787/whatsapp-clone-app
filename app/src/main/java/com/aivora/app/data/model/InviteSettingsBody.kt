package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class InviteSettingsBody(
    @SerialName("requires_approval") val requiresApproval: String? = null,
)
