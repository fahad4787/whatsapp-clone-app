package com.aivora.app.ui.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.request.ImageRequest
import coil3.size.Size

/** Decode chat media at display size — avoids full-camera bitmaps while scrolling. */
@Composable
fun rememberBubbleImageModel(
    data: Any?,
    width: Dp = 240.dp,
    height: Dp = 200.dp,
): ImageRequest = rememberSizedImageModel(data, width, height)

/**
 * PERF H5: decode at roughly on-screen pixels (status full-bleed, media grid cells, etc.).
 */
@Composable
fun rememberSizedImageModel(
    data: Any?,
    width: Dp,
    height: Dp,
): ImageRequest {
    val context = LocalContext.current
    val density = LocalDensity.current
    val w = with(density) { width.roundToPx() }.coerceAtLeast(1)
    val h = with(density) { height.roundToPx() }.coerceAtLeast(1)
    return remember(data, w, h) {
        ImageRequest.Builder(context)
            .data(data)
            .size(Size(w, h))
            .build()
    }
}

/** Full-screen status / preview — capped to device viewport. */
@Composable
fun rememberViewportImageModel(data: Any?): ImageRequest {
    val config = LocalConfiguration.current
    return rememberSizedImageModel(
        data = data,
        width = config.screenWidthDp.dp,
        height = config.screenHeightDp.dp,
    )
}
