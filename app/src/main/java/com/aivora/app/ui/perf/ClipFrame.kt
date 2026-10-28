package com.aivora.app.ui.perf

import android.content.Context
import android.graphics.Canvas
import android.view.ViewOutlineProvider
import android.widget.FrameLayout

/**
 * Host for RecyclerView [androidx.compose.ui.platform.ComposeView] rows.
 * Nested ComposeView (inside AndroidView) can paint at window y=0 when the row is
 * only partially on-screen — skip draw for tiny clipped rows and hard-clip otherwise.
 */
class ClipFrame(context: Context) : FrameLayout(context) {
    init {
        clipChildren = true
        clipToPadding = true
        clipToOutline = true
        outlineProvider = ViewOutlineProvider.BOUNDS
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun dispatchDraw(canvas: Canvas) {
        // Partially scrolled nested ComposeView draws at screen origin — blank it.
        if (height < (48 * resources.displayMetrics.density).toInt()) return
        val checkpoint = canvas.save()
        canvas.clipRect(0, 0, width, height)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(checkpoint)
    }
}
