package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class MembersBody(
    @SerialName("user_ids") val userIds: List<String> = emptyList(),
)
