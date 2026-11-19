package com.aivora.app.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** App theme + default chat wallpaper (Wave 35). */
object AppearancePrefs {
    private const val PREFS = "aivora_prefs"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_WALLPAPER = "default_wallpaper"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    private val _theme = MutableStateFlow(THEME_SYSTEM)
    val themeFlow: StateFlow<String> = _theme.asStateFlow()

    private val _wallpaper = MutableStateFlow("default")
    val wallpaperFlow: StateFlow<String> = _wallpaper.asStateFlow()

    fun hydrate(context: Context) {
        _theme.value = theme(context)
        _wallpaper.value = wallpaper(context)
    }

    fun theme(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, THEME_SYSTEM)
            ?.takeIf { it in setOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK) }
            ?: THEME_SYSTEM

    fun setTheme(context: Context, mode: String) {
        val safe = mode.takeIf { it in setOf(THEME_SYSTEM, THEME_LIGHT, THEME_DARK) } ?: THEME_SYSTEM
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, safe)
            .apply()
        _theme.value = safe
    }

    fun wallpaper(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER, "default")
            ?: "default"

    fun setWallpaper(context: Context, key: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_WALLPAPER, key)
            .apply()
        _wallpaper.value = key
    }

    fun reset(context: Context) {
        setTheme(context, THEME_SYSTEM)
        setWallpaper(context, "default")
        FontScaleStore.set(context, FontScaleStore.MEDIUM)
    }

    fun themeLabel(mode: String): String = when (mode) {
        THEME_LIGHT -> "Light"
        THEME_DARK -> "Dark"
        else -> "System"
    }

    fun wallpaperLabel(key: String): String = when (key) {
        "mint" -> "Mint"
        "dusk" -> "Dusk"
        "sand" -> "Sand"
        else -> "Default · Green"
    }

    fun resolveDark(mode: String, systemDark: Boolean): Boolean = when (mode) {
        THEME_LIGHT -> false
        THEME_DARK -> true
        else -> systemDark
    }
}
