package com.aivora.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightScheme = lightColorScheme(
    primary = AivoraColors.Primary,
    onPrimary = AivoraColors.OnPrimary,
    primaryContainer = AivoraColors.Primary.copy(alpha = 0.12f),
    secondary = AivoraColors.Blue,
    background = AivoraColors.LightBackground,
    surface = AivoraColors.LightSurface,
    surfaceVariant = Color(0xFFE7E9EC),
    onBackground = AivoraColors.Ink,
    onSurface = AivoraColors.Ink,
    onSurfaceVariant = Color(0xFF667781),
    outlineVariant = Color(0xFFD1D7DB),
    error = AivoraColors.Danger,
)

private val DarkScheme = darkColorScheme(
    primary = AivoraColors.Primary,
    onPrimary = AivoraColors.OnPrimary,
    primaryContainer = AivoraColors.PrimaryDark,
    secondary = AivoraColors.BlueLight,
    background = AivoraColors.DarkBackground,
    surface = AivoraColors.DarkSurface,
    surfaceVariant = Color(0xFF2A3942),
    onBackground = Color(0xFFE9EDEF),
    onSurface = Color(0xFFE9EDEF),
    onSurfaceVariant = Color(0xFF8696A0),
    outlineVariant = Color(0xFF3B4A54),
    error = AivoraColors.Danger,
)

@Composable
fun AivoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = AivoraTypography,
        content = content,
    )
}
