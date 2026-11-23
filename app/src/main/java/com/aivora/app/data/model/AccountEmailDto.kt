package com.aivora.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountEmailDto(
    @SerialName("email") val email: String? = null,
    @SerialName("verified") val verified: Boolean = false,
    @SerialName("verified_at") val verifiedAt: String? = null,
)

@Serializable
data class AccountEmailRequestBody(
    @SerialName("email") val email: String,
)

@Serializable
data class AccountEmailVerifyBody(
    @SerialName("email") val email: String,
    @SerialName("code") val code: String,
)

@Serializable
data class EmailOtpRequestResult(
    @SerialName("email") val email: String? = null,
    @SerialName("expires_in") val expiresIn: Int = 300,
    @SerialName("resend_in") val resendIn: Int = 60,
    @SerialName("otp_via_admin") val otpViaAdmin: Boolean = false,
)
