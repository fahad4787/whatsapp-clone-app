package com.aivora.app.ui.feature.calls

import com.aivora.app.ui.icons.AppIcons

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.ExpandMore
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
import com.aivora.app.data.call.CallLog
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.theme.AivoraColors
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Bolta Figma call tokens (file EWBSJebF5KWRv6BIGzq1VO). */
private val CallBg: Color get() = AivoraColors.LightBackground
private val CallInk: Color get() = if (AivoraColors.isDark) Color(0xFFE9EDEF) else Color(0xFF17182B)
private val CallPanel: Color get() = AivoraColors.LightSurface
private val CallControlIdle: Color get() = AivoraColors.SurfaceMuted
private val CallBorder: Color get() = AivoraColors.ChatDivider
private val CallEndRed = Color(0xFFE64C53)
private val CallMuted = Color(0xFFA6B0A9)

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

    LaunchedEffect(session?.callId, permissionReady, enableVideo) {
        val s = session
        if (!permissionReady || s == null) return@LaunchedEffect
        runCatching {
            LiveKitCallController.connect(
                context = context,
                url = s.livekitUrl,
                token = s.token,
                enableVideo = enableVideo || s.media == "video",
                waitForPeer = s.asCaller && !enableVideo, // video: publish self-preview while ringing
            )
        }.onFailure { CallLog.e("in-call connect failed callId=${s.callId}", it) }
    }

    LaunchedEffect(answered, remoteJoined, session?.asCaller, enableVideo) {
        val s = session ?: return@LaunchedEffect
        if (answered && s.asCaller && remoteJoined) {
            LiveKitCallController.enableMedia(enableVideo || s.media == "video")
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
            CallLog.i("hangUp callId=$callId")
            LiveKitCallController.disconnect()
            if (!callId.isNullOrBlank()) {
                runCatching { NetworkModule.api().postCallsIdAction(callId, "end") }
                    .onFailure { CallLog.w("end api failed callId=$callId", it) }
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
    val connectedLabel = when {
        leaveLabel != null -> leaveLabel!!
        isLive -> "Connected"
        session?.asCaller == true && roomConnected -> "Calling"
        roomConnected -> "Connecting"
        else -> "Connecting"
    }

    BoltaCallScreen(
        name = peerName,
        callTypeLabel = if (enableVideo) "Video call" else "Audio call",
        connectedLabel = connectedLabel,
        elapsedLabel = if (isLive) formatElapsed(elapsedSec) else statusLine.takeIf { !isLive },
        showSpeaking = isLive && !muted,
        videoMode = enableVideo,
        muted = muted,
        speakerOn = speakerOn,
        cameraOff = cameraOff,
        remoteVideo = remoteVideo,
        localVideo = localVideo,
        onBack = { hangUp() },
        onMute = { LiveKitCallController.setMuted(!muted) },
        onSpeaker = { LiveKitCallController.setSpeaker(context, !speakerOn) },
        onCamera = {
            if (enableVideo) LiveKitCallController.setCameraOff(!cameraOff)
        },
        onFlip = { LiveKitCallController.flipCamera() },
        onEnd = { hangUp() },
    )
}

@Composable
private fun BoltaCallScreen(
    name: String,
    callTypeLabel: String,
    connectedLabel: String,
    elapsedLabel: String?,
    showSpeaking: Boolean,
    videoMode: Boolean,
    muted: Boolean,
    speakerOn: Boolean,
    cameraOff: Boolean,
    remoteVideo: VideoTrack?,
    localVideo: VideoTrack?,
    onBack: () -> Unit,
    onMute: () -> Unit,
    onSpeaker: () -> Unit,
    onCamera: () -> Unit,
    onFlip: () -> Unit,
    onEnd: () -> Unit,
) {
    if (videoMode) {
        WhatsAppVideoCallScreen(
            name = name,
            connectedLabel = connectedLabel,
            elapsedLabel = elapsedLabel,
            muted = muted,
            cameraOff = cameraOff,
            remoteVideo = remoteVideo,
            localVideo = localVideo,
            onMute = onMute,
            onCamera = onCamera,
            onFlip = onFlip,
            onEnd = onEnd,
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallBg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CallPanel)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcons.ExpandMore,
                        contentDescription = "Minimize",
                        tint = CallInk,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Text(
                    callTypeLabel,
                    color = CallInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(224.dp)) {
                    Box(
                        Modifier
                            .size(224.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary.copy(alpha = 0.08f)),
                    )
                    Box(
                        Modifier
                            .size(192.dp)
                            .clip(CircleShape)
                            .border(2.dp, AivoraColors.Primary.copy(alpha = 0.35f), CircleShape),
                    )
                    Avatar(name, size = 160)
                }
                Spacer(Modifier.height(28.dp))

                Text(
                    name,
                    color = CallInk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (connectedLabel == "Connected") AivoraColors.Primary
                                else CallMuted,
                            ),
                    )
                    Text(
                        connectedLabel,
                        color = if (connectedLabel == "Connected") AivoraColors.Online else CallMuted,
                        fontSize = 14.sp,
                    )
                    if (elapsedLabel != null && connectedLabel == "Connected") {
                        Text("·", color = CallMuted, fontSize = 14.sp)
                        Text(
                            elapsedLabel,
                            color = CallInk,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                        )
                    } else if (elapsedLabel != null && connectedLabel != "Connected") {
                        Text("·", color = CallMuted, fontSize = 14.sp)
                        Text(elapsedLabel, color = CallMuted, fontSize = 14.sp)
                    }
                }

                if (showSpeaking) {
                    Spacer(Modifier.height(20.dp))
                    SpeakingWave()
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "$name is speaking",
                        color = AivoraColors.TextSecondary,
                        fontSize = 12.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(CallPanel)
                    .border(1.dp, CallBorder, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(top = 32.dp, bottom = 28.dp, start = 24.dp, end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    BoltaControl(
                        icon = if (muted) AppIcons.MicOff else AppIcons.Mic,
                        label = "Mute",
                        active = muted,
                        onClick = onMute,
                    )
                    BoltaControl(
                        icon = AppIcons.VolumeUp,
                        label = "Speaker",
                        active = speakerOn,
                        onClick = onSpeaker,
                    )
                    BoltaControl(
                        icon = AppIcons.PersonAdd,
                        label = "Add person",
                        active = false,
                        onClick = {},
                    )
                }
                Spacer(Modifier.height(32.dp))
                Row(
                    modifier = Modifier
                        .width(216.dp)
                        .height(64.dp)
                        .clip(RoundedCornerShape(1000.dp))
                        .background(CallEndRed)
                        .clickable(onClick = onEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        AppIcons.CallEnd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "End call",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}

/**
 * WhatsApp-style video: big = peer (default), PiP = me; tap PiP to swap.
 * Ringing (no peer yet): self fills the stage.
 */
@Composable
private fun WhatsAppVideoCallScreen(
    name: String,
    connectedLabel: String,
    elapsedLabel: String?,
    muted: Boolean,
    cameraOff: Boolean,
    remoteVideo: VideoTrack?,
    localVideo: VideoTrack?,
    onMute: () -> Unit,
    onCamera: () -> Unit,
    onFlip: () -> Unit,
    onEnd: () -> Unit,
) {
    val hasRemote = remoteVideo != null
    val hasLocal = localVideo != null && !cameraOff
    // Tap PiP → swap who is big. Reset when peer leaves.
    var swapped by remember { mutableStateOf(false) }
    LaunchedEffect(hasRemote) {
        if (!hasRemote) swapped = false
    }

    val bigTrack: VideoTrack?
    val pipTrack: VideoTrack?
    val bigIsLocal: Boolean
    when {
        hasRemote && hasLocal -> {
            if (swapped) {
                bigTrack = localVideo
                pipTrack = remoteVideo
                bigIsLocal = true
            } else {
                bigTrack = remoteVideo
                pipTrack = localVideo
                bigIsLocal = false
            }
        }
        hasRemote -> {
            bigTrack = remoteVideo
            pipTrack = null
            bigIsLocal = false
        }
        hasLocal -> {
            bigTrack = localVideo
            pipTrack = null
            bigIsLocal = true
        }
        else -> {
            bigTrack = null
            pipTrack = null
            bigIsLocal = false
        }
    }

    var controlsVisible by remember { mutableStateOf(true) }
    LaunchedEffect(controlsVisible, connectedLabel) {
        if (!controlsVisible) return@LaunchedEffect
        if (connectedLabel != "Connected") return@LaunchedEffect
        delay(4_000)
        controlsVisible = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) { controlsVisible = !controlsVisible },
    ) {
        // Stable main renderer — swap track in place (cheaper than recreate).
        if (bigTrack != null) {
            VideoTrackAndroidView(
                track = bigTrack,
                modifier = Modifier.fillMaxSize(),
                mirror = bigIsLocal,
                fill = true,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 140.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Avatar(name, size = 120)
                Spacer(Modifier.height(16.dp))
                Text(
                    if (cameraOff) "Camera off" else "Starting camera…",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                    ),
                ),
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                    ),
                ),
        )

        if (controlsVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    name,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    when {
                        connectedLabel == "Connected" && elapsedLabel != null -> elapsedLabel
                        connectedLabel == "Calling" -> "Calling…"
                        else -> connectedLabel
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                )
            }
        }

        // PiP — tap swaps big/small (WA). Does not flip lens.
        if (pipTrack != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 14.dp, bottom = if (controlsVisible) 132.dp else 24.dp)
                    .size(width = 108.dp, height = 152.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .clickable {
                        swapped = !swapped
                        controlsVisible = true
                    },
            ) {
                VideoTrackAndroidView(
                    track = pipTrack,
                    modifier = Modifier.fillMaxSize(),
                    mirror = pipTrack === localVideo,
                    fill = true,
                )
            }
        }

        if (controlsVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WaVideoControl(
                    icon = if (muted) AppIcons.MicOff else AppIcons.Mic,
                    label = if (muted) "Unmute" else "Mute",
                    active = muted,
                    onClick = onMute,
                )
                WaVideoControl(
                    icon = AppIcons.Cameraswitch,
                    label = "Flip",
                    active = false,
                    onClick = onFlip,
                )
                WaVideoControl(
                    icon = if (cameraOff) AppIcons.VideocamOff else AppIcons.Videocam,
                    label = if (cameraOff) "Camera on" else "Camera",
                    active = cameraOff,
                    onClick = onCamera,
                )
                WaVideoControl(
                    icon = AppIcons.CallEnd,
                    label = "End",
                    active = true,
                    danger = true,
                    onClick = onEnd,
                )
            }
        }
    }
}

@Composable
private fun WaVideoControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    danger: Boolean = false,
) {
    val bg = when {
        danger -> CallEndRed
        active -> Color.White
        else -> Color.White.copy(alpha = 0.18f)
    }
    val tint = when {
        danger -> Color.White
        active -> Color.Black
        else -> Color.White
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(bg)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp)
    }
}

@Composable
private fun SpeakingWave() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp),
    ) {
        val heights = listOf(8, 13, 21, 15, 24, 11, 18, 8, 15)
        heights.forEach { h ->
            Box(
                Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AivoraColors.Primary),
            )
        }
    }
}

@Composable
private fun BoltaControl(
    icon: ImageVector,
    label: String,
    active: Boolean,
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
                .background(if (active) AivoraColors.Primary else CallControlIdle)
                .border(1.dp, if (active) AivoraColors.Primary else CallBorder, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (active) Color.White else CallInk,
                modifier = Modifier.size(28.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            label,
            color = if (active) AivoraColors.Online else CallInk,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun VideoTrackAndroidView(
    track: VideoTrack,
    modifier: Modifier = Modifier,
    mirror: Boolean = false,
    fill: Boolean = true,
) {
    // Must use Room EGL via LiveKitCallController.initRenderer — a private EglBase
    // leaves the local camera preview blank.
    var bound by remember { mutableStateOf<VideoTrack?>(null) }
    val roomConnected by LiveKitCallController.roomConnected.collectAsState()
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextureViewRenderer(ctx).also { renderer ->
                LiveKitCallController.initRenderer(renderer, fill = fill)
                renderer.setMirror(mirror)
                track.addRenderer(renderer)
                bound = track
            }
        },
        update = { renderer ->
            // Room may connect after first composition — re-init once available.
            if (roomConnected) {
                runCatching { LiveKitCallController.initRenderer(renderer, fill = fill) }
            }
            renderer.setMirror(mirror)
            val prev = bound
            if (prev !== track) {
                prev?.removeRenderer(renderer)
                track.addRenderer(renderer)
                bound = track
            }
        },
        onRelease = { renderer ->
            bound?.removeRenderer(renderer)
            bound = null
            renderer.release()
        },
    )
}

private fun formatElapsed(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(m, s)
}
