package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ContactDto(
    @SerialName("id") val id: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("phone_display") val phoneDisplay: String? = null,
    @SerialName("saved_name") val savedName: String? = null,
    @SerialName("matched_user") val matchedUser: UserDto? = null,
    @SerialName("created_at") val createdAt: String? = null,
)
