package com.aivora.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

private const val FadeMs = 200
private const val SlideMs = 340

private val SlideSpec = tween<IntOffset>(SlideMs, easing = FastOutSlowInEasing)
private val FadeSpec = tween<Float>(FadeMs, easing = FastOutSlowInEasing)

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
        // New screen enters from the right.
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = SlideSpec,
        )
    } else {
        fadeIn(animationSpec = FadeSpec)
    }
}

val NavExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isSlideRoute(targetState.destination.route)) {
        // Previous screen eases slightly left so the slide is obvious.
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = SlideSpec,
            targetOffset = { full -> full / 4 },
        )
    } else {
        fadeOut(animationSpec = FadeSpec)
    }
}

val NavPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (isSlideRoute(initialState.destination.route)) {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = SlideSpec,
            initialOffset = { full -> full / 4 },
        )
    } else {
        fadeIn(animationSpec = FadeSpec)
    }
}

val NavPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (isSlideRoute(initialState.destination.route)) {
        // Leaving screen exits to the right.
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            animationSpec = SlideSpec,
        )
    } else {
        fadeOut(animationSpec = FadeSpec)
    }
}
