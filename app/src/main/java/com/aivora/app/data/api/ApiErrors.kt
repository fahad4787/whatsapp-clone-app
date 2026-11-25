package com.aivora.app.data.api

import retrofit2.HttpException

/** Map Retrofit failures to short user-facing copy. */
object ApiErrors {
    /** Auth-specific copy (OTP / PIN / network) instead of raw "HTTP 401". */
    fun auth(t: Throwable, fallback: String): String {
        if (t is java.io.IOException) {
            return "No connection. Check your internet and try again"
        }
        val http = t as? HttpException
        val body = http?.response()?.errorBody()?.string().orEmpty()
        return when {
            body.contains("invalid_pin") -> "Wrong PIN. Try again"
            body.contains("invalid_challenge") -> "Session expired. Go back and enter your number again"
            body.contains("invalid_or_expired") -> "Code expired. Tap Resend code"
            body.contains("too_many_attempts") -> "Too many wrong attempts. Tap Resend code"
            body.contains("invalid_code") -> "Wrong code. Check it and try again"
            body.contains("rate_limited") -> "Code already sent. Please wait a moment"
            body.contains("banned") -> "This account has been banned"
            body.contains("country_disabled") -> "Sign-in isn’t available in your country"
            body.contains("maintenance") -> "Aivora is under maintenance. Try again soon"
            body.contains("sms_failed") || body.contains("smtp_send_failed") -> "Couldn’t send the code. Try again"
            http?.code() == 401 -> "Wrong code or it expired. Tap Resend code"
            http?.code() == 429 -> "Too many tries. Please wait a moment"
            http != null && http.code() >= 500 -> "Server is waking up. Try again in a few seconds"
            else -> fallback
        }
    }

    /** Why a call couldn't start, instead of "Call failed: HTTP 403". */
    fun call(t: Throwable): String {
        if (t is java.io.IOException) return "No connection. Check your internet and try again"
        val http = t as? HttpException
        val body = http?.response()?.errorBody()?.string().orEmpty()
        return when {
            body.contains("blocked") -> "You can’t call this person"
            body.contains("livekit_not_configured") -> "Calls aren’t available right now"
            body.contains("no_callees") -> "There’s no one to call in this chat"
            body.contains("too_many_callees") -> "This group is too big for a call"
            http?.code() == 429 -> "Too many tries. Please wait a moment"
            http != null && http.code() >= 500 -> "Server is waking up. Try again in a few seconds"
            else -> "Couldn’t start the call. Try again"
        }
    }

    fun message(t: Throwable, fallback: String = "Something went wrong"): String {
        val body = (t as? HttpException)?.response()?.errorBody()?.string().orEmpty()
        val hay = "$body ${t.message.orEmpty()}"
        return when {
            hay.contains("unknown_blocked") ->
                "This person only accepts new chats from people they already talk to"
            hay.contains("links_privacy") ->
                "This person doesn’t allow new chats from their QR link"
            hay.contains("blocked") ->
                "You can’t message this person"
            hay.contains("cannot_chat_self") ->
                "That’s your own account"
            hay.contains("user_not_found") || hay.contains("not_found") ->
                "User not found"
            else -> t.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }
}
