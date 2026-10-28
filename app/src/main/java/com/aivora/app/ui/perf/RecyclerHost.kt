package com.aivora.app.ui.perf

import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Hosts a [RecyclerView] inside Compose — WhatsApp-class fling on low-end (F13).
 * Callers own the adapter; this only configures the scroll surface.
 *
 * Chat: [reverseLayout]=true, [stackFromEnd]=false, newest-first list → latest at bottom,
 * short threads sit above the composer (WA). Do **not** set both reverse+stackFromEnd —
 * that stacks short chats under the header.
 */
@Composable
fun RecyclerHost(
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    stackFromEnd: Boolean = false,
    hasFixedSize: Boolean = true,
    onRecyclerReady: (RecyclerView) -> Unit = {},
    update: (RecyclerView) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ClippingRecyclerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, reverseLayout).also {
                    it.stackFromEnd = stackFromEnd
                }
                itemAnimator = null
                overScrollMode = View.OVER_SCROLL_NEVER
                setHasFixedSize(hasFixedSize)
                setItemViewCacheSize(12)
                recycledViewPool.setMaxRecycledViews(0, 16)
                recycledViewPool.setMaxRecycledViews(1, 8)
                recycledViewPool.setMaxRecycledViews(2, 8)
                clipToPadding = true
                clipChildren = true
                onRecyclerReady(this)
            }
        },
        update = update,
    )
}

/**
 * ComposeView draws via RenderNode and can ignore a plain canvas clip.
 * Outline + clipToOutline keeps fling rows from painting into the chat header.
 */
private class ClippingRecyclerView(context: Context) : RecyclerView(context) {
    init {
        clipToOutline = true
        outlineProvider = android.view.ViewOutlineProvider.BOUNDS
    }

    override fun dispatchDraw(canvas: Canvas) {
        val checkpoint = canvas.save()
        canvas.clipRect(0, 0, width, height)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(checkpoint)
    }
}
