package com.aivora.app.data.local

import android.content.Context
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** Local prefs for Chat backup screen (Wave 29). Cloud Drive sync is not implemented. */
object BackupPrefs {
    private const val PREFS = "aivora_backup"
    private const val KEY_LAST_AT = "last_at_ms"
    private const val KEY_LAST_SIZE = "last_size_bytes"
    private const val KEY_INCLUDE_VIDEOS = "include_videos"
    private const val KEY_WIFI_ONLY = "wifi_only"
    private const val KEY_FREQUENCY = "frequency"

    const val FREQ_OFF = "Off"
    const val FREQ_DAILY = "Daily"
    const val FREQ_WEEKLY = "Weekly"
    const val FREQ_MONTHLY = "Monthly"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun lastAtMs(context: Context): Long =
        prefs(context).getLong(KEY_LAST_AT, 0L)

    fun lastSizeBytes(context: Context): Long =
        prefs(context).getLong(KEY_LAST_SIZE, 0L)

    fun includeVideos(context: Context): Boolean =
        prefs(context).getBoolean(KEY_INCLUDE_VIDEOS, false)

    fun wifiOnly(context: Context): Boolean =
        prefs(context).getBoolean(KEY_WIFI_ONLY, true)

    fun frequency(context: Context): String =
        prefs(context).getString(KEY_FREQUENCY, FREQ_OFF) ?: FREQ_OFF

    fun setIncludeVideos(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_INCLUDE_VIDEOS, value).apply()
    }

    fun setWifiOnly(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_WIFI_ONLY, value).apply()
        com.aivora.app.data.backup.ChatBackupScheduler.apply(context)
    }

    fun setFrequency(context: Context, value: String) {
        prefs(context).edit().putString(KEY_FREQUENCY, value).apply()
        com.aivora.app.data.backup.ChatBackupScheduler.apply(context)
    }

    fun recordSuccess(context: Context, sizeBytes: Long) {
        prefs(context).edit()
            .putLong(KEY_LAST_AT, System.currentTimeMillis())
            .putLong(KEY_LAST_SIZE, sizeBytes)
            .apply()
    }

    fun formatLastBackup(context: Context): String {
        val at = lastAtMs(context)
        if (at <= 0L) return "Never"
        return DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT,
            Locale.getDefault(),
        ).format(Date(at))
    }

    fun formatSize(context: Context): String {
        val bytes = lastSizeBytes(context)
        if (bytes <= 0L) return "—"
        if (bytes < 1024) return "$bytes B"
        if (bytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        if (bytes < 1024L * 1024 * 1024) {
            return String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
        }
        return String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024 * 1024))
    }
}
