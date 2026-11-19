package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Chat / UI font scale: small=0.9, medium=1.0, large=1.15 */
object FontScaleStore {
    private const val PREFS = "aivora_prefs"
    private const val KEY = "font_scale"
    const val SMALL = 0.9f
    const val MEDIUM = 1.0f
    const val LARGE = 1.15f

    private val _scale = MutableStateFlow(MEDIUM)
    val scaleFlow: StateFlow<Float> = _scale.asStateFlow()

    fun hydrate(context: Context) {
        _scale.value = get(context)
    }

    fun get(context: Context): Float =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getFloat(KEY, MEDIUM)

    fun set(context: Context, scale: Float) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY, scale)
            .apply()
        _scale.value = scale
    }

    fun label(scale: Float): String = when {
        scale <= 0.95f -> "Small"
        scale >= 1.1f -> "Large"
        else -> "Medium"
    }
}
