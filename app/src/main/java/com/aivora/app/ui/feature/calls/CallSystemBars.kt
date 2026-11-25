package com.aivora.app.ui.feature.calls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.aivora.app.ui.theme.SystemBars

/**
 * Keeps status/nav icons readable on full-screen call, photo and status screens: [lightContent]
 * means white icons (dark screen), otherwise dark icons (light screen). With [navigationBar] the
 * nav bar also goes transparent so no grey scrim shows under the screen on 3-button navigation.
 */
@Composable
fun CallSystemBars(navigationBar: Boolean = true, lightContent: Boolean = true) {
    DisposableEffect(navigationBar, lightContent) {
        val request = SystemBars.Request(lightContent, navigationBar)
        SystemBars.requests.add(request)
        onDispose { SystemBars.requests.remove(request) }
    }
}
