package com.aivora.app.ui.motion

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext

/** Emphasized decelerate — settles quickly without bounce. */
val AivoraEmphasizedDecel = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
val AivoraEmphasizedAccel = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

/** Snappy chrome (buttons, small state). */
val AivoraSnap = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh,
)

val AivoraTweenFast = tween<Float>(durationMillis = 120, easing = FastOutSlowInEasing)
val AivoraTweenMed = tween<Float>(durationMillis = 160, easing = AivoraEmphasizedDecel)

/**
 * Nav timings — short fade only (low-RAM friendly).
 * Keep [NavTransitions] in sync with these.
 */
const val AivoraNavFadeMs = 120
const val AivoraNavSlideMs = 120

/** System animator scale, refreshed from Activity resume. */
object MotionPrefs {
    @Volatile
    var animatorDurationScale: Float = 1f
        private set

    fun refresh(context: android.content.Context) {
        animatorDurationScale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
        }.getOrDefault(1f)
    }

    fun scaledNavFadeMs(): Int =
        (AivoraNavFadeMs * animatorDurationScale).toInt().coerceAtLeast(0)
}

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

/** Light press scale for primary controls only — avoid on long Lazy lists. */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.98f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduced = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) pressedScale else 1f,
        animationSpec = if (reduced) tween(0) else AivoraSnap,
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
