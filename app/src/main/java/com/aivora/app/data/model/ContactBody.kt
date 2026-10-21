package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ContactBody(
    @SerialName("phone") val phone: String? = null,
    @SerialName("saved_name") val savedName: String? = null,
)
