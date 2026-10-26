package com.aivora.app.ui.feature.calls

import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

@Composable
fun IncomingCallOverlay(
    onNavigate: (String) -> Unit,
) {
    val incoming by ActiveCallSession.incoming.collectAsState()
    val call = incoming ?: return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val name = call.caller?.name?.ifBlank { null } ?: "Incoming call"
    val isVideo = call.media.equals("video", ignoreCase = true)

    val ringtone = remember {
        runCatching {
            RingtoneManager.getRingtone(
                context,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            )
        }.getOrNull()
    }

    DisposableEffect(call.id) {
        val am = context.getSystemService(AudioManager::class.java)
        val prevMode = am?.mode
        am?.mode = AudioManager.MODE_RINGTONE
        runCatching { ringtone?.play() }
        val vibrator = if (Build.VERSION.SDK_INT >= 31) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 800, 400, 800), 0),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 800, 400, 800), 0)
        }
        onDispose {
            runCatching { ringtone?.stop() }
            vibrator?.cancel()
            if (prevMode != null) am?.mode = prevMode
        }
    }

    fun decline() {
        scope.launch {
            val id = call.id ?: return@launch
            ActiveCallSession.clearIncoming()
            runCatching { NetworkModule.api().postCallsIdAction(id, "reject") }
        }
    }

    fun accept() {
        scope.launch {
            val id = call.id ?: return@launch
            val result = runCatching {
                NetworkModule.api().postCallsIdAction(id, "answer")
            }.getOrNull()
            val token = result?.token
            val url = result?.livekitUrl
            if (token.isNullOrBlank() || url.isNullOrBlank()) {
                ActiveCallSession.clearIncoming()
                return@launch
            }
            ActiveCallSession.setActive(
                callId = id,
                token = token,
                livekitUrl = url,
                media = call.media ?: "audio",
                peerName = name,
                asCaller = false,
            )
            onNavigate(
                if (isVideo) Routes.videoCall(name) else Routes.audioCall(name),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B141A), Color(0xFF1A242C), Color(0xFF0B141A)),
                ),
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (isVideo) "Incoming video call" else "Incoming voice call",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 15.sp,
            )
            Spacer(Modifier.height(40.dp))
            Avatar(name, size = 140)
            Spacer(Modifier.height(24.dp))
            Text(
                name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE64C53))
                            .clickable { decline() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.CallEnd,
                            contentDescription = "Decline",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Decline", color = Color.White.copy(alpha = 0.85f))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary)
                            .clickable { accept() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Accept",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("Accept", color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
