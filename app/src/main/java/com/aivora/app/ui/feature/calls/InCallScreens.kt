package com.aivora.app.ui.feature.calls

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.ui.components.Avatar
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import livekit.org.webrtc.EglBase

private val CallBg = Color(0xFF0B141A)
private val CallPanel = Color(0xFF1C252C)
private val CallGreen = Color(0xFF25D366)
private val CallRed = Color(0xFFE74C3C)

@Composable
fun AudioCallScreen(
    name: String = "Contact",
    onEnd: () -> Unit = {},
) {
    InCallContent(name = name, videoMode = false, onEnd = onEnd)
}

@Composable
fun VideoCallScreen(
    name: String = "Contact",
    onEnd: () -> Unit = {},
) {
    InCallContent(name = name, videoMode = true, onEnd = onEnd)
}

@Composable
private fun InCallContent(
    name: String,
    videoMode: Boolean,
    onEnd: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by ActiveCallSession.session.collectAsState()
    val answered by ActiveCallSession.answered.collectAsState()
    val endedReason by ActiveCallSession.endedReason.collectAsState()
    val roomConnected by LiveKitCallController.roomConnected.collectAsState()
    val remoteJoined by LiveKitCallController.remoteJoined.collectAsState()
    val muted by LiveKitCallController.muted.collectAsState()
    val speakerOn by LiveKitCallController.speakerOn.collectAsState()
    val cameraOff by LiveKitCallController.cameraOff.collectAsState()
    val remoteVideo by LiveKitCallController.remoteVideo.collectAsState()
    val localVideo by LiveKitCallController.localVideo.collectAsState()
    val peerName = session?.peerName?.ifBlank { name } ?: name
    val enableVideo = videoMode || session?.media == "video"
    val isLive = answered && remoteJoined

    var elapsedSec by remember { mutableLongStateOf(0L) }
    var permissionReady by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }
    var leaveLabel by remember { mutableStateOf<String?>(null) }

    val permissions = remember(enableVideo) {
        buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (enableVideo) add(Manifest.permission.CAMERA)
        }.toTypedArray()
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        permissionReady = grants.values.all { it }
    }

    LaunchedEffect(Unit) {
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) permissionReady = true
        else permissionLauncher.launch(permissions)
    }

    LaunchedEffect(session?.callId, permissionReady) {
        val s = session
        if (!permissionReady || s == null) return@LaunchedEffect
        runCatching {
            LiveKitCallController.connect(
                context = context,
                url = s.livekitUrl,
                token = s.token,
                enableVideo = s.media == "video",
                waitForPeer = s.asCaller,
            )
        }
    }

    LaunchedEffect(answered, remoteJoined, session?.asCaller) {
        val s = session ?: return@LaunchedEffect
        if (answered && s.asCaller && remoteJoined) {
            LiveKitCallController.enableMedia(s.media == "video")
        }
    }

    LaunchedEffect(isLive) {
        if (!isLive) {
            elapsedSec = 0
            return@LaunchedEffect
        }
        elapsedSec = 0
        while (true) {
            delay(1000)
            elapsedSec += 1
        }
    }

    // Peer cancelled / declined / timeout — leave this screen.
    LaunchedEffect(endedReason) {
        val reason = endedReason ?: return@LaunchedEffect
        if (leaving) return@LaunchedEffect
        leaving = true
        leaveLabel = when (reason) {
            "rejected" -> "Declined"
            "timeout", "missed" -> "No answer"
            "cancelled" -> "Cancelled"
            else -> "Call ended"
        }
        LiveKitCallController.disconnect()
        ActiveCallSession.consumeEndedReason()
        delay(800)
        onEnd()
    }

    fun hangUp() {
        if (leaving) return
        leaving = true
        scope.launch {
            val callId = session?.callId
            LiveKitCallController.disconnect()
            if (!callId.isNullOrBlank()) {
                runCatching { NetworkModule.api().postCallsIdAction(callId, "end") }
            }
            ActiveCallSession.clear()
            onEnd()
        }
    }

    val statusLine = when {
        leaveLabel != null -> leaveLabel!!
        !roomConnected -> "Connecting…"
        session?.asCaller == true && !isLive -> "Ringing…"
        isLive -> formatElapsed(elapsedSec)
        else -> "Connecting…"
    }

    WhatsAppCallLayout(
        name = peerName,
        statusLine = statusLine,
        videoMode = enableVideo,
        muted = muted,
        speakerOn = speakerOn,
        cameraOff = cameraOff,
        remoteVideo = remoteVideo,
        localVideo = localVideo,
        onMute = { LiveKitCallController.setMuted(!muted) },
        onSpeaker = { LiveKitCallController.setSpeaker(context, !speakerOn) },
        onCamera = { LiveKitCallController.setCameraOff(!cameraOff) },
        onFlip = { LiveKitCallController.flipCamera() },
        onEnd = { hangUp() },
    )
}

@Composable
private fun WhatsAppCallLayout(
    name: String,
    statusLine: String,
    videoMode: Boolean,
    muted: Boolean,
    speakerOn: Boolean,
    cameraOff: Boolean,
    remoteVideo: VideoTrack?,
    localVideo: VideoTrack?,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onCamera: () -> Unit,
    onFlip: () -> Unit,
    onEnd: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallBg),
    ) {
        if (videoMode && remoteVideo != null && !cameraOff) {
            VideoTrackAndroidView(track = remoteVideo, modifier = Modifier.fillMaxSize())
        } else if (videoMode) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A242C), CallBg))),
                contentAlignment = Alignment.Center,
            ) {
                Avatar(name, size = 120)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            if (!videoMode || remoteVideo == null) {
                Avatar(name, size = 112)
                Spacer(Modifier.height(20.dp))
            }
            Text(
                name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
            )
            Spacer(Modifier.height(8.dp))
            // Single status / timer line (WhatsApp-style).
            Text(
                statusLine,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(Modifier.weight(1f))

            if (videoMode && localVideo != null && !cameraOff) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 20.dp, bottom = 16.dp)
                        .size(width = 96.dp, height = 132.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                ) {
                    VideoTrackAndroidView(
                        track = localVideo,
                        modifier = Modifier.fillMaxSize(),
                        mirror = true,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(CallPanel)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconOnlyControl(
                    icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                    active = muted,
                    onClick = onMute,
                )
                if (videoMode) {
                    IconOnlyControl(
                        icon = if (cameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        active = !cameraOff,
                        onClick = onCamera,
                    )
                    IconOnlyControl(
                        icon = Icons.Default.Cameraswitch,
                        active = false,
                        onClick = onFlip,
                    )
                } else {
                    IconOnlyControl(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        active = speakerOn,
                        onClick = onSpeaker,
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CallRed)
                        .clickable(onClick = onEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun IconOnlyControl(
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (active) CallGreen else Color.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (active) Color.White else Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun VideoTrackAndroidView(
    track: VideoTrack,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
) {
    val eglBase = remember { EglBase.create() }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureViewRenderer(ctx).also { renderer ->
                renderer.init(eglBase.eglBaseContext, null)
                renderer.setMirror(mirror)
                track.addRenderer(renderer)
            }
        },
        update = { },
        onRelease = { renderer ->
            track.removeRenderer(renderer)
            renderer.release()
        },
    )
    DisposableEffect(Unit) {
        onDispose { eglBase.release() }
    }
}

private fun formatElapsed(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return "%d:%02d".format(m, s)
}
