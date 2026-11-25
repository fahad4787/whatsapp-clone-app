package com.aivora.app.ui.theme

import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

/**
 * What the system bars should look like right now. Screens only state what they need and
 * [ApplySystemBars] is the one place that touches the window, so nothing has to save and
 * restore colours (which breaks when screens overlap during a transition).
 */
object SystemBars {
    class Request(val lightContent: Boolean, val navigationBar: Boolean)

    internal val requests = mutableStateListOf<Request>()

    /** The green return-to-call bar sits under the status bar, so its icons must be white. */
    var returnBarShown by mutableStateOf(false)
}

private val LightNavScrim = Color.argb(0xE6, 0xFF, 0xFF, 0xFF)
private val DarkNavScrim = Color.argb(0x80, 0x1B, 0x1B, 0x1B)

/** Applies the bar colours: the app theme by default, unless a screen or the call bar overrides. */
@Composable
fun ApplySystemBars() {
    val context = LocalContext.current
    val appDark = AivoraColors.isDark
    val screen = SystemBars.requests.lastOrNull()
    val navScreen = SystemBars.requests.lastOrNull { it.navigationBar }
    val lightStatusContent =
        SystemBars.returnBarShown || (screen?.lightContent ?: appDark)

    DisposableEffect(context, appDark, lightStatusContent, navScreen) {
        val activity = generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<ComponentActivity>()
            .firstOrNull()
            ?: return@DisposableEffect onDispose { }
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) {
                lightStatusContent
            },
            navigationBarStyle = SystemBarStyle.auto(LightNavScrim, DarkNavScrim) { appDark },
        )
        if (navScreen != null) {
            // No grey scrim under a full-screen call, photo or status on 3-button navigation.
            val window = activity.window
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightNavigationBars = !navScreen.lightContent
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        }
        onDispose { }
    }
}
