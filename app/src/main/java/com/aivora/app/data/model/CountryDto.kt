package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Reconstructed from APK kotlinx.serialization descriptor. */
@Serializable
data class CountryDto(
    @SerialName("iso2") val iso2: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("dial_code") val dialCode: String? = null,
)
