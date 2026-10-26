package com.aivora.app.ui.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val scope = rememberCoroutineScope()
    val name = call.caller?.name?.ifBlank { null } ?: "Incoming call"
    val isVideo = call.media.equals("video", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1A242C))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, size = 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                Text(
                    if (isVideo) "Incoming video call" else "Incoming voice call",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.size(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(
                onClick = {
                    scope.launch {
                        val id = call.id ?: return@launch
                        runCatching {
                            NetworkModule.api().postCallsIdAction(id, "reject")
                        }
                        ActiveCallSession.clearIncoming()
                    }
                },
            ) {
                Icon(
                    Icons.Default.CallEnd,
                    contentDescription = "Reject",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE64C53))
                        .padding(12.dp),
                )
            }
            TextButton(
                onClick = {
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
                },
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Accept",
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary)
                        .padding(12.dp),
                )
            }
        }
    }
}
