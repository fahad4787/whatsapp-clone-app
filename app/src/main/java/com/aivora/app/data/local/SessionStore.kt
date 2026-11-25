package com.aivora.app.data.local

import android.app.NotificationManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.data.realtime.RealtimeClient

object SessionStore {
    private const val PREFS = "aivora_session"
    private const val KEY_ACCESS = "access"
    private const val KEY_REFRESH = "refresh"
    private const val KEY_PHONE = "phone"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NEEDS_PROFILE = "needs_profile"

    /** True after the server ended the session; the UI then returns to sign-in. */
    var expired by mutableStateOf(false)
        private set

    fun save(context: Context, access: String, refresh: String, phone: String?, userId: String? = null) {
        expired = false
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

    /** True from a first sign-in until the name is saved, so a relaunch returns to that step. */
    fun needsProfile(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_NEEDS_PROFILE, false)

    fun setNeedsProfile(context: Context, needed: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_NEEDS_PROFILE, needed)
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

    /** The server rejected this session (not a user log out). */
    fun expire(context: Context) {
        clear(context)
        expired = true
    }

    fun acknowledgeExpired() {
        expired = false
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
        // Stop what is still running for this account, so it can't refill what is wiped below.
        RealtimeClient.signOut()
        ActiveCallSession.clearIncoming()
        ActiveCallSession.clear()
        Handler(Looper.getMainLooper()).post { LiveKitCallController.disconnect() }
        context.getSystemService(NotificationManager::class.java)?.cancelAll()
        ChatListCache.clear(context)
        MessageCache.clear(context)
        InMemoryStatusStore.clear()
        StatusCache.clear(context)
        ChatListsStore.clear()
        InMemoryCallHistoryStore.clear()
        InMemoryMeStore.attach(context) // so the profile cache on disk is wiped too
        clearInMemoryListCaches()
    }
}
