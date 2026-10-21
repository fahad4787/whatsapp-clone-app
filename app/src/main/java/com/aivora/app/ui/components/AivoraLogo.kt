package com.aivora.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aivora.app.R
import com.aivora.app.ui.motion.rememberReducedMotion
import com.aivora.app.ui.theme.AivoraColors
import kotlin.math.sin

/**
 * Animated Node mark — soft pulse, typing-dot cadence, light sweep.
 * Falls back to static vector when reduced motion is on.
 */
@Composable
fun AivoraLogo(
    size: Dp = 88.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val reduced = rememberReducedMotion()
    val live = animated && !reduced
    val transition = rememberInfiniteTransition(label = "aivoraLogo")

    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (live) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val sweep by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = if (live) 1.35f else -0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "sweep",
    )
    val typing by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (live) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "typing",
    )

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.aivora_mark),
            contentDescription = "Aivora",
            modifier = Modifier.size(size),
        )
        if (live) {
            Canvas(modifier = Modifier.size(size)) {
                val s = this.size.minDimension
                val unit = s / 48f

                val sweepX = sweep * s
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                        startX = sweepX - 14f * unit,
                        endX = sweepX + 14f * unit,
                    ),
                    topLeft = Offset(0f, 1.5f * unit),
                    size = Size(s, 45f * unit),
                )

                val dots = listOf(
                    Offset(13.2f * unit, 24.5f * unit),
                    Offset(16.5f * unit, 24.5f * unit),
                    Offset(19.8f * unit, 24.5f * unit),
                )
                dots.forEachIndexed { i, c ->
                    val phase = ((typing * 3f) - i).coerceIn(0f, 1f)
                    val bounce = sin(phase * Math.PI).toFloat()
                    val r = (1.35f + bounce * 0.45f) * unit
                    drawCircle(
                        color = Color(0xFF0B7A42).copy(alpha = 0.35f + bounce * 0.65f),
                        radius = r,
                        center = c.copy(y = c.y - bounce * 1.2f * unit),
                    )
                }

                drawRoundRect(
                    color = AivoraColors.Primary.copy(alpha = 0.18f + (pulse - 1f) * 2f),
                    topLeft = Offset(0.5f * unit, 0.5f * unit),
                    size = Size(47f * unit, 47f * unit),
                    cornerRadius = CornerRadius(14f * unit, 14f * unit),
                    style = Stroke(width = 1.5f * unit),
                )
            }
        }
    }
}

@Composable
fun StatusRingAvatar(
    name: String,
    hasUnseen: Boolean,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
) {
    val reduced = rememberReducedMotion()
    val transition = rememberInfiniteTransition(label = "statusRing")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (hasUnseen && !reduced) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (hasUnseen) 8000 else 1, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = size.toPx() * 0.06f
            val pad = stroke
            drawArc(
                brush = Brush.sweepGradient(
                    colors = if (hasUnseen) {
                        listOf(
                            AivoraColors.Primary,
                            Color(0xFF1ECF75),
                            AivoraColors.BlueLight,
                            AivoraColors.Primary,
                        )
                    } else {
                        listOf(
                            Color.Gray.copy(alpha = 0.35f),
                            Color.Gray.copy(alpha = 0.35f),
                        )
                    },
                ),
                startAngle = spin - 90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = Size(this.size.minDimension - pad * 2, this.size.minDimension - pad * 2),
                style = Stroke(width = stroke),
            )
        }
        Avatar(name = name, size = (size.value * 0.78f).toInt())
    }
}
