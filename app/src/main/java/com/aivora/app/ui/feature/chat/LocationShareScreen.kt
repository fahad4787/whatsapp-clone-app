package com.aivora.app.ui.feature.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.LocationBody
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationShareScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val chat = remember(chatId) { InMemoryChatStore.chat(chatId) }
    val peer = chat.title.substringBefore(" ").ifBlank { "contact" }
    var query by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Share location", fontWeight = FontWeight.SemiBold)
                    Text(
                        "LOCATION · $peer",
                        style = MaterialTheme.typography.labelMedium,
                        color = AivoraColors.TextSecondary,
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                TextButton(onClick = { onBack?.invoke() }) {
                    Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AivoraColors.LightBackground),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Text("Find a place", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search places or addresses") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AivoraDimens.RadiusMd),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AivoraColors.SurfaceMuted,
                    focusedContainerColor = AivoraColors.SurfaceMuted,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AivoraColors.Primary,
                ),
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE8F0E9), Color(0xFFD6E4F0), Color(0xFFF0F0F0)),
                        ),
                    ),
            ) {
                // Sample map shell
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary),
                    )
                }
                Text(
                    "Willowbrook · Sample map",
                    style = MaterialTheme.typography.labelSmall,
                    color = AivoraColors.Ink,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = AivoraColors.Ink)
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted),
            ) {
                SettingsRow(
                    SettingsItem(
                        "Send current location",
                        subtitle = "Lahore · demo coordinates",
                        icon = Icons.Default.NearMe,
                        onClick = {
                            val uid = SessionStore.userId(context)
                            if (SessionStore.accessToken(context).isNullOrBlank() || uid == null) {
                                InMemoryChatStore.sendText(chatId, "📍 Current location · Lahore")
                                onBack?.invoke()
                            } else if (!sending) {
                                sending = true
                                scope.launch {
                                    runCatching {
                                        NetworkModule.api().postConversationsIdMessages(
                                            chatId,
                                            SendMessageBody(
                                                clientId = UUID.randomUUID().toString(),
                                                location = LocationBody(
                                                    lat = 31.5204,
                                                    lng = 74.3587,
                                                    name = "Current location",
                                                ),
                                            ),
                                        )
                                    }.onSuccess {
                                        InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
                                        onBack?.invoke()
                                    }.onFailure {
                                        sending = false
                                        Toast.makeText(context, "Could not send location", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted),
            ) {
                SettingsRow(
                    SettingsItem(
                        "Share live location",
                        subtitle = "Let $peer see your location for a while",
                        icon = Icons.Default.ShareLocation,
                        onClick = {
                            Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                        },
                    ),
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
