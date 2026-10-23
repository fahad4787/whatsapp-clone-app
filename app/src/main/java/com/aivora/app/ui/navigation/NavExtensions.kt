package com.aivora.app.ui.navigation

import androidx.navigation.NavHostController

/** Detail routes skip launchSingleTop so reopen still runs enter transition. */
fun isDetailRoute(route: String?): Boolean {
    if (route.isNullOrBlank()) return false
    val base = route.substringBefore('?')
    return when {
        base == Routes.Home || base == Routes.Login || base == Routes.ProfileSetup -> false
        else -> true
    }
}

/**
 * Navigate forward. Detail destinations skip launchSingleTop so enter/exit
 * animations always run (reopening the same chat still fades in).
 */
fun NavHostController.navigateSingleTop(route: String) {
    val base = route.substringBefore('?')
    navigate(route) {
        launchSingleTop = !isDetailRoute(base)
    }
}

/**
 * Navigate and clear the back stack up to [popUpToRoute] (inclusive optional).
 * Used for auth → home so Back does not return to login/OTP.
 */
fun NavHostController.navigateClearing(
    route: String,
    popUpToRoute: String,
    inclusive: Boolean = true,
) {
    navigate(route) {
        popUpTo(popUpToRoute) { this.inclusive = inclusive }
        launchSingleTop = true
    }
}

fun NavHostController.safePopBackStack(): Boolean =
    if (previousBackStackEntry != null) popBackStack() else false
