package com.aivora.app.data.local

import android.content.Context
import com.aivora.app.data.model.AutoDownloadDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local mirror of `/api/me/auto-download` (Wave 58). */
object AutoDownloadPrefs {
    private const val PREFS = "aivora_auto_download"
    private const val KEY_MOBILE = "mobile"
    private const val KEY_WIFI = "wifi"
    private const val KEY_ROAMING = "roaming"
    private const val KEY_LESS = "less_data_calls"
    private const val KEY_QUALITY = "photo_upload_quality"

    data class Snapshot(
        val mobile: String = "photos",
        val wifi: String = "all",
        val roaming: String = "none",
        val lessDataCalls: Boolean = false,
        val photoUploadQuality: String = "auto",
    )

    private val _snap = MutableStateFlow(Snapshot())
    val flow: StateFlow<Snapshot> = _snap.asStateFlow()

    fun hydrate(context: Context) {
        _snap.value = load(context)
    }

    fun load(context: Context): Snapshot {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return Snapshot(
            mobile = p.getString(KEY_MOBILE, "photos") ?: "photos",
            wifi = p.getString(KEY_WIFI, "all") ?: "all",
            roaming = p.getString(KEY_ROAMING, "none") ?: "none",
            lessDataCalls = p.getBoolean(KEY_LESS, false),
            photoUploadQuality = p.getString(KEY_QUALITY, "auto") ?: "auto",
        )
    }

    fun apply(context: Context, dto: AutoDownloadDto) {
        val next = Snapshot(
            mobile = dto.mobile,
            wifi = dto.wifi,
            roaming = dto.roaming,
            lessDataCalls = dto.lessDataCalls,
            photoUploadQuality = dto.photoUploadQuality,
        )
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_MOBILE, next.mobile)
            .putString(KEY_WIFI, next.wifi)
            .putString(KEY_ROAMING, next.roaming)
            .putBoolean(KEY_LESS, next.lessDataCalls)
            .putString(KEY_QUALITY, next.photoUploadQuality)
            .apply()
        _snap.value = next
    }
}
