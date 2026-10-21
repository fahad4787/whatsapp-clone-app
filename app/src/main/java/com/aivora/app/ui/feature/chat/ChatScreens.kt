package com.aivora.app.ui.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.TypingIndicatorBubble
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.motion.AivoraNavSlideMs
import com.aivora.app.ui.motion.AivoraTweenFast
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
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
    val density = LocalDensity.current
    val haptics = rememberAivoraHaptics()
    val chat = remember(chatId) { InMemoryChatStore.chat(chatId) }
    val messages by InMemoryChatStore.messages(chatId).collectAsState()
    var text by remember { mutableStateOf("") }
    var peerTyping by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val wallpaper = if (dark) AivoraColors.ChatWallpaperDark else AivoraColors.ChatWallpaperLight
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val media = rememberMediaActions(context) { uri ->
        haptics.confirm()
        InMemoryChatStore.sendMedia(chatId, uri.toString())
        peerTyping = true
    }
    val firstName = chat.title.substringBefore(' ').ifBlank { chat.title }
    val subtitle = when {
        peerTyping -> "typing…"
        chat.isGroup -> "3 online · 5 members"
        else -> "Online now"
    }

    LaunchedEffect(chatId) {
        InMemoryChatStore.markRead(chatId)
    }

    // Defer list settle until after slide enter so they don't fight.
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(chatId) {
        entered = false
        delay(AivoraNavSlideMs.toLong() + 20L)
        entered = true
    }
    LaunchedEffect(messages.size, peerTyping, imeVisible, entered) {
        if (messages.isEmpty()) return@LaunchedEffect
        val index = (messages.lastIndex + if (peerTyping) 1 else 0).coerceAtLeast(0)
        if (entered) listState.animateScrollToItem(index)
        else listState.scrollToItem(index)
    }

    LaunchedEffect(peerTyping) {
        if (!peerTyping) return@LaunchedEffect
        delay(1400)
        peerTyping = false
        delay(200)
        InMemoryChatStore.sendText(chatId, "Got it 👍", mine = false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding(),
    ) {
        ChatHeader(
            title = chat.title,
            subtitle = subtitle,
            showTyping = peerTyping,
            onBack = {
                haptics.tick()
                onBack?.invoke()
            },
            onCall = { onNavigate(Routes.audioCall(chat.title)) },
            onVideo = { onNavigate(Routes.videoCall(chat.title)) },
            onOpenProfile = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(wallpaper),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            ) {
                item(key = "date") {
                    DateSeparator(label = "Today")
                }
                items(messages, key = { it.id }) { msg ->
                    MessageRow(
                        message = msg,
                        isGroup = chat.isGroup,
                        dark = dark,
                    )
                }
                if (peerTyping) {
                    item(key = "typing") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                        ) {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }
        }

        ChatComposer(
            value = text,
            onValueChange = { text = it },
            placeholder = "Message $firstName…",
            onAttach = media.pickGallery,
            onCamera = media.takePhoto,
            onSendOrMic = {
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
        )
    }
}

@Composable
private fun ChatHeader(
    title: String,
    subtitle: String,
    showTyping: Boolean,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onVideo: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, bottom = 8.dp)
            .height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(AivoraIcons.Back),
                contentDescription = "Back",
                tint = AivoraColors.Ink,
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenProfile),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Avatar(title, size = 48)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 2.dp, bottom = 2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary)
                        .border(1.5.dp, Color.White, CircleShape),
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AivoraColors.InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(AivoraTweenFast),
                    exit = fadeOut(AivoraTweenFast),
                ) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (showTyping) AivoraColors.Primary else AivoraColors.Online,
                    )
                }
            }
        }
        IconButton(onClick = onCall) {
            Icon(
                painter = painterResource(AivoraIcons.Call),
                contentDescription = "Voice call",
                tint = AivoraColors.Primary,
            )
        }
        IconButton(onClick = onVideo) {
            Icon(
                painter = painterResource(AivoraIcons.Video),
                contentDescription = "Video call",
                tint = AivoraColors.Primary,
            )
        }
    }
    HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
}

@Composable
private fun DateSeparator(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AivoraColors.ChatDivider,
        )
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = AivoraColors.TextSecondary,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AivoraColors.ChatDivider,
        )
    }
}

@Composable
private fun MessageRow(
    message: LocalMessage,
    isGroup: Boolean,
    dark: Boolean,
) {
    val mine = message.mine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (!mine) {
            Avatar(message.senderName ?: "Peer", size = 16)
            Spacer(modifier = Modifier.width(4.dp))
        }
        val bubbleBg = when {
            mine -> AivoraColors.BubbleMine
            dark -> AivoraColors.BubbleOtherDark
            else -> AivoraColors.BubbleOtherLight
        }
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(bubbleShape(mine))
                .background(bubbleBg)
                .then(
                    if (!mine && !dark) {
                        Modifier.border(1.dp, AivoraColors.ChatDivider, bubbleShape(mine))
                    } else {
                        Modifier
                    },
                )
                .padding(
                    horizontal = if (message.mediaUri != null) 8.dp else 14.dp,
                    vertical = if (message.mediaUri != null) 8.dp else 10.dp,
                ),
        ) {
            if (!mine && isGroup && !message.senderName.isNullOrBlank()) {
                Text(
                    message.senderName,
                    color = AivoraColors.Primary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            message.mediaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 196.dp)
                        .clip(RoundedCornerShape(AivoraDimens.RadiusMd)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (message.body.isNotBlank()) {
                Text(
                    message.body,
                    color = AivoraColors.Ink,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = if (message.mediaUri != null) 4.dp else 0.dp),
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = if (message.mediaUri != null) 4.dp else 0.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    message.time,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = AivoraColors.TextSecondary,
                )
                if (mine) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(AivoraIcons.TicksRead),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(width = 16.dp, height = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onSendOrMic: () -> Unit,
) {
    val canSend = value.isNotBlank()
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(AivoraColors.ComposerFill)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onAttach, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(AivoraIcons.Attach),
                        contentDescription = "Attach",
                        tint = AivoraColors.TextSecondary,
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = AivoraColors.Ink),
                    cursorBrush = SolidColor(AivoraColors.Primary),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AivoraColors.TextSecondary,
                                )
                            }
                            inner()
                        }
                    },
                )
                IconButton(onClick = onCamera, modifier = Modifier.size(40.dp)) {
                    Icon(
                        painter = painterResource(AivoraIcons.Camera),
                        contentDescription = "Camera",
                        tint = AivoraColors.TextSecondary,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(AivoraColors.Primary, AivoraColors.PrimaryDark),
                        ),
                    )
                    .clickable(onClick = onSendOrMic),
                contentAlignment = Alignment.Center,
            ) {
                if (canSend) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = AivoraColors.OnPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                } else {
                    Icon(
                        painter = painterResource(AivoraIcons.MicFab),
                        contentDescription = "Voice",
                        tint = AivoraColors.OnPrimary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        }
    }
}

private fun bubbleShape(mine: Boolean) = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = if (mine) 16.dp else 4.dp,
    bottomEnd = if (mine) 4.dp else 16.dp,
)
