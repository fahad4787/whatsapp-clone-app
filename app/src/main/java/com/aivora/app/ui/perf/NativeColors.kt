package com.aivora.app.ui.perf

import android.graphics.Color

/** Android color ints for View bind — no Compose MaterialTheme lookups during fling. */
object NativeColors {
    const val Primary = 0xFF00BE3C.toInt()
    /** Figma Voice duration label (`284:6521`). */
    const val VoiceDuration = 0xFF07833A.toInt()
    /** Figma unplayed waveform bars (`284:6498+`). */
    const val WaveUnplayed = 0xFFA6CBB2.toInt()
    const val Ink = 0xFF141414.toInt()
    const val TextSecondary = 0xFF727272.toInt()
    const val Danger = 0xFFF44649.toInt()
    const val BubbleMine = 0xFFE7F8ED.toInt()
    const val BubbleOtherLight = 0xFFFFFFFF.toInt()
    const val BubbleOtherDark = 0xFF1F2C34.toInt()
    const val ChatDivider = 0xFFE8EDE9.toInt()
    const val TickRead = 0xFF53BDEB.toInt()
    const val SurfaceMuted = 0xFFF8F8F8.toInt()
    const val White = Color.WHITE
    const val Transparent = Color.TRANSPARENT
}
