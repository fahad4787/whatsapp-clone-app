package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TwoStepStatusDto(
    @SerialName("enabled") val enabled: Boolean = false,
    @SerialName("enabled_at") val enabledAt: String? = null,
    @SerialName("email") val email: String? = null,
)
