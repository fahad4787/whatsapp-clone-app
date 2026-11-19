package com.aivora.app.ui.perf

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aivora.app.ui.theme.AivoraColors

/** Android color ints for View bind — no Compose MaterialTheme lookups during fling. */
object NativeColors {
    const val Primary = 0xFF00BE3C.toInt()
    /** Figma Voice duration label (`284:6521`). */
    const val VoiceDuration = 0xFF07833A.toInt()
    /** Figma unplayed waveform bars (`284:6498+`). */
    const val WaveUnplayed = 0xFFA6CBB2.toInt()
    val Ink: Int get() = if (AivoraColors.isDark) 0xFFE9EDEF.toInt() else 0xFF141414.toInt()
    val TextSecondary: Int get() = if (AivoraColors.isDark) 0xFF8A9AA3.toInt() else 0xFF727272.toInt()
    const val Danger = 0xFFF44649.toInt()
    val BubbleMine: Int get() = if (AivoraColors.isDark) 0xFF144D37.toInt() else 0xFFE7F8ED.toInt()
    const val BubbleOtherLight = 0xFFFFFFFF.toInt()
    const val BubbleOtherDark = 0xFF1F2C34.toInt()
    val ChatDivider: Int get() = if (AivoraColors.isDark) 0xFF2A3840.toInt() else 0xFFE8EDE9.toInt()
    const val TickRead = 0xFF53BDEB.toInt()
    val SurfaceMuted: Int get() = if (AivoraColors.isDark) 0xFF202C34.toInt() else 0xFFF8F8F8.toInt()
    const val White = Color.WHITE
    const val Transparent = Color.TRANSPARENT
}
