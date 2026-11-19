package com.aivora.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Figma Design tokens (file EWBSJebF5KWRv6BIGzq1VO).
 * Primary green from active chrome / badges; success variable is close cousin.
 */
object AivoraColors {
    /** Set by [AivoraTheme]; neutral tokens below resolve against it (snapshot state → recomposes). */
    var isDark by mutableStateOf(false)

    val Primary = Color(0xFF00BE3C)
    val PrimaryBright = Color(0xFF09C26F)
    val PrimaryDark = Color(0xFF009530)
    val PrimarySoft: Color get() = if (isDark) Color(0xFF12301F) else Color(0xFFEAF9EE)
    val PrimarySoftLight = Color(0xFFEAF9EE)
    val Blue = Color(0xFF2C4A8C)
    val BlueLight = Color(0xFF6B9FE8)
    val Lavender = Color(0xFFAA9EE8)
    val Ink: Color get() = if (isDark) Color(0xFFE9EDEF) else Color(0xFF141414)
    val InkLight = Color(0xFF141414)
    val InkMuted: Color get() = if (isDark) Color(0xFFE9EDEF) else Color(0xFF141525)
    val InkMutedLight = Color(0xFF141525)
    val TextSecondary: Color get() = if (isDark) Color(0xFF8A9AA3) else Color(0xFF727272)
    val TextSecondaryLight = Color(0xFF727272)
    val TextTertiary: Color get() = if (isDark) Color(0xFF6F808A) else Color(0xFF9B9B9B)
    val TextTertiaryLight = Color(0xFF9B9B9B)
    val IconSecondary: Color get() = if (isDark) Color(0xFF7C8C95) else Color(0xFFA1A1A1)
    val IconSecondaryLight = Color(0xFFA1A1A1)
    val Danger = Color(0xFFF44649)
    val Warning = Color(0xFFFFAB00)

    val LightBackground: Color get() = if (isDark) Color(0xFF0B141A) else Color(0xFFFFFFFF)
    val LightBackgroundLight = Color(0xFFFFFFFF)
    val DarkBackground = Color(0xFF0B141A)
    val LightSurface: Color get() = if (isDark) Color(0xFF1A242C) else Color(0xFFFFFFFF)
    val LightSurfaceLight = Color(0xFFFFFFFF)
    val DarkSurface = Color(0xFF1A242C)
    val SurfaceMuted: Color get() = if (isDark) Color(0xFF202C34) else Color(0xFFF8F8F8)
    val SurfaceMutedLight = Color(0xFFF8F8F8)
    val SearchFill: Color get() = if (isDark) Color(0xFF202C34) else Color(0xFFF4F4F4)
    val SearchFillLight = Color(0xFFF4F4F4)
    val Divider: Color get() = if (isDark) Color(0xFF2F3D46) else Color(0xFFEEEEF0)
    val DividerLight = Color(0xFFEEEEF0)
    val BorderMuted: Color get() = if (isDark) Color(0xFF2A3840) else Color(0xFFE8ECE9)
    val BorderMutedLight = Color(0xFFE8ECE9)
    val BorderLight: Color get() = if (isDark) Color(0xFF222F37) else Color(0xFFF5F5F5)
    val BorderLightLight = Color(0xFFF5F5F5)
    val SoftMint: Color get() = if (isDark) Color(0xFF12261C) else Color(0xFFEEF9F1)
    val SoftMintLight = Color(0xFFEEF9F1)
    val InfoGreen = Color(0xFF078649)
    val ChatWallpaperLight = Color(0xFFF8FAF8)
    val ChatWallpaperDark = Color(0xFF0F1C18)
    val ChatDivider: Color get() = if (isDark) Color(0xFF2A3840) else Color(0xFFE8EDE9)
    val ChatDividerLight = Color(0xFFE8EDE9)
    val ComposerFill: Color get() = if (isDark) Color(0xFF1F2C34) else Color(0xFFF6F7F6)
    val ComposerFillLight = Color(0xFFF6F7F6)
    /** Outgoing bubble fill from Figma (soft mint, dark ink text). */
    val BubbleMine: Color get() = if (isDark) Color(0xFF144D37) else Color(0xFFE7F8ED)
    val BubbleMineLight = Color(0xFFE7F8ED)
    val BubbleOtherLight = Color(0xFFFFFFFF)
    val BubbleOtherDark = Color(0xFF1F2C34)
    val Online = Color(0xFF07833A)
    val OnPrimary = Color(0xFFFFFFFF)
}
