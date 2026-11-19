package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatLockDto(
    @SerialName("enabled") val enabled: Boolean = false,
    @SerialName("enabled_at") val enabledAt: String? = null,
    @SerialName("hide_folder") val hideFolder: Boolean = false,
    @SerialName("locked_count") val lockedCount: Int = 0,
)

@Serializable
data class ChatLockUnlockResult(
    @SerialName("ok") val ok: Boolean = false,
)
