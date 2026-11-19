package com.aivora.app.data.api

import retrofit2.HttpException

/** Map Retrofit failures to short user-facing copy. */
object ApiErrors {
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
