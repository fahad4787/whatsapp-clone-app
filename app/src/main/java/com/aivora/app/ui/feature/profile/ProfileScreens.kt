package com.aivora.app.ui.feature.profile

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.CallStartBody
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.feature.chat.ChatControls
import com.aivora.app.ui.feature.chat.DisappearingMessagesDialog
import com.aivora.app.ui.feature.chat.formatDisappearing
import com.aivora.app.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun ProfileIdScreen(
    profileId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val chats by InMemoryChatStore.chats.collectAsState()
    val chat = remember(profileId, chats) {
        InMemoryChatStore.chat(profileId.ifBlank { "1" })
    }
    var showDisappearing by remember { mutableStateOf(false) }
    var showWallpaper by remember { mutableStateOf(false) }
    var showClearChat by remember { mutableStateOf(false) }

    if (chat.isGroup) {
        com.aivora.app.ui.feature.chat.GroupInfoScreen(
            chatId = chat.id,
            onBack = onBack,
            onNavigate = onNavigate,
        )
        return
    }

    fun startCall(media: String) {
        if (!useRemote) {
            onNavigate(if (media == "video") Routes.videoCall(chat.title) else Routes.audioCall(chat.title))
            return
        }
        scope.launch {
            Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()
            val result = runCatching {
                api.postCalls(CallStartBody(conversationId = chat.id, media = media))
            }.onFailure {
                Toast.makeText(context, "Call failed: ${it.message ?: "error"}", Toast.LENGTH_LONG).show()
            }.getOrNull() ?: return@launch
            val token = result.token
            val url = result.livekitUrl
            val callId = result.call?.id
            if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                Toast.makeText(context, "Call failed: bad response", Toast.LENGTH_LONG).show()
                return@launch
            }
            ActiveCallSession.setActive(
                callId = callId,
                token = token,
                livekitUrl = url,
                media = media,
                peerName = chat.title,
                asCaller = true,
            )
            onNavigate(
                if (media == "video") Routes.videoCall(chat.title) else Routes.audioCall(chat.title),
            )
        }
    }

    AivoraScaffold(title = "Contact info", onBack = onBack) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar(chat.title, size = 96)
            Spacer(Modifier.height(12.dp))
            Text(chat.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                chat.peerPhone?.takeIf { it.isNotBlank() } ?: "Phone hidden",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Audio call", null, AppIcons.Call, onClick = { startCall("audio") }),
                    SettingsItem("Video call", null, AppIcons.Videocam, onClick = { startCall("video") }),
                    SettingsItem("Schedule call", null, AppIcons.Event, onClick = {
                        onNavigate(Routes.ScheduledCalls)
                    }),
                    SettingsItem("Media, links, and docs", null, AppIcons.Photo, onClick = {
                        onNavigate(Routes.chatMedia(profileId))
                    }),
                    SettingsItem("Starred messages", null, AppIcons.Star, onClick = {
                        onNavigate(Routes.SettingsStarred)
                    }),
                    SettingsItem(
                        if (chat.isMuted) "Unmute" else "Mute notifications",
                        null,
                        if (chat.isMuted) AppIcons.NotificationsActive else AppIcons.NotificationsOff,
                        onClick = {
                            scope.launch {
                                val ok = ChatControls.toggleMute(api, useRemote, chat)
                                if (!ok) {
                                    Toast.makeText(context, "Could not update mute", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    ),
                    SettingsItem(
                        if (chat.pinned) "Unpin chat" else "Pin chat",
                        null,
                        AppIcons.PushPin,
                        onClick = {
                            scope.launch {
                                val ok = ChatControls.togglePin(api, useRemote, chat)
                                if (!ok) {
                                    Toast.makeText(context, "Could not pin (max 3)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    ),
                    SettingsItem(
                        if (chat.favorite) "Remove from favourites" else "Add to favourites",
                        null,
                        if (chat.favorite) AppIcons.Star else AppIcons.StarBorder,
                        onClick = {
                            scope.launch {
                                val ok = ChatControls.toggleFavorite(api, useRemote, chat)
                                if (!ok) {
                                    Toast.makeText(context, "Could not update favourites", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    ),
                    SettingsItem(
                        if (chat.archived) "Unarchive chat" else "Archive chat",
                        null,
                        if (chat.archived) AppIcons.Unarchive else AppIcons.Archive,
                        onClick = {
                            scope.launch {
                                val ok = ChatControls.toggleArchive(api, useRemote, chat)
                                if (ok && !chat.archived) onBack?.invoke()
                                else if (!ok) {
                                    Toast.makeText(context, "Could not archive", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                    ),
                    SettingsItem("Chat wallpaper", null, AppIcons.Wallpaper, onClick = {
                        showWallpaper = true
                    }),
                    SettingsItem(
                        "Disappearing messages",
                        icon = AppIcons.Timer,
                        trailing = formatDisappearing(chat.disappearingSeconds),
                        onClick = { showDisappearing = true },
                    ),
                    SettingsItem(
                        "Advanced chat privacy",
                        null,
                        AppIcons.Security,
                        onClick = { onNavigate(Routes.SettingsPrivacyAdvanced) },
                    ),
                    SettingsItem(
                        "Encryption",
                        "Messages and calls are end-to-end encrypted.",
                        AppIcons.Lock,
                        onClick = { },
                    ),
                    SettingsItem("Clear chat", null, AppIcons.Delete, danger = true, onClick = {
                        showClearChat = true
                    }),
                    if (chat.blockedByMe) {
                        SettingsItem("Unblock", null, AppIcons.Block, onClick = {
                            scope.launch {
                                val ok = ChatControls.unblock(api, useRemote, chat)
                                Toast.makeText(
                                    context,
                                    if (ok) "Contact unblocked" else "Could not unblock",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        })
                    } else {
                        SettingsItem("Block", null, AppIcons.Block, danger = true, onClick = {
                            scope.launch {
                                val ok = ChatControls.block(api, useRemote, chat)
                                Toast.makeText(
                                    context,
                                    if (ok) "Contact blocked" else "Could not block",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        })
                    },
                    SettingsItem("Report", null, AppIcons.Info, danger = true, onClick = {
                        scope.launch {
                            runCatching {
                                api.postReports(
                                    com.aivora.app.data.model.ReportRequestBody(
                                        targetUserId = chat.peerUserId,
                                        targetConversationId = chat.id,
                                        reason = "Reported from contact info",
                                        messageIds = emptyList(),
                                        block = false,
                                    ),
                                )
                            }
                            Toast.makeText(context, "Report sent", Toast.LENGTH_SHORT).show()
                        }
                    }),
                ),
            ),
        )
    }
    if (showDisappearing) {
        DisappearingMessagesDialog(
            chatId = chat.id,
            currentSeconds = chat.disappearingSeconds,
            onDismiss = { showDisappearing = false },
        )
    }
    if (showWallpaper) {
        com.aivora.app.ui.feature.chat.ChatWallpaperDialog(
            selected = chat.wallpaper,
            onPick = { key ->
                showWallpaper = false
                scope.launch {
                    val ok = ChatControls.setWallpaper(api, useRemote, chat, key)
                    if (!ok) {
                        Toast.makeText(context, "Could not save wallpaper", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showWallpaper = false },
        )
    }
    if (showClearChat) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showClearChat = false },
            title = { Text("Clear this chat?") },
            text = { Text("Messages will be removed from this device.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showClearChat = false
                        InMemoryChatStore.clearChat(chat.id, context)
                        Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                        onBack?.invoke()
                    },
                ) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showClearChat = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
