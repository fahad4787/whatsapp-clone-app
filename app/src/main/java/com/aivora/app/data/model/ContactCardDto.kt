package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ContactCardDto(
    @SerialName("name") val name: String? = null,
    @SerialName("phones") val phones: List<String> = emptyList(),
    @SerialName("user_id") val userId: String? = null,
)
