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
    primaryContainer = AivoraColors.Primary.copy(alpha = 0.14f),
    secondary = AivoraColors.Blue,
    background = AivoraColors.LightBackground,
    surface = AivoraColors.LightSurface,
    surfaceVariant = Color(0xFFE3EDE7),
    onBackground = AivoraColors.Ink,
    onSurface = AivoraColors.Ink,
    onSurfaceVariant = Color(0xFF5B6B63),
    outlineVariant = Color(0xFFD5E0D9),
    error = AivoraColors.Danger,
)

private val DarkScheme = darkColorScheme(
    primary = AivoraColors.PrimaryBright,
    onPrimary = AivoraColors.OnPrimary,
    primaryContainer = AivoraColors.PrimaryDark,
    secondary = AivoraColors.BlueLight,
    background = AivoraColors.DarkBackground,
    surface = AivoraColors.DarkSurface,
    surfaceVariant = Color(0xFF243038),
    onBackground = Color(0xFFE9EDEF),
    onSurface = Color(0xFFE9EDEF),
    onSurfaceVariant = Color(0xFF8A9AA3),
    outlineVariant = Color(0xFF2F3D46),
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
