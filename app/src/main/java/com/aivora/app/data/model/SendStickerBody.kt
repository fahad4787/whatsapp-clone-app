package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class SendStickerBody(
    @SerialName("sticker_id") val stickerId: String? = null,
    @SerialName("client_id") val clientId: String? = null,
    @SerialName("reply_to") val replyTo: ReplyPreviewDto? = null,
)
