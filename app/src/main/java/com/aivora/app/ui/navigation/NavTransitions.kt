package com.aivora.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.aivora.app.ui.motion.AivoraEmphasizedAccel
import com.aivora.app.ui.motion.AivoraEmphasizedDecel
import com.aivora.app.ui.motion.MotionPrefs

/**
 * Lightweight fade-only navigation — cheaper on low-RAM devices than slide+fade.
 * Duration follows system animator scale (0 = instant).
 */
val NavEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val ms = MotionPrefs.scaledNavFadeMs()
    if (ms == 0) EnterTransition.None else fadeIn(tween(ms, easing = AivoraEmphasizedDecel))
}

val NavExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val ms = MotionPrefs.scaledNavFadeMs()
    if (ms == 0) ExitTransition.None else fadeOut(tween((ms - 20).coerceAtLeast(0), easing = AivoraEmphasizedAccel))
}

val NavPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    val ms = MotionPrefs.scaledNavFadeMs()
    if (ms == 0) EnterTransition.None else fadeIn(tween(ms, easing = AivoraEmphasizedDecel))
}

val NavPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    val ms = MotionPrefs.scaledNavFadeMs()
    if (ms == 0) ExitTransition.None else fadeOut(tween((ms - 20).coerceAtLeast(0), easing = AivoraEmphasizedAccel))
}
