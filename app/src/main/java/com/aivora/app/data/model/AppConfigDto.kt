package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigDto(
    @SerialName("maintenance_mode") val maintenanceMode: Boolean = false,
    @SerialName("latest_android_version") val latestAndroidVersion: String? = null,
    @SerialName("latest_android_version_code") val latestAndroidVersionCode: Int = 0,
    @SerialName("min_android_version_code") val minAndroidVersionCode: Int = 1,
    @SerialName("download_url") val downloadUrl: String? = null,
    @SerialName("allow_people_search") val allowPeopleSearch: Boolean = true,
    @SerialName("min_search_chars") val minSearchChars: Int = 3,
    @SerialName("ring_timeout") val ringTimeout: Int = 60,
    @SerialName("max_group_size") val maxGroupSize: Int = 256,
    @SerialName("max_group_call_participants") val maxGroupCallParticipants: Int = 32,
    @SerialName("upload_limit_mb") val uploadLimitMb: Int = 100,
    @SerialName("countries") val countries: List<AppCountryDto> = emptyList(),
)

@Serializable
data class AppCountryDto(
    @SerialName("iso") val iso: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("dial") val dial: String? = null,
    @SerialName("enabled") val enabled: Boolean = true,
)
