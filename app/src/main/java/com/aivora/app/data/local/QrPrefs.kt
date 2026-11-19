package com.aivora.app.data.local

import android.content.Context

/** Show name/photo on My QR page (Wave 38). */
object QrPrefs {
    private const val PREFS = "aivora_qr_prefs"
    private const val KEY_SHOW = "show_name_photo"

    fun showNamePhoto(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW, true)

    fun setShowNamePhoto(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW, value)
            .apply()
    }
}
