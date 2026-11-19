package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local mirror of Advanced → Protect IP in calls (Wave 45).
 * Read by [com.aivora.app.data.call.LiveKitCallController] at connect time.
 */
object CallPrivacyPrefs {
    private const val PREFS = "aivora_call_privacy"
    private const val KEY_PROTECT_IP = "protect_ip_calls"

    private val _protectIp = MutableStateFlow(true)
    val protectIpFlow: StateFlow<Boolean> = _protectIp.asStateFlow()

    @Volatile
    private var cachedProtectIp = true

    fun hydrate(context: Context) {
        cachedProtectIp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_PROTECT_IP, true)
        _protectIp.value = cachedProtectIp
    }

    fun protectIpCalls(): Boolean = cachedProtectIp

    fun setProtectIpCalls(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_PROTECT_IP, enabled)
            .apply()
        cachedProtectIp = enabled
        _protectIp.value = enabled
    }
}
