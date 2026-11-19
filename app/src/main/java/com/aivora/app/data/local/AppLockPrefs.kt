package com.aivora.app.data.local

import android.content.Context

/** Wave 33 — unlock Aivora with device biometric / screen lock. */
object AppLockPrefs {
    private const val PREFS = "aivora_app_lock"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_NEEDS_UNLOCK = "needs_unlock"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun enabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
        if (!value) setNeedsUnlock(context, false)
    }

    fun needsUnlock(context: Context): Boolean =
        prefs(context).getBoolean(KEY_NEEDS_UNLOCK, false)

    fun setNeedsUnlock(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_NEEDS_UNLOCK, value).apply()
    }
}
