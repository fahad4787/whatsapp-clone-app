package com.aivora.app.ui.feature.status

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.StatusRepository
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.PendingMedia
import com.aivora.app.data.local.SessionStore
import com.aivora.app.ui.media.rememberViewportImageModel
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch

private val StatusColors = listOf(
    Color(0xFF075E54),
    Color(0xFF128C7E),
    Color(0xFF1DAA61),
    Color(0xFF1B3A6B),
    Color(0xFF4A90D9),
    Color(0xFF6B4C9A),
    Color(0xFFD64545),
    Color(0xFFE67E22),
    Color(0xFF2C2C2C),
)

private val StatusColorHex = listOf(
    "#075E54", "#128C7E", "#1DAA61", "#1B3A6B",
    "#4A90D9", "#6B4C9A", "#D64545", "#E67E22", "#2C2C2C",
)

@Composable
fun NewStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var colorIndex by remember { mutableIntStateOf(0) }
    var mediaUri by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var showColors by remember { mutableStateOf(true) }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }

    LaunchedEffect(Unit) {
        PendingMedia.consume()?.let {
            mediaUri = it
            showColors = false
        }
    }

    val bg = StatusColors[colorIndex.coerceIn(StatusColors.indices)]
    val isTextOnly = mediaUri == null
    val canSend = text.isNotBlank() || mediaUri != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isTextOnly) bg else Color.Black),
    ) {
        if (mediaUri != null) {
            AsyncImage(
                model = rememberViewportImageModel(mediaUri),
                contentDescription = "Status photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.28f)),
            )
        }

        // Top bar — close | privacy + colour (WhatsApp-style spacing)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(
                    AppIcons.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onNavigate(Routes.SettingsPrivacyStatus) }) {
                Icon(
                    AppIcons.Lock,
                    contentDescription = "Status privacy",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            if (isTextOnly) {
                IconButton(onClick = { showColors = !showColors }) {
                    Icon(
                        AppIcons.Palette,
                        contentDescription = "Background colour",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        // Centered text / caption (matches WhatsApp compose + view)
        BasicTextField(
            value = text,
            onValueChange = { if (it.length <= 700) text = it },
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(vertical = 96.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = if (isTextOnly) 32.sp else 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = if (isTextOnly) 40.sp else 26.sp,
            ),
            cursorBrush = SolidColor(Color.White),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (text.isEmpty()) {
                        Text(
                            if (isTextOnly) "Type a status" else "Add a caption…",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = if (isTextOnly) 32.sp else 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    inner()
                }
            },
        )

        // Bottom: colours + send
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isTextOnly && showColors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                ) {
                    StatusColors.forEachIndexed { index, color ->
                        val selected = index == colorIndex
                        Box(
                            modifier = Modifier
                                .size(if (selected) 34.dp else 28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selected) Modifier.border(2.5.dp, Color.White, CircleShape)
                                    else Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
                                )
                                .clickable { colorIndex = index },
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Disappears after 24 hours",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    enabled = canSend && !busy,
                    onClick = {
                        if (!canSend || busy) return@IconButton
                        if (!useRemote) {
                            Toast.makeText(context, "Sign in to post a status", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }
                        busy = true
                        scope.launch {
                            val ok = StatusRepository.post(
                                context = context,
                                api = api,
                                text = text,
                                background = if (isTextOnly) StatusColorHex.getOrNull(colorIndex) else null,
                                mediaUri = mediaUri,
                            ) != null
                            busy = false
                            if (ok) onNavigate(Routes.Home)
                            else Toast.makeText(context, "Could not post status", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) Color.White else Color.White.copy(alpha = 0.45f),
                        ),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = AivoraColors.Primary,
                        )
                    } else {
                        Icon(
                            AppIcons.Send,
                            contentDescription = "Send",
                            tint = AivoraColors.Primary,
                            modifier = Modifier.size(26.dp),
                        )
                    }
                }
            }
        }
    }
}
