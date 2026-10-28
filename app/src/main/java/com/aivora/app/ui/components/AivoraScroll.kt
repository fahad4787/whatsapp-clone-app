package com.aivora.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier

/**
 * F10/F11 — overscroll glow is expensive on Mate-class GPUs during fling.
 * Wrap every primary scroll surface with this.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AivoraNoOverscroll(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
        content()
    }
}

/**
 * Prefer for short lists (≤ ~40 rows). Column + translate beats LazyColumn recycle
 * on low-end when the window is small (same lesson as chat F10).
 */
@Composable
fun AivoraScrollColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    AivoraNoOverscroll {
        Column(
            modifier = modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
