package com.aivora.app.ui.feature.calls

import android.content.Context
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallLog
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.ui.icons.AppIcons
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.SystemBars
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal fun isCallRoute(route: String?): Boolean =
    route == Routes.AudioCall || route == Routes.VideoCall

internal fun callEndedLabel(reason: String): String = when (reason) {
    "rejected" -> "Call declined"
    "timeout", "missed" -> "No answer"
    "cancelled" -> "Call cancelled"
    else -> "Call ended"
}

internal fun formatElapsed(sec: Long): String = "%02d:%02d".format(sec / 60, sec % 60)

/** Seconds since the call connected, ticking; null while it is still ringing or connecting. */
@Composable
internal fun rememberCallElapsedSeconds(): Long? {
    val liveSince by ActiveCallSession.liveSince.collectAsState()
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(liveSince) {
        while (liveSince != null) {
            now = SystemClock.elapsedRealtime()
            delay(500)
        }
    }
    return liveSince?.let { ((now - it) / 1000).coerceAtLeast(0) }
}

/** True (with a hint) when a call is already running, so a second one isn't started over it. */
fun blockSecondCall(context: Context): Boolean {
    if (ActiveCallSession.session.value == null) return false
    Toast.makeText(
        context,
        "You're already on a call. Tap the green bar to return.",
        Toast.LENGTH_SHORT,
    ).show()
    return true
}

/**
 * Keeps a call running outside the call screen: shows the "return to call" bar above the app,
 * and reacts when the call connects or ends while that screen is hidden.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveCallHost(
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by ActiveCallSession.session.collectAsState()
    val endedReason by ActiveCallSession.endedReason.collectAsState()
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val onCallScreen = isCallRoute(route)
    val showBar = session != null && !onCallScreen && route != Routes.Login

    LaunchedEffect(onCallScreen, session?.callId) {
        if (session != null) LiveKitCallController.setMinimised(!onCallScreen)
    }

    // The call screen announces the end itself; this covers a call that ends while hidden.
    LaunchedEffect(endedReason, onCallScreen) {
        val reason = endedReason ?: return@LaunchedEffect
        if (onCallScreen) return@LaunchedEffect
        LiveKitCallController.disconnect()
        Toast.makeText(context, callEndedLabel(reason), Toast.LENGTH_SHORT).show()
        ActiveCallSession.consumeEndedReason()
    }

    // Signing out ends whatever call is still running.
    LaunchedEffect(route) {
        val callId = ActiveCallSession.session.value?.callId
        if (route != Routes.Login || callId == null) return@LaunchedEffect
        LiveKitCallController.disconnect()
        ActiveCallSession.clear()
        scope.launch {
            runCatching { NetworkModule.api().postCallsIdAction(callId, "end") }
                .onFailure { CallLog.w("end on sign out failed callId=$callId", it) }
        }
    }

    SideEffect { SystemBars.returnBarShown = showBar }

    Column(Modifier.fillMaxSize()) {
        val active = session
        if (showBar && active != null) {
            ReturnToCallBar(
                video = active.media == "video",
                onClick = {
                    val target = if (active.media == "video") {
                        Routes.videoCall(active.peerName)
                    } else {
                        Routes.audioCall(active.peerName)
                    }
                    navController.navigate(target) { launchSingleTop = true }
                },
            )
        }
        // The bar already covers the status bar, so screens below must not pad for it again.
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(
                    if (showBar) Modifier.consumeWindowInsets(WindowInsets.statusBars) else Modifier,
                ),
        ) {
            content()
        }
    }
}

@Composable
private fun ReturnToCallBar(video: Boolean, onClick: () -> Unit) {
    val elapsed = rememberCallElapsedSeconds()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AivoraColors.Primary)
            .clickable(onClick = onClick)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (video) AppIcons.Videocam else AppIcons.Call,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            if (elapsed == null) "Tap to return to call"
            else "Tap to return to call · ${formatElapsed(elapsed)}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
