package com.aivora.app.ui.navigation

import androidx.navigation.NavHostController

/** Navigate without stacking duplicate destinations. */
fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
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
