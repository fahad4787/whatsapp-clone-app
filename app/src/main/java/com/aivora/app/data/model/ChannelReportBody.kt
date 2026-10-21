package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class ChannelReportBody(
    @SerialName("reason") val reason: String? = null,
    @SerialName("post_id") val postId: String? = null,
)
