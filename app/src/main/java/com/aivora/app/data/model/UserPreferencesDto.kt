package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPreferencesDto(
    @SerialName("default_disappearing_seconds") val defaultDisappearingSeconds: Int = 0,
    @SerialName("protect_ip_calls") val protectIpCalls: Boolean = true,
    @SerialName("disable_link_previews") val disableLinkPreviews: Boolean = false,
    @SerialName("block_unknown") val blockUnknown: Boolean = false,
)

@Serializable
data class UserPreferencesPatchBody(
    @SerialName("default_disappearing_seconds") val defaultDisappearingSeconds: Int? = null,
    @SerialName("protect_ip_calls") val protectIpCalls: Boolean? = null,
    @SerialName("disable_link_previews") val disableLinkPreviews: Boolean? = null,
    @SerialName("block_unknown") val blockUnknown: Boolean? = null,
)
