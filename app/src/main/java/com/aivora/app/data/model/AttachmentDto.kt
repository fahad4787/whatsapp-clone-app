package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class AttachmentDto(
    @SerialName("key") val key: String? = null,
    @SerialName("mime") val mime: String? = null,
    @SerialName("size") val size: Int = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("width") val width: Int = 0,
    @SerialName("height") val height: Int = 0,
    @SerialName("duration") val duration: Int = 0,
    @SerialName("waveform") val waveform: List<Float>? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("thumbnail_key") val thumbnailKey: String? = null,
    @SerialName("pages") val pages: Int = 0,
    @SerialName("processing") val processing: Boolean = false,
    @SerialName("preview_thumb") val previewThumb: String? = null,
    @SerialName("cleared") val cleared: Boolean = false,
    @SerialName("opened") val opened: Boolean = false,
    @SerialName("opened_count") val openedCount: Int = 0,
    @SerialName("lat") val lat: Double? = null,
    @SerialName("lng") val lng: Double? = null,
    @SerialName("accuracy") val accuracy: Double? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("stopped") val stopped: String? = null,
    @SerialName("contacts") val contacts: String? = null,
    @SerialName("emoji") val emoji: String? = null,
    @SerialName("animated") val animated: Boolean = false,
    @SerialName("call_id") val callId: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("end_reason") val endReason: String? = null,
    @SerialName("drop_in") val dropIn: Boolean = false,
    @SerialName("initiator_id") val initiatorId: String? = null,
    @SerialName("scheduled_call_id") val scheduledCallId: String? = null,
)
