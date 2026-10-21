package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class GroupCreateBody(
    @SerialName("title") val title: String? = null,
    @SerialName("member_ids") val memberIds: List<String> = emptyList(),
)
