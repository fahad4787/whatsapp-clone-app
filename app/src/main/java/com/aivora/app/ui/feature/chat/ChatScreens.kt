package com.aivora.app.ui.feature.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.TypingIndicatorBubble
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.motion.AivoraTweenFast
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatIdScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val haptics = rememberAivoraHaptics()
    val chat = remember(chatId) { InMemoryChatStore.chat(chatId) }
    val messages by InMemoryChatStore.messages(chatId).collectAsState()
    var text by remember { mutableStateOf("") }
    var peerTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val media = rememberMediaActions(context) { uri ->
        haptics.confirm()
        InMemoryChatStore.sendMedia(chatId, uri.toString())
        peerTyping = true
    }

    LaunchedEffect(chatId) {
        InMemoryChatStore.markRead(chatId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex + if (peerTyping) 1 else 0)
        }
    }

    LaunchedEffect(peerTyping) {
        if (!peerTyping) return@LaunchedEffect
        delay(1400)
        peerTyping = false
        delay(200)
        InMemoryChatStore.sendText(chatId, "Got it 👍", mine = false)
    }

    val avatarModifier = sharedAvatarModifier(
        sharedTransitionScope,
        animatedVisibilityScope,
        chatId,
    )

    AivoraScaffold(
        title = chat.title,
        onBack = {
            haptics.tick()
            onBack?.invoke()
        },
        titleContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(chat.title, size = 36, modifier = avatarModifier)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        chat.title,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    AnimatedVisibility(visible = peerTyping, enter = fadeIn(AivoraTweenFast), exit = fadeOut(AivoraTweenFast)) {
                        Text(
                            "typing…",
                            style = MaterialTheme.typography.labelMedium,
                            color = AivoraColors.Primary,
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(onClick = { onNavigate(Routes.NewCall) }) {
                Icon(Icons.Default.Videocam, contentDescription = "Video call")
            }
            IconButton(onClick = { onNavigate(Routes.NewCall) }) {
                Icon(Icons.Default.Call, contentDescription = "Voice call")
            }
            IconButton(onClick = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (dark) AivoraColors.ChatWallpaperDark else AivoraColors.ChatWallpaperLight,
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                ),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.mine) Arrangement.End else Arrangement.Start,
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 320.dp)
                                .clip(bubbleShape(msg.mine))
                                .background(
                                    if (msg.mine) AivoraColors.BubbleMine
                                    else if (dark) AivoraColors.BubbleOtherDark
                                    else AivoraColors.BubbleOtherLight,
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            msg.mediaUri?.let { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Photo",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .padding(bottom = 6.dp),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                            Text(
                                msg.body,
                                color = if (msg.mine) AivoraColors.OnPrimary
                                else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(
                                msg.time,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (msg.mine) AivoraColors.OnPrimary.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                            )
                        }
                    }
                }
                if (peerTyping) {
                    item(key = "typing") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }
        }

        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onNavigate(Routes.EventCreateChat.replace("{chat}", chatId)) }) {
                    Icon(Icons.Default.Add, contentDescription = "Attach", tint = AivoraColors.Primary)
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    shape = RoundedCornerShape(24.dp),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = media.pickGallery) {
                                Icon(Icons.Default.AttachFile, contentDescription = "Gallery")
                            }
                            IconButton(onClick = media.takePhoto) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            }
                        }
                    },
                )
                AnimatedContent(
                    targetState = text.isNotBlank(),
                    transitionSpec = {
                        (fadeIn(AivoraTweenFast) + scaleIn(initialScale = 0.85f, animationSpec = AivoraTweenFast)) togetherWith
                            (fadeOut(AivoraTweenFast) + scaleOut(targetScale = 0.85f, animationSpec = AivoraTweenFast))
                    },
                    label = "sendMic",
                ) { canSend ->
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        IconButton(
                            onClick = {
                                val body = text.trim()
                                if (body.isNotEmpty()) {
                                    haptics.confirm()
                                    InMemoryChatStore.sendText(chatId, body)
                                    text = ""
                                    peerTyping = true
                                } else {
                                    haptics.tick()
                                }
                            },
                        ) {
                            Icon(
                                if (canSend) Icons.Default.Send else Icons.Default.Mic,
                                contentDescription = if (canSend) "Send" else "Voice",
                                tint = AivoraColors.OnPrimary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun sharedAvatarModifier(
    scope: SharedTransitionScope?,
    visibility: AnimatedVisibilityScope?,
    chatId: String,
): Modifier {
    if (scope == null || visibility == null) return Modifier
    return with(scope) {
        Modifier.sharedElement(
            state = rememberSharedContentState(key = "chat-avatar-$chatId"),
            animatedVisibilityScope = visibility,
        )
    }
}

private fun bubbleShape(mine: Boolean) = RoundedCornerShape(
    topStart = 18.dp,
    topEnd = 18.dp,
    bottomStart = if (mine) 18.dp else 4.dp,
    bottomEnd = if (mine) 4.dp else 18.dp,
)
