package com.aivora.app.ui.perf

import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Chat-only host: RecyclerView + opaque top cover in one FrameLayout.
 * ComposeView rows can paint past item bounds; the cover View (same parent, higher Z)
 * hides that bleed under the Compose header — WhatsApp toolbar behavior.
 */
@Composable
fun ChatRecyclerHost(
    modifier: Modifier = Modifier,
    coverColor: Int,
    coverHeightPx: Int,
    onRecyclerReady: (RecyclerView) -> Unit = {},
    update: (RecyclerView) -> Unit,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                clipChildren = true
                clipToPadding = true
                val rv = object : RecyclerView(context) {
                    init {
                        clipToOutline = true
                        outlineProvider = android.view.ViewOutlineProvider.BOUNDS
                    }
                }.apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    // Chronological + stackFromEnd (no reverseLayout): latest at bottom, ComposeView stable.
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false).also {
                        it.stackFromEnd = true
                    }
                    itemAnimator = null
                    overScrollMode = View.OVER_SCROLL_NEVER
                    setHasFixedSize(false)
                    setItemViewCacheSize(12)
                    recycledViewPool.setMaxRecycledViews(0, 16)
                    recycledViewPool.setMaxRecycledViews(1, 8)
                    recycledViewPool.setMaxRecycledViews(2, 8)
                    clipToPadding = true
                    clipChildren = true
                    tag = "chat_rv"
                }
                addView(rv)
                val cover = View(context).apply {
                    tag = "chat_cover"
                    setBackgroundColor(coverColor)
                    elevation = 24f * resources.displayMetrics.density
                    translationZ = 24f * resources.displayMetrics.density
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        coverHeightPx.coerceAtLeast(0),
                    )
                }
                addView(cover)
                onRecyclerReady(rv)
            }
        },
        update = { frame ->
            val rv = frame.findViewWithTag<RecyclerView>("chat_rv") ?: return@AndroidView
            val cover = frame.findViewWithTag<View>("chat_cover")
            if (cover != null) {
                val lp = cover.layoutParams
                if (lp.height != coverHeightPx) {
                    lp.height = coverHeightPx.coerceAtLeast(0)
                    cover.layoutParams = lp
                }
                cover.setBackgroundColor(coverColor)
                cover.bringToFront()
            }
            // Keep RV below the cover geometrically as well as by Z.
            val padTop = coverHeightPx.coerceAtLeast(0)
            if (rv.paddingTop != padTop) {
                rv.setPadding(rv.paddingLeft, padTop, rv.paddingRight, rv.paddingBottom)
            }
            update(rv)
        },
    )
}
