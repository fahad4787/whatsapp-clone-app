package com.aivora.app.ui.motion

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

val AivoraSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMediumLow,
)

val AivoraSoftSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMedium,
)

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}

/** Scale down slightly while [interactionSource] reports pressed. */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduced = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) pressedScale else 1f,
        animationSpec = if (reduced) tween(0) else AivoraSoftSpring,
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** One-shot fade + slide when [key] first composes / changes. */
@Composable
fun Modifier.enterFadeSlide(
    key: Any,
    fromY: Float = 16f,
    fromX: Float = 0f,
): Modifier {
    val reduced = rememberReducedMotion()
    val alpha = remember(key) { Animatable(if (reduced) 1f else 0f) }
    val ty = remember(key) { Animatable(if (reduced) 0f else fromY) }
    val tx = remember(key) { Animatable(if (reduced) 0f else fromX) }
    LaunchedEffect(key) {
        if (reduced) {
            alpha.snapTo(1f)
            ty.snapTo(0f)
            tx.snapTo(0f)
            return@LaunchedEffect
        }
        alpha.snapTo(0f)
        ty.snapTo(fromY)
        tx.snapTo(fromX)
        alpha.animateTo(1f, tween(260, easing = FastOutSlowInEasing))
        ty.animateTo(0f, AivoraSoftSpring)
        tx.animateTo(0f, AivoraSoftSpring)
    }
    return graphicsLayer {
        this.alpha = alpha.value
        translationY = ty.value
        translationX = tx.value
    }
}
