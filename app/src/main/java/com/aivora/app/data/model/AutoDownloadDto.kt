package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutoDownloadDto(
    @SerialName("mobile") val mobile: String = "photos",
    @SerialName("wifi") val wifi: String = "all",
    @SerialName("roaming") val roaming: String = "none",
    @SerialName("less_data_calls") val lessDataCalls: Boolean = false,
    @SerialName("photo_upload_quality") val photoUploadQuality: String = "auto",
)

@Serializable
data class AutoDownloadPatchBody(
    @SerialName("mobile") val mobile: String? = null,
    @SerialName("wifi") val wifi: String? = null,
    @SerialName("roaming") val roaming: String? = null,
    @SerialName("less_data_calls") val lessDataCalls: Boolean? = null,
    @SerialName("photo_upload_quality") val photoUploadQuality: String? = null,
)
