package com.aivora.app.ui.perf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Figma Voice bubble waveform (`284:6486`) — 4dp bars, 3dp gap, played = Primary.
 * Cheap draw; safe during RecyclerView fling.
 */
class VoiceWaveformView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    /** 0f..1f fraction played. */
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val bar = RectF()
    private val density = resources.displayMetrics.density
    private val barW = 4f * density
    private val gap = 3f * density

    /** Normalized heights from Figma Voice bubble bars (max 34). */
    private val heights = floatArrayOf(
        8f, 14f, 22f, 12f, 28f, 18f, 32f, 24f, 16f, 26f, 34f, 20f, 12f, 18f, 30f, 22f,
        14f, 26f, 18f, 10f, 20f, 28f, 16f, 12f, 22f, 14f, 8f, 18f, 26f, 12f, 20f, 10f,
    ).map { it / 34f }.toFloatArray()

    override fun onDraw(canvas: Canvas) {
        val h = height.toFloat()
        val maxBar = h * 0.95f
        var x = 0f
        val n = heights.size
        for (i in 0 until n) {
            if (x + barW > width) break
            val bh = (heights[i] * maxBar).coerceAtLeast(2f * density)
            val top = (h - bh) / 2f
            bar.set(x, top, x + barW, top + bh)
            val played = (i + 0.5f) / n <= progress
            paint.color = if (played) NativeColors.Primary else NativeColors.WaveUnplayed
            // Figma bars use rounded-[2px] on 4dp width.
            val rr = 2f * density
            canvas.drawRoundRect(bar, rr, rr, paint)
            x += barW + gap
        }
    }
}
