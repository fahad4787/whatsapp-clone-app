package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class EventDto(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("starts_at") val startsAt: String? = null,
    @SerialName("ends_at") val endsAt: String? = null,
    @SerialName("with_call") val withCall: Boolean = false,
    @SerialName("call_media") val callMedia: String? = null,
    @SerialName("call_link") val callLink: CallLinkDto? = null,
    @SerialName("cancelled") val cancelled: Boolean = false,
    @SerialName("counts") val counts: EventCountsDto? = null,
    @SerialName("my_answer") val myAnswer: String? = null,
    @SerialName("my_plus_one") val myPlusOne: Boolean? = null,
    @SerialName("going") val going: List<EventRsvpDto>? = null,
    @SerialName("timezone") val timezone: String? = null,
    @SerialName("edited_at") val editedAt: String? = null,
    @SerialName("maybe") val maybe: List<EventRsvpDto>? = null,
    @SerialName("not_going") val notGoing: List<EventRsvpDto>? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
)
