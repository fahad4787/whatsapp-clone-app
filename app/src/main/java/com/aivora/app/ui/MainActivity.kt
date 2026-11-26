package com.aivora.app.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.aivora.app.data.SessionRestorer
import com.aivora.app.data.StartupWarm
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.push.MessageNotifier
import com.aivora.app.data.push.PushRegistrar
import com.aivora.app.data.realtime.PresenceLifecycle
import com.aivora.app.data.realtime.RealtimeClient
import com.aivora.app.ui.feature.calls.ActiveCallHost
import com.aivora.app.ui.feature.calls.IncomingCallOverlay
import com.aivora.app.ui.feature.calls.isCallRoute
import com.aivora.app.ui.motion.MotionPrefs
import com.aivora.app.ui.navigation.AivoraNavHost
import com.aivora.app.ui.navigation.DeepLinks
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.security.AppLockHost
import com.aivora.app.ui.theme.AivoraTheme
import com.aivora.app.ui.theme.ApplySystemBars
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
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
        com.aivora.app.data.local.InMemoryMeStore.attach(this)
        // Ensure warm kick even if Application path already ran (idempotent).
        StartupWarm.kick(applicationContext)
        pendingDeepLink.value = DeepLinks.routeFrom(intent)
        handleCallIntent(intent)

        // Like a phone call: a ringing or ongoing call shows over the lock screen and turns
        // the screen on. Off again as soon as the call is over, so chats stay behind the lock.
        if (Build.VERSION.SDK_INT >= 27) {
            lifecycleScope.launch {
                combine(ActiveCallSession.incoming, ActiveCallSession.session) { ringing, call ->
                    ringing != null || call != null
                }.distinctUntilChanged().collect { onCall ->
                    setShowWhenLocked(onCall)
                    setTurnScreenOn(onCall)
                }
            }
        }

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
                PresenceLifecycle.allowConnect(applicationContext)
                PushRegistrar.refresh(applicationContext)
                runCatching {
                    val api = com.aivora.app.data.api.NetworkModule.api()
                    val prefs = api.getMeAutoDownload()
                    com.aivora.app.data.local.AutoDownloadPrefs.apply(applicationContext, prefs)
                }
            }
        }
        setContent {
            val start = remember {
                when {
                    !SessionStore.hasSession(this@MainActivity) -> Routes.Login
                    SessionStore.needsProfile(this@MainActivity) -> Routes.ProfileSetup
                    else -> Routes.Home
                }
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
        handleCallIntent(intent)
    }

    /** Answer tapped on the incoming-call notification: the call screen picks it up. */
    private fun handleCallIntent(intent: Intent?) {
        val callId = intent?.getStringExtra(MessageNotifier.EXTRA_ANSWER_CALL_ID) ?: return
        intent.removeExtra(MessageNotifier.EXTRA_ANSWER_CALL_ID)
        ActiveCallSession.requestAnswer(callId)
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
        ApplySystemBars()
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val context = LocalContext.current
            // Without this the user stays on a screen where every request fails.
            LaunchedEffect(SessionStore.expired) {
                if (!SessionStore.expired) return@LaunchedEffect
                RealtimeClient.stop()
                if (navController.currentDestination?.route != Routes.Login) {
                    navController.navigate(Routes.Login) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                    Toast.makeText(context, "You were signed out. Please sign in again.", Toast.LENGTH_LONG)
                        .show()
                }
                SessionStore.acknowledgeExpired()
            }
            LaunchedEffect(deepLinkRoute, startDestination) {
                val route = deepLinkRoute ?: return@LaunchedEffect
                if (startDestination != Routes.Home) return@LaunchedEffect
                navController.navigate(route) {
                    launchSingleTop = true
                }
                onDeepLinkConsumed()
            }
            AppLockHost {
                Box(Modifier.fillMaxSize()) {
                    ActiveCallHost(navController) {
                        AivoraNavHost(
                            navController = navController,
                            startDestination = startDestination,
                        )
                    }
                    IncomingCallOverlay(
                        onNavigate = { route ->
                            // Answering from another call replaces that call's screen.
                            if (isCallRoute(navController.currentDestination?.route)) {
                                navController.popBackStack()
                            }
                            navController.navigate(route)
                        },
                    )
                    com.aivora.app.ui.feature.chat.MuteDurationHost()
            com.aivora.app.ui.feature.chat.ConfirmActionHost()
                }
            }
        }
    }
}
