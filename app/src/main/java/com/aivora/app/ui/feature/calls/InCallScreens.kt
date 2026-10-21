package com.aivora.app.ui.feature.calls

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@Composable
fun AudioCallScreen(
    name: String = "George Alan",
    onEnd: () -> Unit = {},
) {
    var muted by remember { mutableStateOf(false) }
    var speaker by remember { mutableStateOf(true) }

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
                Text("Connected", color = AivoraColors.Online, style = MaterialTheme.typography.bodyMedium)
                Text("  ·  ", color = AivoraColors.IconSecondary)
                Text("02:34", color = AivoraColors.InkMuted, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(18.dp))
            VoiceActivityBars()
            Spacer(Modifier.height(8.dp))
            Text(
                "${name.substringBefore(' ')} is speaking",
                color = AivoraColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        CallControlsPanel(
            muted = muted,
            speakerOn = speaker,
            onMute = { muted = !muted },
            onSpeaker = { speaker = !speaker },
            onEnd = onEnd,
            videoMode = false,
        )
    }
}

@Composable
fun VideoCallScreen(
    name: String = "George Alan",
    onEnd: () -> Unit = {},
) {
    var muted by remember { mutableStateOf(false) }
    var cameraOff by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A)),
    ) {
        // Mock remote video plane
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A242C), Color(0xFF0B141A)),
                    ),
                ),
        )
        if (cameraOff) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AivoraColors.Primary),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Connected · 02:34", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Local preview stub
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
                if (cameraOff) {
                    Icon(Icons.Default.VideocamOff, null, tint = Color.White.copy(alpha = 0.7f))
                } else {
                    Avatar("You", size = 48)
                }
            }

            CallControlsPanel(
                muted = muted,
                speakerOn = true,
                onMute = { muted = !muted },
                onSpeaker = { cameraOff = !cameraOff },
                onEnd = onEnd,
                videoMode = true,
                cameraOff = cameraOff,
            )
        }
    }
}

@Composable
private fun VoiceActivityBars() {
    val heights = listOf(8, 13, 21, 15, 24, 11, 18, 8, 15)
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(24.dp),
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(h.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AivoraColors.Primary),
            )
        }
    }
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
