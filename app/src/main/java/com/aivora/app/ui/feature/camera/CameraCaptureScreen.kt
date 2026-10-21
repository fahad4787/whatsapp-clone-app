package com.aivora.app.ui.feature.camera

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

/**
 * @param target `"status"` or `"chat:{id}"`
 */
@Composable
fun CameraCaptureScreen(
    target: String = "status",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var mode by remember { mutableIntStateOf(1) } // 0 video, 1 photo
    val media = rememberMediaActions(context) { uri ->
        val value = uri.toString()
        when {
            target.startsWith("chat:") -> {
                val chatId = target.removePrefix("chat:")
                InMemoryChatStore.sendMedia(chatId, value)
                onBack?.invoke()
            }
            else -> {
                PendingMedia.set(value)
                onNavigate(Routes.NewStatus)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                "Camera",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.WbSunny, contentDescription = "Flash", tint = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A), Color(0xFF0D0D0D)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Mock grid + focus (no live camera preview in local shell)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .border(2.dp, AivoraColors.Primary, RoundedCornerShape(8.dp)),
                )
                Spacer(Modifier.height(24.dp))
                Text("1x", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "VIDEO",
                color = if (mode == 0) AivoraColors.Primary else Color.White.copy(alpha = 0.55f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { mode = 0 }
                    .padding(horizontal = 16.dp),
            )
            Text(
                "PHOTO",
                color = if (mode == 1) AivoraColors.Primary else Color.White.copy(alpha = 0.55f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clickable { mode = 1 }
                    .padding(horizontal = 16.dp),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, Color.White, RoundedCornerShape(10.dp))
                    .background(Color(0xFF3A3A3A))
                    .clickable { onNavigate(Routes.gallery(target)) },
            )
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .border(4.dp, Color.White, CircleShape)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable(onClick = media.takePhoto),
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable { },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Cameraswitch, contentDescription = "Flip", tint = Color.White)
            }
        }
        Text(
            "Tap for a photo · Hold for video",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp),
        )
    }
}
