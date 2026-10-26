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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
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
import androidx.compose.ui.text.style.TextAlign
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

/** Classic WhatsApp call palette */
private val WaBg = Color(0xFF0B141A)
private val WaSheet = Color(0xFF1F2C34)
private val WaGreen = Color(0xFF00A884)
private val WaRed = Color(0xFFF15C6D)
private val WaMuted = Color(0xFF8696A0)

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

    CallSystemBars()

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

    // IMPORTANT: do not clear endedReason until AFTER onEnd — clearing cancels this effect.
    LaunchedEffect(endedReason) {
        val reason = endedReason ?: return@LaunchedEffect
        if (leaving) return@LaunchedEffect
        leaving = true
        leaveLabel = when (reason) {
            "rejected" -> "Call declined"
            "timeout", "missed" -> "No answer"
            "cancelled" -> "Call cancelled"
            else -> "Call ended"
        }
        LiveKitCallController.disconnect()
        delay(1200)
        ActiveCallSession.clear()
        ActiveCallSession.consumeEndedReason()
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
        session?.asCaller == true && !isLive -> "Calling…"
        isLive -> formatElapsed(elapsedSec)
        else -> "Connecting…"
    }

    WhatsAppCallScreen(
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
        onCamera = {
            if (!enableVideo) {
                // Mirror WA: tapping Video on an audio call upgrades intent — for now toggle camera when video.
            } else {
                LiveKitCallController.setCameraOff(!cameraOff)
            }
        },
        onFlip = { LiveKitCallController.flipCamera() },
        onEnd = { hangUp() },
        showVideoControl = true,
        videoControlEnabled = enableVideo,
    )
}

@Composable
private fun WhatsAppCallScreen(
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
    showVideoControl: Boolean,
    videoControlEnabled: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WaBg),
    ) {
        if (videoMode && remoteVideo != null) {
            VideoTrackAndroidView(track = remoteVideo, modifier = Modifier.fillMaxSize())
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top: encrypted + add person (WA-like)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = WaMuted,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "End-to-end encrypted",
                        color = WaMuted,
                        fontSize = 13.sp,
                    )
                }
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = "Add",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.height(36.dp))

            if (!videoMode || remoteVideo == null) {
                // Soft rings behind avatar (WA vibe)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                    Box(
                        Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.04f)),
                    )
                    Box(
                        Modifier
                            .size(168.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f)),
                    )
                    Avatar(name, size = 136)
                }
                Spacer(Modifier.height(28.dp))
            } else {
                Spacer(Modifier.height(12.dp))
            }

            Text(
                name,
                color = Color.White,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                statusLine,
                color = WaMuted,
                fontSize = 15.sp,
            )

            Spacer(Modifier.weight(1f))

            if (videoMode && localVideo != null && !cameraOff) {
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 20.dp, bottom = 12.dp)
                        .size(width = 100.dp, height = 140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                ) {
                    VideoTrackAndroidView(
                        track = localVideo,
                        modifier = Modifier.fillMaxSize(),
                        mirror = true,
                    )
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Flip",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .clickable(onClick = onFlip)
                            .padding(5.dp),
                    )
                }
            }

            // Bottom control sheet — WhatsApp style
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(WaSheet)
                    .padding(top = 28.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    WaLabeledControl(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        label = "Speaker",
                        active = speakerOn,
                        onClick = onSpeaker,
                    )
                    if (showVideoControl) {
                        WaLabeledControl(
                            icon = if (!videoControlEnabled || cameraOff) {
                                Icons.Default.VideocamOff
                            } else {
                                Icons.Default.Videocam
                            },
                            label = "Video",
                            active = videoControlEnabled && !cameraOff,
                            enabled = videoControlEnabled,
                            onClick = onCamera,
                        )
                    } else {
                        WaLabeledControl(
                            icon = Icons.Default.Bluetooth,
                            label = "Bluetooth",
                            active = false,
                            enabled = false,
                            onClick = {},
                        )
                    }
                    WaLabeledControl(
                        icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = "Mute",
                        active = muted,
                        onClick = onMute,
                    )
                }
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(WaRed)
                        .clickable(onClick = onEnd),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End call",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WaLabeledControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    when {
                        !enabled -> Color.White.copy(alpha = 0.06f)
                        active -> Color.White
                        else -> Color.White.copy(alpha = 0.12f)
                    },
                )
                .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = when {
                    !enabled -> WaMuted.copy(alpha = 0.5f)
                    active -> WaBg
                    else -> Color.White
                },
                modifier = Modifier.size(24.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            label,
            color = if (enabled) WaMuted else WaMuted.copy(alpha = 0.45f),
            fontSize = 12.sp,
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
