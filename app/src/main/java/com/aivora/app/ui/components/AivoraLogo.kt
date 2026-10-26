package com.aivora.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.motion.rememberReducedMotion
import com.aivora.app.ui.theme.AivoraColors

/** Brand mark drawn in Compose — crisp at any size (no broken vector paths). */
@Composable
fun AivoraLogo(
    size: Dp = 88.dp,
    animated: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reduced = rememberReducedMotion()
    val live = animated && !reduced
    val pulse = if (live) {
        val transition = rememberInfiniteTransition(label = "aivoraLogo")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        ).value
    } else {
        1f
    }

    Canvas(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
    ) {
        val w = this.size.width
        val h = this.size.height
        val corner = w * 0.28f

        // Green squircle
        drawRoundRect(
            color = AivoraColors.Primary,
            cornerRadius = CornerRadius(corner, corner),
            size = Size(w, h),
        )

        // White chat bubble
        val bubbleLeft = w * 0.20f
        val bubbleTop = h * 0.26f
        val bubbleW = w * 0.52f
        val bubbleH = h * 0.36f
        val bubbleR = bubbleH * 0.38f
        val bubble = Path().apply {
            addRoundRect(
                RoundRect(
                    left = bubbleLeft,
                    top = bubbleTop,
                    right = bubbleLeft + bubbleW,
                    bottom = bubbleTop + bubbleH,
                    cornerRadius = CornerRadius(bubbleR, bubbleR),
                ),
            )
            // Tail
            moveTo(bubbleLeft + bubbleW * 0.18f, bubbleTop + bubbleH)
            lineTo(bubbleLeft + bubbleW * 0.08f, bubbleTop + bubbleH + h * 0.12f)
            lineTo(bubbleLeft + bubbleW * 0.38f, bubbleTop + bubbleH)
            close()
        }
        drawPath(bubble, color = Color.White)

        // Typing dots
        val cy = bubbleTop + bubbleH * 0.52f
        val dotR = w * 0.035f
        val gap = bubbleW * 0.22f
        val startX = bubbleLeft + bubbleW * 0.28f
        repeat(3) { i ->
            drawCircle(
                color = AivoraColors.Primary,
                radius = dotR,
                center = Offset(startX + gap * i, cy),
            )
        }
    }
}

/** Status ring — segmented when [segmentCount] > 1 (WhatsApp-style). */
@Composable
fun StatusRingAvatar(
    name: String,
    hasUnseen: Boolean,
    size: Dp = 52.dp,
    segmentCount: Int = 1,
    modifier: Modifier = Modifier,
) {
    val ring = if (hasUnseen) AivoraColors.Primary else Color.Gray.copy(alpha = 0.35f)
    val stroke = (size.value * 0.06f).dp
    val segments = segmentCount.coerceAtLeast(1)
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (segments <= 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(width = stroke, color = ring, shape = CircleShape)
                    .padding(stroke),
                contentAlignment = Alignment.Center,
            ) {
                Avatar(name = name, size = (size.value * 0.78f).toInt())
            }
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokePx = stroke.toPx()
                val gapDeg = if (segments > 8) 4f else 8f
                val sweep = (360f / segments) - gapDeg
                var start = -90f + gapDeg / 2f
                repeat(segments) {
                    drawArc(
                        color = ring,
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    )
                    start += sweep + gapDeg
                }
            }
            Avatar(name = name, size = (size.value * 0.72f).toInt())
        }
    }
}
