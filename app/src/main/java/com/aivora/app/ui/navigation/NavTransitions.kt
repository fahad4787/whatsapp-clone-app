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
import com.aivora.app.ui.motion.AivoraNavFadeMs

/**
 * Lightweight fade-only navigation — cheaper on low-RAM devices than slide+fade.
 */
private val FadeInSpec = tween<Float>(AivoraNavFadeMs, easing = AivoraEmphasizedDecel)
private val FadeOutSpec = tween<Float>(AivoraNavFadeMs - 20, easing = AivoraEmphasizedAccel)

val NavEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(FadeInSpec)
}

val NavExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(FadeOutSpec)
}

val NavPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    fadeIn(FadeInSpec)
}

val NavPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    fadeOut(FadeOutSpec)
}
