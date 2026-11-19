package com.aivora.app.data.local

import android.content.Context

/** Per-device preference for security notifications (Wave 32). */
object SecurityNotifPrefs {
    private const val PREFS = "aivora_security_notif"
    private const val KEY_SHOW = "show_security_notifications"

    fun show(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW, true)

    fun setShow(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW, value)
            .apply()
    }
}
