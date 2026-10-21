package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CallLinkJoinDto(
    @SerialName("code") val code: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("media") val media: String? = null,
    @SerialName("room_name") val roomName: String? = null,
    @SerialName("token") val token: String? = null,
    @SerialName("livekit_url") val livekitUrl: String? = null,
    @SerialName("host") val host: UserDto? = null,
    @SerialName("call") val call: CallDto? = null,
    @SerialName("audio") val audio: Boolean = false,
)
