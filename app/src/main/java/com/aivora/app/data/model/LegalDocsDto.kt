package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LegalDocsDto(
    @SerialName("privacy_policy") val privacyPolicy: String = "",
    @SerialName("terms_of_service") val termsOfService: String = "",
)
