package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class UserDto(
    @SerialName("id") val id: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("phone_display") val phoneDisplay: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("about") val about: String? = null,
    @SerialName("about_updated_at") val aboutUpdatedAt: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("avatar_thumb") val avatarThumb: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null,
    @SerialName("wallpaper") val wallpaper: String? = null,
    @SerialName("username_key") val usernameKey: String? = null,
    @SerialName("delete_confirmation") val deleteConfirmation: String? = null,
    @SerialName("unverified_phone") val unverifiedPhone: String? = null,
    @SerialName("unverified_phone_display") val unverifiedPhoneDisplay: String? = null,
)
