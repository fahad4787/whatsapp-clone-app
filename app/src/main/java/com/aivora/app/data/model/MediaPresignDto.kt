package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MediaPresignBody(
    @SerialName("content_type") val contentType: String,
    @SerialName("filename") val filename: String? = null,
    @SerialName("size") val size: Int? = null,
)

@Serializable
data class MediaPresignDto(
    @SerialName("key") val key: String? = null,
    @SerialName("content_type") val contentType: String? = null,
    @SerialName("upload_url") val uploadUrl: String? = null,
    @SerialName("public_url") val publicUrl: String? = null,
    @SerialName("expires_in") val expiresIn: Int = 0,
)

@Serializable
data class MediaUploadResultDto(
    @SerialName("key") val key: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("mime") val mime: String? = null,
    @SerialName("size") val size: Int = 0,
)
