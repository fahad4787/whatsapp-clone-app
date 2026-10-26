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
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import livekit.org.webrtc.EglBase

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
    val connected by LiveKitCallController.connected.collectAsState()
    val muted by LiveKitCallController.muted.collectAsState()
    val speakerOn by LiveKitCallController.speakerOn.collectAsState()
    val cameraOff by LiveKitCallController.cameraOff.collectAsState()
    val remoteVideo by LiveKitCallController.remoteVideo.collectAsState()
    val localVideo by LiveKitCallController.localVideo.collectAsState()
    val peerName = session?.peerName?.ifBlank { name } ?: name
    val enableVideo = videoMode || session?.media == "video"

    var elapsedSec by remember { mutableLongStateOf(0L) }
    var statusText by remember { mutableStateOf("Connecting…") }
    var permissionReady by remember { mutableStateOf(false) }

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
        if (missing.isEmpty()) {
            permissionReady = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }

    LaunchedEffect(session?.callId, permissionReady) {
        val s = session
        if (!permissionReady || s == null) return@LaunchedEffect
        statusText = if (s.asCaller) "Calling…" else "Connecting…"
        runCatching {
            LiveKitCallController.connect(
                context = context,
                url = s.livekitUrl,
                token = s.token,
                enableVideo = s.media == "video",
            )
            statusText = "Connected"
        }.onFailure {
            statusText = "Failed to connect"
        }
    }

    LaunchedEffect(connected) {
        if (!connected) return@LaunchedEffect
        elapsedSec = 0
        while (true) {
            delay(1000)
            elapsedSec += 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // Keep room if minimizing later; for now hangup is explicit.
        }
    }

    fun hangUp() {
        scope.launch {
            val callId = session?.callId
            LiveKitCallController.disconnect()
            if (!callId.isNullOrBlank()) {
                runCatching {
                    NetworkModule.api().postCallsIdAction(callId, "end")
                }
            }
            ActiveCallSession.clear()
            onEnd()
        }
    }

    if (enableVideo) {
        VideoCallLayout(
            name = peerName,
            statusText = if (connected) "Connected · ${formatElapsed(elapsedSec)}" else statusText,
            muted = muted,
            cameraOff = cameraOff,
            remoteVideo = remoteVideo,
            localVideo = localVideo,
            onMute = { LiveKitCallController.setMuted(!muted) },
            onCamera = { LiveKitCallController.setCameraOff(!cameraOff) },
            onEnd = { hangUp() },
        )
    } else {
        AudioCallLayout(
            name = peerName,
            statusText = if (connected) "Connected" else statusText,
            elapsed = formatElapsed(elapsedSec),
            muted = muted,
            speakerOn = speakerOn,
            onMute = { LiveKitCallController.setMuted(!muted) },
            onSpeaker = { LiveKitCallController.setSpeaker(context, !speakerOn) },
            onEnd = { hangUp() },
        )
    }
}

@Composable
private fun AudioCallLayout(
    name: String,
    statusText: String,
    elapsed: String,
    muted: Boolean,
    speakerOn: Boolean,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onEnd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.ChatWallpaperLight)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onEnd,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = AivoraColors.Ink)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Audio call",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.InkMuted,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(224.dp)) {
                Box(
                    modifier = Modifier
                        .size(224.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary.copy(alpha = 0.08f)),
                )
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary.copy(alpha = 0.12f)),
                )
                Avatar(name, size = 160)
            }
            Spacer(Modifier.height(28.dp))
            Text(
                name,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.InkMuted,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary),
                )
                Spacer(Modifier.width(8.dp))
                Text(statusText, color = AivoraColors.Online, style = MaterialTheme.typography.bodyMedium)
                Text("  ·  ", color = AivoraColors.IconSecondary)
                Text(elapsed, color = AivoraColors.InkMuted, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            }
        }

        CallControlsPanel(
            muted = muted,
            speakerOn = speakerOn,
            onMute = onMute,
            onSpeaker = onSpeaker,
            onEnd = onEnd,
            videoMode = false,
        )
    }
}

@Composable
private fun VideoCallLayout(
    name: String,
    statusText: String,
    muted: Boolean,
    cameraOff: Boolean,
    remoteVideo: VideoTrack?,
    localVideo: VideoTrack?,
    onMute: () -> Unit,
    onCamera: () -> Unit,
    onEnd: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A)),
    ) {
        if (remoteVideo != null) {
            VideoTrackAndroidView(
                track = remoteVideo,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1A242C), Color(0xFF0B141A)),
                        ),
                    ),
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onEnd,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text(statusText, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 24.dp, bottom = 16.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF243038))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    cameraOff || localVideo == null -> {
                        Icon(Icons.Default.VideocamOff, null, tint = Color.White.copy(alpha = 0.7f))
                    }
                    else -> {
                        VideoTrackAndroidView(
                            track = localVideo,
                            modifier = Modifier.fillMaxSize(),
                            mirror = true,
                        )
                    }
                }
            }

            CallControlsPanel(
                muted = muted,
                speakerOn = true,
                onMute = onMute,
                onSpeaker = onCamera,
                onEnd = onEnd,
                videoMode = true,
                cameraOff = cameraOff,
            )
        }
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
        update = { renderer ->
            // Track already attached in factory for this renderer instance.
        },
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

@Composable
private fun CallControlsPanel(
    muted: Boolean,
    speakerOn: Boolean,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onEnd: () -> Unit,
    videoMode: Boolean,
    cameraOff: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(if (videoMode) Color(0xFF1A242C) else Color.White)
            .border(
                width = if (videoMode) 0.dp else 1.dp,
                color = AivoraColors.ChatDivider,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            )
            .padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CallControl(
                icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                label = "Mute",
                active = muted,
                onDark = videoMode,
                onClick = onMute,
            )
            CallControl(
                icon = if (videoMode) {
                    if (cameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam
                } else {
                    Icons.Default.VolumeUp
                },
                label = if (videoMode) "Camera" else "Speaker",
                active = if (videoMode) !cameraOff else speakerOn,
                onDark = videoMode,
                onClick = onSpeaker,
            )
            CallControl(
                icon = Icons.Default.Dialpad,
                label = "Keypad",
                active = false,
                onDark = videoMode,
                onClick = { },
            )
            CallControl(
                icon = Icons.Default.PersonAdd,
                label = "Add person",
                active = false,
                onDark = videoMode,
                onClick = { },
            )
        }
        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier
                .width(216.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color(0xFFE64C53))
                .clickable(onClick = onEnd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Text("End call", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CallControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onDark: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(84.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    when {
                        active -> AivoraColors.Primary
                        onDark -> Color.White.copy(alpha = 0.12f)
                        else -> AivoraColors.ChatWallpaperLight
                    },
                )
                .then(
                    if (!active && !onDark) {
                        Modifier.border(1.dp, AivoraColors.ChatDivider, CircleShape)
                    } else {
                        Modifier
                    },
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = when {
                    active -> AivoraColors.OnPrimary
                    onDark -> Color.White
                    else -> AivoraColors.InkMuted
                },
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
            color = when {
                active && !onDark -> AivoraColors.Online
                onDark -> Color.White.copy(alpha = 0.85f)
                else -> AivoraColors.InkMuted
            },
        )
    }
}
