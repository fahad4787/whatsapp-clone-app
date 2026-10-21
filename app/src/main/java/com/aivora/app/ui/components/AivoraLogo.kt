package com.aivora.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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

/** Soft pulse only — no per-frame canvas overlays (keeps login smooth). */
@Composable
fun AivoraLogo(
    size: Dp = 88.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val reduced = rememberReducedMotion()
    val live = animated && !reduced
    val pulse = if (live) {
        val transition = rememberInfiniteTransition(label = "aivoraLogo")
        transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        ).value
    } else {
        1f
    }

    Image(
        painter = painterResource(R.drawable.aivora_mark),
        contentDescription = "Aivora",
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            },
    )
}

/** Static gradient ring — no infinite spin (list-friendly). */
@Composable
fun StatusRingAvatar(
    name: String,
    hasUnseen: Boolean,
    size: Dp = 52.dp,
    modifier: Modifier = Modifier,
) {
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
                startAngle = -90f,
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
