package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ContactSyncBody(
    @SerialName("phones") val phones: List<String> = emptyList(),
)
