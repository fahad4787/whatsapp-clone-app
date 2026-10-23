package com.aivora.app.data.local

import android.content.Context

object SessionStore {
    private const val PREFS = "aivora_session"
    private const val KEY_ACCESS = "access"
    private const val KEY_REFRESH = "refresh"
    private const val KEY_PHONE = "phone"
    private const val KEY_USER_ID = "user_id"

    fun save(context: Context, access: String, refresh: String, phone: String?, userId: String? = null) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .putString(KEY_PHONE, phone)
            .apply {
                if (userId != null) putString(KEY_USER_ID, userId)
            }
            .apply()
    }

    fun updateTokens(context: Context, access: String, refresh: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .apply()
    }

    fun saveUserId(context: Context, userId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun accessToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACCESS, null)

    fun refreshToken(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_REFRESH, null)

    fun phone(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PHONE, null)

    fun userId(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_USER_ID, null)

    fun hasSession(context: Context): Boolean =
        !accessToken(context).isNullOrBlank() || !refreshToken(context).isNullOrBlank()

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        ChatListCache.clear(context)
    }
}
