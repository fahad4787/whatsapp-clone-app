package com.aivora.app.ui.feature.status

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

private val StatusColors = listOf(
    Color(0xFF1DAA61),
    Color(0xFF0B6B3A),
    Color(0xFF1B3A6B),
    Color(0xFF4A90D9),
    Color(0xFF9B8CDB),
    Color(0xFF2C2C2C),
    Color(0xFFD64545),
    Color(0xFFE67E22),
)

@Composable
fun NewStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var mediaUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        PendingMedia.consume()?.let { mediaUri = it }
    }

    val media = rememberMediaActions(context) { uri ->
        mediaUri = uri.toString()
    }

    val bg = StatusColors[colorIndex.coerceIn(StatusColors.indices)]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onBack?.invoke() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f)),
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
            Text(
                "New status",
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
            )
            IconButton(
                onClick = media.takePhoto,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f)),
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.Black)
            }
            Spacer(modifier = Modifier.size(6.dp))
            IconButton(
                onClick = media.pickGallery,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f)),
            ) {
                Icon(Icons.Default.Photo, null, tint = Color.Black)
            }
        }

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(Color.White),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (text.isEmpty()) {
                        Text(
                            "Type a status",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    inner()
                }
            },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                StatusColors.forEachIndexed { index, color ->
                    val selected = index == colorIndex
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                                else Modifier,
                            )
                            .clickable { colorIndex = index },
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Your status disappears after 24 hours.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = {
                        if (text.isBlank() && mediaUri == null) return@IconButton
                        InMemoryStatusStore.post(text.ifBlank { " " }, mediaUri)
                        onNavigate(Routes.Home)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                ) {
                    Icon(Icons.Default.Send, null, tint = AivoraColors.Primary)
                }
            }
        }
    }
}
