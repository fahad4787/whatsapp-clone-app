package com.aivora.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

private const val FadeMs = 180
private const val SlideMs = 300

/** Material 3 emphasized — decelerate into place, accelerate out. */
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

private val SlideInSpec = tween<IntOffset>(SlideMs, easing = EmphasizedDecelerate)
private val SlideOutSpec = tween<IntOffset>(SlideMs - 20, easing = EmphasizedAccelerate)
private val FadeSpec = tween<Float>(FadeMs)

/** Push screens that slide horizontally (chat, settings, new-chat, …). */
fun isSlideRoute(route: String?): Boolean {
    if (route.isNullOrBlank()) return false
    val base = route.substringBefore('?')
    return when {
        base == Routes.Home || base == Routes.Login || base == Routes.ProfileSetup -> false
        base.startsWith("chat/") -> true
        base.startsWith("settings/") -> true
        base.startsWith("profile/") -> true
        base.startsWith("status/") -> true
        base.startsWith("camera/") -> true
        base.startsWith("community/") -> true
        base.startsWith("channel/") -> true
        base.startsWith("event-") -> true
        base.startsWith("message-info/") -> true
        base == Routes.NewChat || base == Routes.NewGroup || base == Routes.NewCall -> true
        base == Routes.NewStatus || base == Routes.LockedChats -> true
        base == Routes.ScheduledCalls || base == Routes.Channels || base == Routes.YourDetails -> true
        else -> true
    }
}

val NavEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (isSlideRoute(targetState.destination.route)) {
        // Only the new screen moves — slides in from the right.
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = SlideInSpec,
        )
    } else {
        fadeIn(animationSpec = FadeSpec)
    }
}

val NavExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isSlideRoute(targetState.destination.route)) {
        // Keep home still under the sliding screen (soft fade only).
        fadeOut(animationSpec = tween(SlideMs - 40), targetAlpha = 0.92f)
    } else {
        fadeOut(animationSpec = FadeSpec)
    }
}

val NavPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (isSlideRoute(initialState.destination.route)) {
        fadeIn(animationSpec = tween(SlideMs - 40), initialAlpha = 0.92f)
    } else {
        fadeIn(animationSpec = FadeSpec)
    }
}

val NavPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isSlideRoute(initialState.destination.route)) {
        // Leaving screen exits fully to the right.
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = SlideOutSpec,
        )
    } else {
        fadeOut(animationSpec = FadeSpec)
    }
}
