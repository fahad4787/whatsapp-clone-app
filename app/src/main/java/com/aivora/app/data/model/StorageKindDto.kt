package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class StorageKindDto(
    @SerialName("kind") val kind: String? = null,
    @SerialName("count") val count: Int = 0,
    @SerialName("bytes") val bytes: Int = 0,
)
