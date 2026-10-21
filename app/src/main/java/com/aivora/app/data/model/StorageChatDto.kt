package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StorageChatDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("count") val count: Int = 0,
    @SerialName("bytes") val bytes: Int = 0,
)
