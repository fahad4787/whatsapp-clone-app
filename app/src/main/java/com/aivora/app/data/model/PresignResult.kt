package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class PresignResult(
    @SerialName("key") val key: String? = null,
    @SerialName("upload_url") val uploadUrl: String? = null,
    @SerialName("method") val method: String? = null,
    @SerialName("headers") val headers: JsonObject? = null,
    @SerialName("expires_in") val expiresIn: Int = 0,
    @SerialName("type") val type: String? = null,
    @SerialName("document") val document: String? = null,
)
