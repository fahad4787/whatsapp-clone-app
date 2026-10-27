package com.aivora.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.aivora.app.data.SessionRestorer
import com.aivora.app.data.StartupWarm
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.push.MessageNotifier
import com.aivora.app.data.push.PushRegistrar
import com.aivora.app.data.realtime.PresenceLifecycle
import com.aivora.app.ui.feature.calls.IncomingCallOverlay
import com.aivora.app.ui.motion.MotionPrefs
import com.aivora.app.ui.navigation.AivoraNavHost
import com.aivora.app.ui.navigation.DeepLinks
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraTheme
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user may deny; badge still works */ }

    /** Pending deep-link route consumed by [AivoraAppRoot]. */
    private val pendingDeepLink = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Keep system splash until chat list hydrate finishes (or no session).
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition {
            SessionStore.hasSession(this) && !StartupWarm.chatListReady
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MotionPrefs.refresh(this)
        // Ensure warm kick even if Application path already ran (idempotent).
        StartupWarm.kick(applicationContext)
        pendingDeepLink.value = DeepLinks.routeFrom(intent)

        // After first frame: allow WS, then settle before FCM/permission.
        lifecycleScope.launch {
            awaitFrame()
            PresenceLifecycle.allowConnect(applicationContext)
            delay(250)
            MessageNotifier.ensureChannel(this@MainActivity)
            if (Build.VERSION.SDK_INT >= 33) {
                requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (SessionStore.hasSession(this@MainActivity)) {
                SessionRestorer.ensureFresh(applicationContext)
                // After token refresh, ensure WS uses the fresh access token (idempotent).
                com.aivora.app.data.realtime.RealtimeClient.startFromSession(applicationContext)
                PushRegistrar.refresh(applicationContext)
            }
        }
        setContent {
            val start = remember {
                if (SessionStore.hasSession(this@MainActivity)) Routes.Home else Routes.Login
            }
            val deepRoute by pendingDeepLink
            AivoraAppRoot(
                startDestination = start,
                deepLinkRoute = deepRoute,
                onDeepLinkConsumed = { pendingDeepLink.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLink.value = DeepLinks.routeFrom(intent)
    }

    override fun onResume() {
        super.onResume()
        MotionPrefs.refresh(this)
    }
}

@Composable
fun AivoraAppRoot(
    startDestination: String = Routes.Login,
    deepLinkRoute: String? = null,
    onDeepLinkConsumed: () -> Unit = {},
) {
    AivoraTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            LaunchedEffect(deepLinkRoute, startDestination) {
                val route = deepLinkRoute ?: return@LaunchedEffect
                if (startDestination == Routes.Login) return@LaunchedEffect
                navController.navigate(route) {
                    launchSingleTop = true
                }
                onDeepLinkConsumed()
            }
            Box(Modifier.fillMaxSize()) {
                AivoraNavHost(
                    navController = navController,
                    startDestination = startDestination,
                )
                IncomingCallOverlay(
                    onNavigate = { route -> navController.navigate(route) },
                )
            }
        }
    }
}
