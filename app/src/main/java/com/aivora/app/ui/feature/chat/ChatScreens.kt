package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.filled.Unarchive
import android.Manifest
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ConcatAdapter
import coil3.compose.AsyncImage
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.api.AivoraApi
import com.aivora.app.ui.media.rememberBubbleImageModel
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.LocalMediaKind
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.LocalReplyPreview
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.EditMessageBody
import com.aivora.app.data.model.EmptyRequestBody
import com.aivora.app.data.model.ForwardMessageBody
import com.aivora.app.data.model.ReactionBody
import com.aivora.app.data.model.ReplyPreviewDto
import com.aivora.app.data.model.RsvpBody
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.model.VoteBody
import com.aivora.app.data.realtime.RealtimeClient
import com.aivora.app.data.sendAttachmentMessage
import com.aivora.app.data.sendImageMessage
import com.aivora.app.data.sendVoiceFile
import com.aivora.app.data.toLocalChat
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraOverflowMenu
import com.aivora.app.ui.components.AivoraSheetAction
import com.aivora.app.ui.components.AivoraSheetActionGrid
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.components.OverflowMenuItem
import com.aivora.app.ui.components.TypingIndicatorBubble
import com.aivora.app.ui.perf.ChatHistoryLoaderAdapter
import com.aivora.app.ui.perf.ChatMessageAdapter
import com.aivora.app.ui.perf.ChatOlderScrollListener
import com.aivora.app.ui.perf.ChatRecyclerHost
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.media.VoiceRecorder
import com.aivora.app.ui.media.hasMicPermission
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.motion.AivoraTweenFast
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID

private val ReactionEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

@Composable
fun ChatIdScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val api = remember { ChatRepository.api() }
    val dark = isSystemInDarkTheme()
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptics = rememberAivoraHaptics()
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    // Disk hydrate on IO — never MessageCache.load on Main during composition.
    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
        if (InMemoryChatStore.messages(chatId).value.isNotEmpty()) return@LaunchedEffect
        ChatRepository.hydrateMessagesFromDiskAsync(context, chatId)
    }
    val chatFlow = remember(chatId) { InMemoryChatStore.chatFlow(chatId) }
    val chat by chatFlow.collectAsState(initial = InMemoryChatStore.chat(chatId))
    // PERF H1: messages collected inside ChatMessageList — not here — so emits don't
    // restart header / menus / composer / dialogs.
    var searchQuery by remember(chatId) { mutableStateOf("") }
    var showSearch by remember(chatId) { mutableStateOf(false) }
    var peerTyping by remember(chatId) { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showClearChat by remember { mutableStateOf(false) }
    var showExportChat by remember { mutableStateOf(false) }
    var exportBusy by remember { mutableStateOf(false) }
    var actionTarget by remember { mutableStateOf<LocalMessage?>(null) }
    var deleteTarget by remember { mutableStateOf<LocalMessage?>(null) }
    var reactTarget by remember { mutableStateOf<LocalMessage?>(null) }
    var forwardTarget by remember { mutableStateOf<LocalMessage?>(null) }
    var editTarget by remember { mutableStateOf<LocalMessage?>(null) }
    var editText by remember { mutableStateOf("") }
    var showWallpaper by remember { mutableStateOf(false) }
    // Message + url currently shown in the one-time viewer.
    var viewOnceShown by remember { mutableStateOf<Pair<LocalMessage, String>?>(null) }
    var replyTo by remember(chatId) { mutableStateOf<LocalMessage?>(null) }
    var myUserId by remember { mutableStateOf(SessionStore.userId(context)) }
    val wallpaper = chatWallpaperColor(chat.wallpaper, dark)
    fun uploadPickedUri(uri: Uri, viewOnce: Boolean = false) {
        haptics.confirm()
        val mime = context.contentResolver.getType(uri).orEmpty()
        val kind = when {
            mime.startsWith("video/") -> LocalMediaKind.Video
            mime.startsWith("audio/") -> LocalMediaKind.Audio
            mime.startsWith("image/") -> LocalMediaKind.Image
            else -> LocalMediaKind.File
        }
        if (useRemote) {
            scope.launch {
                try {
                    val sent = when (kind) {
                        LocalMediaKind.Image -> api.sendImageMessage(context, chatId, uri, viewOnce = viewOnce)
                        else -> api.sendAttachmentMessage(
                            context = context,
                            conversationId = chatId,
                            uri = uri,
                            defaultName = if (kind == LocalMediaKind.Video) "video.mp4" else "document.bin",
                            defaultMime = mime.ifBlank { "application/octet-stream" },
                        )
                    }
                    val uid = myUserId ?: SessionStore.userId(context)
                    if (uid != null) {
                        InMemoryChatStore.upsertMessage(
                            chatId,
                            sent.toLocalMessage(uid).copy(mediaUri = uri.toString()),
                        )
                    }
                } catch (_: Exception) {
                    InMemoryChatStore.sendMedia(chatId, uri.toString(), kind = kind)
                }
            }
        } else {
            InMemoryChatStore.sendMedia(chatId, uri.toString(), kind = kind)
            peerTyping = true
        }
    }
    val media = rememberMediaActions(
        context = context,
        onDocument = { uri -> uploadPickedUri(uri) },
        onImage = { uri -> uploadPickedUri(uri) },
    )
    val pickViewOnce = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) uploadPickedUri(uri, viewOnce = true) }
    val voiceRecorder = remember { VoiceRecorder(context) }
    var recordingVoice by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose { voiceRecorder.cancel() }
    }
    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && voiceRecorder.start()) {
            recordingVoice = true
        } else {
            Toast.makeText(context, "Microphone permission needed", Toast.LENGTH_SHORT).show()
        }
    }
    fun finishVoiceSend() {
        haptics.confirm()
        val result = voiceRecorder.stop()
        recordingVoice = false
        if (result == null) return
        val (file, secs) = result
        if (useRemote) {
            scope.launch {
                try {
                    val sent = api.sendVoiceFile(chatId, file, durationSec = secs)
                    val uid = myUserId ?: SessionStore.userId(context)
                    if (uid != null) {
                        InMemoryChatStore.upsertMessage(chatId, sent.toLocalMessage(uid))
                    }
                } catch (_: Exception) {
                    InMemoryChatStore.sendVoice(
                        chatId,
                        durationSec = secs,
                        uri = Uri.fromFile(file).toString(),
                    )
                } finally {
                    file.delete()
                }
            }
        } else {
            InMemoryChatStore.sendVoice(
                chatId,
                durationSec = secs,
                uri = Uri.fromFile(file).toString(),
            )
        }
    }
    val firstName = chat.title.substringBefore(' ').ifBlank { chat.title }
    val hidePresence = chat.blockedByMe || chat.blockedMe
    val peerOnlineRaw by RealtimeClient.observeUserOnline(chat.peerUserId)
        .collectAsState(initial = RealtimeClient.isUserOnline(chat.peerUserId))
    val groupOnlineCount by RealtimeClient.observeRoomOnlineCount(chatId, myUserId)
        .collectAsState(initial = RealtimeClient.onlineCountInChat(chatId, myUserId))
    val peerOnline = !hidePresence && !chat.isGroup && chat.peerPresenceVisible && peerOnlineRaw
    var lastSeenLocal by remember(chatId) { mutableStateOf(chat.peerLastSeenAt) }
    LaunchedEffect(chat.peerLastSeenAt) { lastSeenLocal = chat.peerLastSeenAt }
    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
        RealtimeClient.presence.collect { event ->
            if (event.userId != chat.peerUserId) return@collect
            if (!event.online && !event.lastSeenAt.isNullOrBlank()) {
                lastSeenLocal = event.lastSeenAt
            }
        }
    }
    val subtitle = when {
        hidePresence -> ""
        peerTyping -> "typing…"
        chat.isGroup -> when {
            groupOnlineCount > 0 -> "$groupOnlineCount online · tap for info"
            else -> "tap for group info"
        }
        !chat.peerPresenceVisible -> ""
        peerOnline -> "Online"
        !lastSeenLocal.isNullOrBlank() -> formatLastSeen(lastSeenLocal!!)
        else -> ""
    }

    DisposableEffect(chatId) {
        InMemoryChatStore.setViewingChat(chatId)
        onDispose {
            InMemoryChatStore.persistMessages(context, chatId)
            InMemoryChatStore.setViewingChat(null)
        }
    }

    LaunchedEffect(Unit) {
        if (!useRemote) return@LaunchedEffect
        RealtimeClient.startFromSession(context)
        if (myUserId == null) {
            try {
                val me = api.getMe()
                me.id?.let {
                    SessionStore.saveUserId(context, it)
                    myUserId = it
                }
            } catch (_: Exception) { /* offline */ }
        }
    }

    // Join + typing after first paint — open must not wait on WS.
    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
        delay(80)
        RealtimeClient.join(chatId)
        try {
            RealtimeClient.typing.collect { event ->
                if (event.conversationId == chatId) {
                    peerTyping = event.typing
                }
            }
        } finally {
            RealtimeClient.leave(chatId)
            RealtimeClient.setTyping(chatId, false)
        }
    }

    // Meta refresh only when home row is unknown — avoid open hitch when title already painted.
    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
        val known = InMemoryChatStore.chats.value.any { it.id == chatId && it.title != "Chat" }
        if (known) delay(2_000) else delay(400)
        runCatching {
            val remote = api.getConversationsId(chatId)
            // Don't bump this chat to #1 on the home list while opening.
            InMemoryChatStore.upsertChat(remote.toLocalChat(), promote = false)
        }
    }
    LaunchedEffect(chatId, myUserId, useRemote) {
        if (!useRemote || myUserId == null) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val empty = InMemoryChatStore.messages(chatId).value.isEmpty()
            if (empty) {
                // Cold open: need network for first paint fill.
                ChatRepository.refreshMessages(context, chatId, myUserId!!)
            } else if (!RealtimeClient.connected.value) {
                // Warm + WS down: deferred catch-up merge (silent when unchanged).
                delay(900)
                ChatRepository.refreshMessages(context, chatId, myUserId!!)
            }
            // Warm + WS up: skip pull — socket + backup poll below are enough.
            while (true) {
                delay(30_000)
                if (RealtimeClient.connected.value) continue
                ChatRepository.refreshMessages(context, chatId, myUserId!!)
            }
        }
    }
    LaunchedEffect(peerTyping) {
        if (!peerTyping || useRemote) return@LaunchedEffect
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
        // Header owns space; list is Views-only RecyclerView below (no nested ComposeView).
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                ChatHeader(
                    title = chat.title,
                    subtitle = subtitle,
                    showTyping = peerTyping,
                    menuExpanded = showChatMenu,
                    onMenuExpandedChange = { showChatMenu = it },
                    onBack = {
                        haptics.tick()
                        onBack?.invoke()
                    },
                    onCall = {
                        if (!useRemote) {
                            onNavigate(Routes.audioCall(chat.title))
                        } else {
                            scope.launch {
                                Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()
                                val result = runCatching {
                                    api.postCalls(
                                        com.aivora.app.data.model.CallStartBody(
                                            conversationId = chatId,
                                            media = "audio",
                                        ),
                                    )
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Call failed: ${it.message ?: "error"}",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }.getOrNull() ?: return@launch
                                val token = result.token
                                val url = result.livekitUrl
                                val callId = result.call?.id
                                if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                                    Toast.makeText(context, "Call failed: bad response", Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                com.aivora.app.data.call.ActiveCallSession.setActive(
                                    callId = callId,
                                    token = token,
                                    livekitUrl = url,
                                    media = "audio",
                                    peerName = chat.title,
                                    asCaller = true,
                                )
                                onNavigate(Routes.audioCall(chat.title))
                            }
                        }
                    },
                    onVideo = {
                        if (!useRemote) {
                            onNavigate(Routes.videoCall(chat.title))
                        } else {
                            scope.launch {
                                Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()
                                val result = runCatching {
                                    api.postCalls(
                                        com.aivora.app.data.model.CallStartBody(
                                            conversationId = chatId,
                                            media = "video",
                                        ),
                                    )
                                }.onFailure {
                                    Toast.makeText(
                                        context,
                                        "Call failed: ${it.message ?: "error"}",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }.getOrNull() ?: return@launch
                                val token = result.token
                                val url = result.livekitUrl
                                val callId = result.call?.id
                                if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                                    Toast.makeText(context, "Call failed: bad response", Toast.LENGTH_LONG).show()
                                    return@launch
                                }
                                com.aivora.app.data.call.ActiveCallSession.setActive(
                                    callId = callId,
                                    token = token,
                                    livekitUrl = url,
                                    media = "video",
                                    peerName = chat.title,
                                    asCaller = true,
                                )
                                onNavigate(Routes.videoCall(chat.title))
                            }
                        }
                    },
                    onOpenProfile = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
                    menuItems = ChatControls.headerOverflowMenuItems(
                        chat = chat,
                        scope = scope,
                        context = context,
                        api = api,
                        useRemote = useRemote,
                        onDismiss = { showChatMenu = false },
                        onSearch = { showSearch = true },
                        onClear = { showClearChat = true },
                        onViewInfo = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
                        onMedia = { onNavigate(Routes.chatMedia(chatId)) },
                        onWallpaper = { showWallpaper = true },
                    ),
                )
                if (showSearch) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(AivoraColors.ComposerFill)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = AivoraColors.Ink),
                            cursorBrush = SolidColor(AivoraColors.Primary),
                            decorationBox = { inner ->
                                Box {
                                    if (searchQuery.isEmpty()) {
                                        Text("Search in chat", color = AivoraColors.TextSecondary)
                                    }
                                    inner()
                                }
                            },
                        )
                        TextButton(onClick = {
                            showSearch = false
                            searchQuery = ""
                        }) {
                            Text("Close", color = AivoraColors.Primary)
                        }
                    }
                }
            }
            val listIsGroup = chat.isGroup
            val listBlockedMe = chat.blockedMe
            ChatMessageList(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(wallpaper)
                    .clipToBounds(),
                chatId = chatId,
                useRemote = useRemote,
                myUserId = myUserId,
                isGroup = listIsGroup,
                blockedMe = listBlockedMe,
                dark = dark,
                peerTyping = peerTyping,
                peerTitle = chat.title,
                peerAvatarUrl = chat.avatarUrl,
                searchQuery = searchQuery,
                showSearch = showSearch,
                api = api,
                coverHeightPx = 0,
                onLongPress = { msg ->
                    haptics.tick()
                    actionTarget = msg
                },
                onViewOnceShown = { msg, url -> viewOnceShown = msg to url },
            )
        }

        if (chat.blockedByMe) {
            // WhatsApp-style: composer replaced by Unblock.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            ) {
                HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
                TextButton(
                    onClick = {
                        scope.launch {
                            val ok = ChatControls.unblock(api, useRemote, chat)
                            Toast.makeText(
                                context,
                                if (ok) "Contact unblocked" else "Could not unblock",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                ) {
                    Text(
                        "Unblock",
                        color = AivoraColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }
        } else if (chat.isGroup && chat.onlyAdminsCanSend && !chat.isGroupAdmin) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            ) {
                HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
                Text(
                    "Only admins can send messages",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        } else {
            ChatComposer(
                chatId = chatId,
                useRemote = useRemote,
                placeholder = "Message $firstName…",
                replyPreview = replyTo?.let { r ->
                    if (r.deletedForEveryone) "Original message was deleted" else r.body.ifBlank { "Message" }
                },
                onClearReply = { replyTo = null },
                recording = recordingVoice,
                voiceRecorder = voiceRecorder,
                onCancelRecording = {
                    voiceRecorder.cancel()
                    recordingVoice = false
                },
                onAttach = {
                    focusManager.clearFocus(force = true)
                    keyboard?.hide()
                    showAttach = true
                },
                onCamera = { onNavigate(Routes.camera("chat:$chatId")) },
                onSendText = { body ->
                    haptics.confirm()
                    val quoting = replyTo
                    replyTo = null
                    val replyDto = quoting?.let {
                        ReplyPreviewDto(id = it.id, text = it.body.takeIf { t -> t.isNotBlank() })
                    }
                    val localReply = quoting?.let {
                        LocalReplyPreview(
                            id = it.id,
                            body = if (it.deletedForEveryone) "Original message was deleted" else it.body,
                            deleted = it.deletedForEveryone,
                        )
                    }
                    if (useRemote) {
                        RealtimeClient.setTyping(chatId, false)
                        scope.launch {
                            try {
                                val sent = api.postConversationsIdMessages(
                                    chatId,
                                    SendMessageBody(
                                        text = body,
                                        clientId = UUID.randomUUID().toString(),
                                        replyTo = replyDto,
                                    ),
                                )
                                val uid = myUserId ?: SessionStore.userId(context)
                                if (uid != null) {
                                    InMemoryChatStore.upsertMessage(chatId, sent.toLocalMessage(uid))
                                }
                            } catch (_: Exception) {
                                InMemoryChatStore.sendText(chatId, body, replyTo = localReply)
                            }
                        }
                    } else {
                        InMemoryChatStore.sendText(chatId, body, replyTo = localReply)
                        peerTyping = true
                    }
                },
                onRequestMic = {
                    haptics.confirm()
                    if (context.hasMicPermission()) {
                        if (voiceRecorder.start()) recordingVoice = true
                    } else {
                        requestMic.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onFinishRecording = { finishVoiceSend() },
            )
        }
    }

    if (showAttach) {
        AivoraBottomSheet(
            onDismiss = { showAttach = false },
            title = "Share",
        ) {
            AivoraSheetActionGrid(
                actions = listOf(
                    AivoraSheetAction("Gallery", AppIcons.PhotoLibrary) {
                        showAttach = false
                        media.pickGallery()
                    },
                    AivoraSheetAction("View once photo", AppIcons.Visibility) {
                        showAttach = false
                        pickViewOnce.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    AivoraSheetAction("Camera", AppIcons.CameraAlt) {
                        showAttach = false
                        onNavigate(Routes.camera("chat:$chatId"))
                    },
                    AivoraSheetAction("Document", AppIcons.Description) {
                        showAttach = false
                        media.pickDocument()
                    },
                    AivoraSheetAction("Location", AppIcons.LocationOn) {
                        showAttach = false
                        onNavigate(Routes.locationShare(chatId))
                    },
                    AivoraSheetAction("Contact", AppIcons.ContactPage) {
                        showAttach = false
                        if (useRemote) {
                            onNavigate(Routes.contactShare(chatId))
                        } else {
                            haptics.confirm()
                            InMemoryChatStore.sendText(chatId, "👤 Contact card · $firstName")
                            peerTyping = true
                        }
                    },
                    AivoraSheetAction("Poll", AppIcons.Poll) {
                        showAttach = false
                        onNavigate(Routes.pollCreate(chatId))
                    },
                    AivoraSheetAction("Event", AppIcons.Event) {
                        showAttach = false
                        onNavigate(Routes.eventCreate(chatId))
                    },
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showClearChat) {
        AivoraAlertDialog(
            title = "Clear this chat?",
            body = "All messages will be removed for you and this chat leaves your list. This can’t be undone.",
            confirmLabel = "Clear chat",
            confirmDanger = true,
            onConfirm = {
                showClearChat = false
                if (useRemote) {
                    scope.launch {
                        val ok = runCatching {
                            api.postConversationsIdClear(chatId)
                        }.isSuccess
                        if (ok) {
                            InMemoryChatStore.clearChat(chatId, context)
                            InMemoryChatStore.removeChat(chatId, context)
                            onBack?.invoke()
                        } else {
                            Toast.makeText(context, "Could not clear chat", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    InMemoryChatStore.clearChat(chatId, context)
                    InMemoryChatStore.removeChat(chatId, context)
                    onBack?.invoke()
                }
            },
            onDismiss = { showClearChat = false },
        )
    }

    if (showExportChat) {
        AlertDialog(
            onDismissRequest = { if (!exportBusy) showExportChat = false },
            title = { Text("Export chat") },
            text = { Text("Include media in the export? Without media is a text file; with media is a zip.") },
            confirmButton = {
                TextButton(
                    enabled = !exportBusy,
                    onClick = {
                        if (exportBusy) return@TextButton
                        exportBusy = true
                        scope.launch {
                            val ok = com.aivora.app.data.ChatExport.downloadAndShare(
                                context = context,
                                api = api,
                                conversationId = chatId,
                                chatTitle = chat.title,
                                includeMedia = true,
                            )
                            exportBusy = false
                            showExportChat = false
                            if (ok) {
                                Toast.makeText(context, "Chat exported", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) { Text(if (exportBusy) "Exporting…" else "Include media") }
            },
            dismissButton = {
                TextButton(
                    enabled = !exportBusy,
                    onClick = {
                        if (exportBusy) return@TextButton
                        exportBusy = true
                        scope.launch {
                            val ok = com.aivora.app.data.ChatExport.downloadAndShare(
                                context = context,
                                api = api,
                                conversationId = chatId,
                                chatTitle = chat.title,
                                includeMedia = false,
                            )
                            exportBusy = false
                            showExportChat = false
                            if (ok) {
                                Toast.makeText(context, "Chat exported", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ) { Text("Without media") }
            },
        )
    }

    actionTarget?.let { target ->
        AivoraBottomSheet(
            onDismiss = { actionTarget = null },
            title = "Message",
        ) {
            AivoraSheetActionList(
                actions = buildList {
                    if (!target.deletedForEveryone) {
                        add(
                            AivoraSheetAction("Reply", AppIcons.Reply) {
                                replyTo = target
                                actionTarget = null
                            },
                        )
                        add(
                            AivoraSheetAction("React", AppIcons.EmojiEmotions) {
                                reactTarget = target
                                actionTarget = null
                            },
                        )
                        add(
                            AivoraSheetAction(
                                if (target.starred) "Unstar" else "Star",
                                if (target.starred) AppIcons.Star else AppIcons.StarBorder,
                            ) {
                                actionTarget = null
                                if (useRemote) {
                                    scope.launch {
                                        try {
                                            val uid = myUserId ?: SessionStore.userId(context)
                                            val updated = if (target.starred) {
                                                api.deleteMessagesIdStar(target.id)
                                                target.copy(starred = false)
                                            } else {
                                                val dto = api.postMessagesIdStar(target.id, EmptyRequestBody())
                                                if (uid != null) dto.toLocalMessage(uid)
                                                else target.copy(starred = true)
                                            }
                                            InMemoryChatStore.upsertMessage(chatId, updated)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Could not update star", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    InMemoryChatStore.upsertMessage(
                                        chatId,
                                        target.copy(starred = !target.starred),
                                    )
                                }
                            },
                        )
                        add(
                            AivoraSheetAction("Forward", AppIcons.Forward) {
                                forwardTarget = target
                                actionTarget = null
                            },
                        )
                    }
                    if (canEditMessage(target)) {
                        add(
                            AivoraSheetAction("Edit", AppIcons.Edit) {
                                editText = target.body
                                editTarget = target
                                actionTarget = null
                            },
                        )
                    }
                    if (target.mine && !target.deletedForEveryone && !target.system) {
                        add(
                            AivoraSheetAction("Info", AppIcons.Info) {
                                actionTarget = null
                                onNavigate(Routes.messageInfo(chatId, target.id))
                            },
                        )
                    }
                    add(
                        AivoraSheetAction("Delete", AppIcons.Delete, danger = true) {
                            deleteTarget = target
                            actionTarget = null
                        },
                    )
                },
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    reactTarget?.let { target ->
        AivoraBottomSheet(
            onDismiss = { reactTarget = null },
            title = "React",
        ) {
            Row(
                        modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ReactionEmojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.SurfaceMuted)
                            .clickable {
                                reactTarget = null
                                if (useRemote) {
                                    scope.launch {
                                        runCatching {
                                            api.postMessagesIdReactions(
                                                target.id,
                                                ReactionBody(emoji = emoji),
                                            )
                                        }.onSuccess { dto ->
                                            val uid = myUserId ?: SessionStore.userId(context)
                                            if (uid != null) {
                                                InMemoryChatStore.upsertMessage(
                                                    chatId,
                                                    dto.toLocalMessage(uid),
                                                )
                                            }
                                        }.onFailure {
                                            Toast.makeText(context, "Could not react", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    forwardTarget?.let { target ->
        ForwardToSheet(
            chatId = chatId,
            target = target,
            useRemote = useRemote,
            myUserId = myUserId,
            onDismiss = { forwardTarget = null },
        )
    }

    deleteTarget?.let { target ->
        AivoraAlertDialog(
            title = "Delete message?",
            body = if (target.mine) {
                "Delete for you, or for everyone in this chat."
            } else {
                "This removes the message from your chat only."
            },
            confirmLabel = "Delete for me",
            confirmDanger = true,
            secondaryLabel = if (target.mine && !target.deletedForEveryone) "Delete for everyone" else null,
            secondaryDanger = true,
            onSecondary = {
                val id = target.id
                deleteTarget = null
                if (useRemote) {
                    scope.launch {
                        runCatching { api.deleteMessagesId(id, "everyone") }
                            .onSuccess {
                                InMemoryChatStore.upsertMessage(
                                    chatId,
                                    target.copy(
                                        body = "This message was deleted",
                                        mediaUri = null,
                                        deletedForEveryone = true,
                                    ),
                                )
                            }
                            .onFailure {
                                Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    InMemoryChatStore.upsertMessage(
                        chatId,
                        target.copy(
                            body = "This message was deleted",
                            mediaUri = null,
                            deletedForEveryone = true,
                        ),
                    )
                }
            },
            onConfirm = {
                val id = target.id
                deleteTarget = null
                if (useRemote) {
                    scope.launch {
                        runCatching { api.deleteMessagesId(id, "me") }
                            .onSuccess { InMemoryChatStore.removeMessage(chatId, id) }
                            .onFailure {
                                Toast.makeText(context, "Could not delete", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    InMemoryChatStore.removeMessage(chatId, id)
                }
            },
            onDismiss = { deleteTarget = null },
        )
    }

    editTarget?.let { target ->
        val container = if (dark) AivoraColors.DarkSurface else AivoraColors.LightSurface
        val ink = if (dark) Color.White else AivoraColors.Ink
        val trimmed = editText.trim()
        AlertDialog(
            onDismissRequest = { editTarget = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = container,
            title = { Text("Edit message", fontWeight = FontWeight.Bold, color = ink) },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 6,
                )
            },
            confirmButton = {
                TextButton(
                    enabled = trimmed.isNotEmpty() && trimmed != target.body,
                    onClick = {
                        editTarget = null
                        haptics.confirm()
                        if (useRemote) {
                            scope.launch {
                                val uid = myUserId ?: SessionStore.userId(context)
                                runCatching { api.putMessagesId(target.id, EditMessageBody(text = trimmed)) }
                                    .onSuccess { dto ->
                                        InMemoryChatStore.upsertMessage(
                                            chatId,
                                            if (uid != null) dto.toLocalMessage(uid)
                                            else target.copy(body = trimmed, editedAtIso = dto.editedAt),
                                        )
                                    }
                                    .onFailure {
                                        Toast.makeText(context, "Could not edit message", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            InMemoryChatStore.upsertMessage(
                                chatId,
                                target.copy(body = trimmed, editedAtIso = java.time.Instant.now().toString()),
                            )
                        }
                    },
                ) {
                    Text("Save", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editTarget = null }) {
                    Text("Cancel", color = AivoraColors.TextSecondary)
                }
            },
        )
    }

    if (showWallpaper) {
        ChatWallpaperDialog(
            selected = chat.wallpaper,
            onPick = { key ->
                showWallpaper = false
                scope.launch {
                    val ok = ChatControls.setWallpaper(api, useRemote, chat, key)
                    if (!ok) {
                        Toast.makeText(context, "Could not change wallpaper", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDismiss = { showWallpaper = false },
        )
    }

    viewOnceShown?.let { (target, url) ->
        val closeViewer = {
            viewOnceShown = null
            InMemoryChatStore.upsertMessage(chatId, target.copy(mediaUri = null, body = "Opened"))
        }
        Dialog(
            onDismissRequest = closeViewer,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(onClick = closeViewer),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = rememberBubbleImageModel(url, width = 720.dp, height = 1280.dp),
                    contentDescription = "View once photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    "View once · tap to close",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 12.dp),
                )
            }
        }
    }

}

/**
 * Owns message [StateFlow] + native [RecyclerView] so parent chrome does not
 * restart on every WS upsert. F13: View recycle (WhatsApp-class fling).
 */
@Composable
private fun ChatMessageList(
    modifier: Modifier = Modifier,
    chatId: String,
    useRemote: Boolean,
    myUserId: String?,
    isGroup: Boolean,
    blockedMe: Boolean,
    dark: Boolean,
    peerTyping: Boolean,
    peerTitle: String = "",
    peerAvatarUrl: String? = null,
    searchQuery: String,
    showSearch: Boolean,
    api: AivoraApi,
    coverHeightPx: Int = 0,
    onLongPress: (LocalMessage) -> Unit,
    onViewOnceShown: (LocalMessage, String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAivoraHaptics()
    val messageFlow = remember(chatId) { InMemoryChatStore.messages(chatId) }
    val messages by messageFlow.collectAsState()
    val visibleMessages = remember(messages, searchQuery, showSearch) {
        val base = if (!showSearch || searchQuery.isBlank()) messages
        else messages.filter { it.body.contains(searchQuery, ignoreCase = true) }
        base.distinctBy { it.id }
    }
    // Chronological (oldest→newest). stackFromEnd pins latest to the bottom (WA).
    val listChronological = visibleMessages
    val view = LocalView.current
    val onLongPressState = rememberUpdatedState(onLongPress)
    val recyclerRef = remember(chatId) { arrayOfNulls<androidx.recyclerview.widget.RecyclerView>(1) }
    // Snap to latest once per open / after first non-empty submit (DiffUtil is async).
    val snapLatest = remember(chatId) { booleanArrayOf(true) }
    // Stick-to-bottom: auto-scroll new msgs without fighting history scroll (cheap flag).
    val stickBottom = remember(chatId) { booleanArrayOf(true) }
    val lastSubmittedId = remember(chatId) { arrayOfNulls<String>(1) }

    val newestId = messages.lastOrNull()?.id
    LaunchedEffect(chatId, newestId) {
        InMemoryChatStore.markRead(chatId)
        if (!useRemote) return@LaunchedEffect
        delay(250)
        runCatching { api.postConversationsIdRead(chatId) }
            .onSuccess { InMemoryChatStore.markRead(chatId) }
            .onFailure {
                android.util.Log.w("Chat", "mark read failed", it)
            }
    }

    val loadGate = remember(chatId) { booleanArrayOf(false, false) } // [loading, reachedStart]
    val olderListenerRef = remember(chatId) { arrayOfNulls<ChatOlderScrollListener>(1) }
    val historyLoader = remember(chatId) { ChatHistoryLoaderAdapter() }
    val myUserIdState = rememberUpdatedState(myUserId)
    val useRemoteState = rememberUpdatedState(useRemote)
    val loadOlder = remember<(String?) -> Unit>(chatId) {
        fun(anchorId: String?) {
            val uid = myUserIdState.value
            if (!useRemoteState.value || uid == null || loadGate[0] || loadGate[1]) return
            loadGate[0] = true
            historyLoader.loading = true
            olderListenerRef[0]?.setLoading(true)
            scope.launch {
                val added = ChatRepository.loadOlderMessages(chatId, uid)
                if (added == 0) {
                    loadGate[1] = true
                } else if (anchorId != null) {
                    val msgs = InMemoryChatStore.messages(chatId).value
                    val msgIndex = msgs.indexOfFirst { it.id == anchorId }
                    val rv = recyclerRef[0]
                    // ConcatAdapter: optional loader at 0, then messages.
                    val offset = if (historyLoader.loading) 1 else 0
                    if (msgIndex >= 0 && rv != null) {
                        rv.scrollToPosition(msgIndex + offset)
                    }
                }
                loadGate[0] = false
                historyLoader.loading = false
                olderListenerRef[0]?.setLoading(false)
            }
        }
    }

    val stableLongPress = remember<(LocalMessage) -> Unit>(chatId) {
        { msg -> onLongPressState.value(msg) }
    }
    val adapter = remember(chatId, isGroup, dark, blockedMe) {
        ChatMessageAdapter(
            isGroup = isGroup,
            dark = dark,
            forceSingleTick = blockedMe,
        )
    }
    adapter.onLongPress = stableLongPress
    adapter.peerTitle = peerTitle
    adapter.peerAvatarUrl = peerAvatarUrl
    // Chronological adapter first; loader prepended at visual top while paging older (WA spinner).
    val chatConcat = remember(chatId) { ConcatAdapter(historyLoader, adapter) }

    val olderListener = remember(chatId) {
        ChatOlderScrollListener(nearStart = true) {
            val anchorId = InMemoryChatStore.messages(chatId).value.firstOrNull()?.id
            loadOlder(anchorId)
        }.also { olderListenerRef[0] = it }
    }

    val stickListener = remember(chatId) {
        object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val lm = recyclerView.layoutManager as? androidx.recyclerview.widget.LinearLayoutManager
                    ?: return
                val last = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                if (total <= 0) return
                stickBottom[0] = last >= total - 2
            }
        }
    }

    var prevMsgCount by remember(chatId) { mutableStateOf(-1) }
    LaunchedEffect(chatId, view) {
        snapshotFlow {
            val msgs = InMemoryChatStore.messages(chatId).value
            val imeBottom = androidx.core.view.ViewCompat.getRootWindowInsets(view)
                ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime())
                ?.bottom ?: 0
            Triple(msgs.size, msgs.lastOrNull()?.id, imeBottom > 0)
        }
            .distinctUntilChanged()
            .collect { (size, lastId, ime) ->
                if (size == 0) {
                    prevMsgCount = 0
                    snapLatest[0] = true
                    stickBottom[0] = true
                    return@collect
                }
                val grew = prevMsgCount >= 0 && size > prevMsgCount
                prevMsgCount = size
                val rv = recyclerRef[0] ?: return@collect
                val lastMine = InMemoryChatStore.messages(chatId).value.lastOrNull()?.mine == true
                // Own send / IME / stick-to-bottom → jump to latest (post avoids layout races).
                if ((grew && (stickBottom[0] || lastMine)) || ime) {
                    stickBottom[0] = true
                    rv.post {
                        val end = (rv.adapter?.itemCount ?: 1) - 1
                        if (end >= 0) rv.scrollToPosition(end)
                    }
                }
                lastSubmittedId[0] = lastId
            }
    }

    Box(modifier = modifier.clipToBounds()) {
        ChatRecyclerHost(
            modifier = Modifier.fillMaxSize(),
            coverColor = android.graphics.Color.WHITE,
            coverHeightPx = coverHeightPx,
            onRecyclerReady = { rv ->
                recyclerRef[0] = rv
                rv.adapter = chatConcat
                val density = rv.resources.displayMetrics.density
                val pad = (6 * density).toInt()
                val hPad = (6 * density).toInt()
                rv.setPadding(hPad, pad, hPad, pad)
                rv.addOnScrollListener(olderListener)
                rv.addOnScrollListener(stickListener)
            },
            update = { rv ->
                recyclerRef[0] = rv
                if (rv.adapter !== chatConcat) rv.adapter = chatConcat
                val density = rv.resources.displayMetrics.density
                val pad = (6 * density).toInt()
                val hPad = (6 * density).toInt()
                if (rv.paddingTop != pad || rv.paddingLeft != hPad) {
                    rv.setPadding(hPad, pad, hPad, pad)
                }
                val shouldSnap = snapLatest[0]
                val newest = listChronological.lastOrNull()
                val grewTail = newest != null && newest.id != lastSubmittedId[0] &&
                    prevMsgCount >= 0 && listChronological.size >= prevMsgCount
                adapter.submitList(listChronological) {
                    lastSubmittedId[0] = newest?.id
                    val jump = shouldSnap ||
                        (grewTail && (stickBottom[0] || newest?.mine == true))
                    if (jump && listChronological.isNotEmpty()) {
                        stickBottom[0] = true
                        rv.post {
                            val end = (rv.adapter?.itemCount ?: 1) - 1
                            if (end >= 0) rv.scrollToPosition(end)
                        }
                        snapLatest[0] = false
                    }
                }
            },
        )
        if (peerTyping) {
            TypingIndicatorBubble(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 10.dp),
            )
        }
    }
}

/** Fixed-size media slot — always Coil (cached); no mid-fling placeholder thrash. */
@Composable
private fun ChatMediaImage(uri: String, contentDescription: String) {
    Box(
        modifier = Modifier
            .widthIn(min = 160.dp, max = 236.dp)
            .heightIn(min = 120.dp, max = 196.dp)
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .background(AivoraColors.PrimarySoft),
    ) {
        AsyncImage(
            model = rememberBubbleImageModel(uri),
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun AivoraAlertDialog(
    title: String,
    body: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmDanger: Boolean = false,
    secondaryLabel: String? = null,
    secondaryDanger: Boolean = false,
    onSecondary: (() -> Unit)? = null,
) {
    val dark = isSystemInDarkTheme()
    val container = if (dark) AivoraColors.DarkSurface else AivoraColors.LightSurface
    val ink = if (dark) Color.White else AivoraColors.Ink
    val muted = AivoraColors.TextSecondary
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = container,
        titleContentColor = ink,
        textContentColor = muted,
        title = { Text(title, fontWeight = FontWeight.Bold, color = ink) },
        text = { Text(body, color = muted) },
        confirmButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (secondaryLabel != null && onSecondary != null) {
                    TextButton(onClick = onSecondary) {
                        Text(
                            secondaryLabel,
                            color = if (secondaryDanger) AivoraColors.Danger else AivoraColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                TextButton(onClick = onConfirm) {
                    Text(
                        confirmLabel,
                        color = if (confirmDanger) AivoraColors.Danger else AivoraColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = muted)
            }
        },
    )
}

@Composable
private fun ChatHeader(
    title: String,
    subtitle: String,
    showTyping: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onCall: () -> Unit,
    onVideo: () -> Unit,
    onOpenProfile: () -> Unit,
    menuItems: List<OverflowMenuItem>,
) {
    // Figma Contact controls (284:6439): px12, gap8, call/video 44×44 gap8; keep compact ⋮.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 8.dp)
            .height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatHeaderIconButton(onClick = onBack, size = 40.dp) {
            Icon(
                painter = painterResource(AivoraIcons.Back),
                contentDescription = "Back",
                tint = AivoraColors.Ink,
                modifier = Modifier.size(22.dp),
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenProfile),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Avatar(title, size = 40)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 1.dp, bottom = 1.dp)
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
                if (subtitle.isNotBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                        color = if (showTyping) AivoraColors.Primary else AivoraColors.Online,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChatHeaderIconButton(onClick = onCall, size = 44.dp) {
                Icon(
                    painter = painterResource(AivoraIcons.Call),
                    contentDescription = "Voice call",
                    tint = AivoraColors.Primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            ChatHeaderIconButton(onClick = onVideo, size = 44.dp) {
                Icon(
                    painter = painterResource(AivoraIcons.Video),
                    contentDescription = "Video call",
                    tint = AivoraColors.Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Box {
                ChatHeaderIconButton(onClick = { onMenuExpandedChange(true) }, size = 36.dp) {
                    Icon(
                        painter = painterResource(AivoraIcons.More),
                        contentDescription = "More",
                        tint = AivoraColors.Ink,
                        modifier = Modifier.size(22.dp),
                    )
                }
                AivoraOverflowMenu(
                    expanded = menuExpanded,
                    onDismiss = { onMenuExpandedChange(false) },
                    items = menuItems,
                )
            }
        }
    }
    HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
}

/** Figma call actions are 44×44 — avoid Material IconButton’s 48dp min that squeezes last-seen. */
@Composable
private fun ChatHeaderIconButton(
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
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

/** WhatsApp/Bolta-style centered info strip (disappearing, call notices, etc.). */
@Composable
private fun SystemNotice(body: String) {
    val isCall = body.contains("call", ignoreCase = true)
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isCall) AivoraColors.SurfaceMuted
                    else AivoraColors.SoftMint,
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (isCall) {
                Icon(
                    imageVector = if (body.contains("video", ignoreCase = true)) {
                        AppIcons.Videocam
                    } else {
                        AppIcons.Call
                    },
                    contentDescription = null,
                    tint = AivoraColors.TextSecondary,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                body,
                style = MaterialTheme.typography.labelMedium,
                color = if (isCall) AivoraColors.TextSecondary else AivoraColors.InfoGreen,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MessageRow(
    message: LocalMessage,
    isGroup: Boolean,
    dark: Boolean,
    forceSingleTick: Boolean = false,
    onLongPress: (LocalMessage) -> Unit = {},
    onVote: (LocalMessage, optionId: String) -> Unit = { _, _ -> },
    onRsvp: (LocalMessage, answer: String) -> Unit = { _, _ -> },
    onOpenViewOnce: (LocalMessage) -> Unit = {},
) {
    val mine = message.mine
    val voiceSecs = message.voiceDurationSec
    val displayStatus = if (forceSingleTick && mine) MessageDeliveryStatus.Sent else message.status
    // Keep handler identity stable across parent recompositions so Lazy items can skip.
    val longPress by rememberUpdatedState(onLongPress)
    val vote by rememberUpdatedState(onVote)
    val rsvp by rememberUpdatedState(onRsvp)
    val openViewOnce by rememberUpdatedState(onOpenViewOnce)
    val bubbleBg = when {
        mine -> AivoraColors.BubbleMine
        dark -> AivoraColors.BubbleOtherDark
        else -> AivoraColors.BubbleOtherLight
    }
    val shape = if (mine) BubbleMineShape else BubbleOtherShape
    // Plain text is ~90% of fling work — keep that path tiny (no clip + no combinedClickable).
    val plainText = voiceSecs == null &&
        message.mediaUri == null &&
        message.mediaKind == null &&
        message.poll == null &&
        message.location == null &&
        message.contact == null &&
        message.event == null &&
        message.linkPreview == null &&
        !message.viewOnce &&
        !message.disappeared &&
        !message.deletedForEveryone &&
        message.replyTo == null &&
        message.reactions.isEmpty() &&
        !message.forwarded
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .pointerInput(message.id) {
                detectTapGestures(onLongPress = { longPress(message) })
            },
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Avatars only in groups — 1:1 WhatsApp style + cheaper scroll.
        if (!mine && isGroup) {
            Avatar(message.senderName ?: "Peer", size = 16)
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (plainText) {
            PlainTextBubble(
                message = message,
                mine = mine,
                dark = dark,
                isGroup = isGroup,
                bubbleBg = bubbleBg,
                shape = shape,
                displayStatus = displayStatus,
            )
            return@Row
        }
        Column(
            modifier = Modifier
                .widthIn(max = if (voiceSecs != null) 280.dp else 260.dp)
                .wrapContentWidth()
                .background(bubbleBg, shape)
                .then(
                    if (!mine && !dark) {
                        Modifier.border(1.dp, AivoraColors.ChatDivider, shape)
                    } else {
                        Modifier
                    },
                )
                .padding(
                    horizontal = if (message.mediaUri != null) 8.dp else 12.dp,
                    vertical = 8.dp,
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
            if (message.forwarded) {
                Text(
                    "Forwarded",
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            message.replyTo?.let { quote ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AivoraColors.Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(AivoraColors.Primary),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        quote.body,
                        color = AivoraColors.TextSecondary,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (message.disappeared || message.deletedForEveryone) {
                Text(
                    message.body,
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                )
            } else if (message.viewOnce) {
                ViewOnceBubble(
                    opened = message.mediaUri == null,
                    onClick = if (!mine && message.mediaUri != null) {
                        { openViewOnce(message) }
                    } else {
                        null
                    },
                )
            } else if (message.poll != null) {
                PollBubble(message.poll) { optionId -> vote(message, optionId) }
            } else if (message.location != null) {
                LocationBubble(message.location)
            } else if (message.contact != null) {
                ContactBubble(message.contact)
            } else if (message.event != null) {
                EventBubble(message.event) { answer -> rsvp(message, answer) }
            } else if (voiceSecs != null) {
                VoiceNoteContent(
                    durationSec = voiceSecs,
                    mediaUrl = message.mediaUri,
                )
            } else when (message.mediaKind) {
                LocalMediaKind.File -> {
                    FileAttachmentContent(
                        name = message.fileName ?: message.body.ifBlank { "Document" },
                        url = message.mediaUri,
                    )
                }
                LocalMediaKind.Video -> {
                    val ctx = LocalContext.current
                    message.mediaUri?.let { uri ->
                        Box(
                            modifier = Modifier.clickable {
                                runCatching {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                                }
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            ChatMediaImage(uri = uri, contentDescription = "Video")
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    AppIcons.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (message.body.isNotBlank() && message.body != "Video") {
                        Text(
                            message.body,
                            color = AivoraColors.Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
                LocalMediaKind.Image, null -> {
                    message.mediaUri?.let { uri ->
                        ChatMediaImage(uri = uri, contentDescription = "Photo")
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (message.body.isNotBlank() && message.mediaUri == null) {
                        Text(
                            message.body,
                            color = if (message.deletedForEveryone) {
                                AivoraColors.TextSecondary
                            } else {
                                AivoraColors.Ink
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = if (message.deletedForEveryone) {
                                androidx.compose.ui.text.font.FontStyle.Italic
                            } else {
                                androidx.compose.ui.text.font.FontStyle.Normal
                            },
                        )
                    } else if (message.body.isNotBlank() && message.mediaUri != null &&
                        message.body != "Photo"
                    ) {
                        Text(
                            message.body,
                            color = AivoraColors.Ink,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
                LocalMediaKind.Audio -> {
                    VoiceNoteContent(
                        durationSec = message.voiceDurationSec ?: 1,
                        mediaUrl = message.mediaUri,
                    )
                }
            }
            message.linkPreview?.let { card ->
                LinkPreviewCard(card)
            }
            if (message.reactions.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    message.reactions.forEach { reaction ->
                        Text(
                            "${reaction.emoji} ${reaction.count}".trim(),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (reaction.mine) AivoraColors.Primary.copy(alpha = 0.18f)
                                    else AivoraColors.SurfaceMuted,
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp, start = if (message.mediaUri != null) 4.dp else 0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (message.starred) {
                Icon(
                        AppIcons.Star,
                        contentDescription = "Starred",
                    tint = AivoraColors.Primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (voiceSecs != null && mine) {
                    Icon(
                        painter = painterResource(AivoraIcons.Mic),
                        contentDescription = null,
                        tint = AivoraColors.Primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                if (message.expiresAtIso != null && !message.disappeared) {
                    Icon(
                        AppIcons.Timer,
                        contentDescription = "Disappearing",
                        tint = AivoraColors.TextSecondary,
                        modifier = Modifier.size(11.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }
                if (message.edited && !message.deletedForEveryone) {
                    Text(
                        "Edited",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = AivoraColors.TextSecondary,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    message.time,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = 11.sp,
                    color = AivoraColors.TextSecondary,
                )
                if (mine && !message.deletedForEveryone) {
                    Spacer(modifier = Modifier.width(4.dp))
                    DeliveryTicks(displayStatus)
                }
            }
        }
    }
}

/** Cheap text bubble — no MaterialTheme lookups, no vector ticks inflate during fling. */
@Composable
private fun PlainTextBubble(
    message: LocalMessage,
    mine: Boolean,
    dark: Boolean,
    isGroup: Boolean,
    bubbleBg: Color,
    shape: RoundedCornerShape,
    displayStatus: MessageDeliveryStatus,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .wrapContentWidth()
            .background(bubbleBg, shape)
            .then(
                if (!mine && !dark) {
                    Modifier.border(1.dp, AivoraColors.ChatDivider, shape)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        if (!mine && isGroup && !message.senderName.isNullOrBlank()) {
            Text(
                message.senderName,
                color = AivoraColors.Primary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 2.dp),
            )
        }
        Text(
            message.body,
            color = AivoraColors.Ink,
            fontSize = 15.sp,
            lineHeight = 20.sp,
        )
        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (message.starred) {
                Text("★", color = AivoraColors.Primary, fontSize = 11.sp)
                Spacer(modifier = Modifier.width(3.dp))
            }
            if (message.edited) {
                Text(
                    "Edited",
                    fontSize = 11.sp,
                    color = AivoraColors.TextSecondary,
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                message.time,
                fontSize = 11.sp,
                color = AivoraColors.TextSecondary,
            )
            if (mine) {
                Spacer(modifier = Modifier.width(4.dp))
                DeliveryTicks(displayStatus)
            }
        }
    }
}

@Composable
private fun DeliveryTicks(status: MessageDeliveryStatus) {
    Text(
        text = when (status) {
            MessageDeliveryStatus.Sending -> "○"
            MessageDeliveryStatus.Sent -> "✓"
            else -> "✓✓"
        },
        fontSize = 11.sp,
        color = when (status) {
            MessageDeliveryStatus.Read -> Color(0xFF53BDEB)
            MessageDeliveryStatus.Delivered -> AivoraColors.TextSecondary
            else -> AivoraColors.TextSecondary.copy(alpha = 0.55f)
        },
    )
}

private const val EDIT_WINDOW_SECONDS = 15 * 60L

private fun canEditMessage(m: LocalMessage): Boolean {
    if (!m.mine || m.deletedForEveryone || m.system || m.disappeared || m.viewOnce) return false
    if (m.mediaUri != null || m.mediaKind != null || m.voiceDurationSec != null) return false
    if (m.poll != null || m.contact != null || m.location != null || m.event != null) return false
    if (m.body.isBlank() || m.id.startsWith("local-")) return false
    val created = runCatching { java.time.Instant.parse(m.createdAtIso) }.getOrNull() ?: return false
    return java.time.Duration.between(created, java.time.Instant.now()).seconds <= EDIT_WINDOW_SECONDS
}

@Composable
private fun ViewOnceBubble(opened: Boolean, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .background(AivoraColors.PrimarySoft)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            if (opened) AppIcons.VisibilityOff else AppIcons.Visibility,
            contentDescription = null,
            tint = AivoraColors.Primary,
        )
        Text(
            if (opened) "Opened" else "View once photo",
            color = AivoraColors.Ink,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun FileAttachmentContent(name: String, url: String?) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .widthIn(min = 180.dp)
            .clickable(enabled = !url.isNullOrBlank()) {
                url?.let {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                    }
                }
            }
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AivoraColors.Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.InsertDriveFile,
                contentDescription = null,
                tint = AivoraColors.Primary,
            )
        }
        Text(
            name,
            color = AivoraColors.Ink,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun LinkPreviewCard(card: com.aivora.app.data.local.LocalLinkPreview) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(AivoraColors.Primary.copy(alpha = 0.1f))
            .clickable {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(card.url)))
                }
            }
            .padding(10.dp),
    ) {
        card.imageUrl?.takeIf { it.isNotBlank() }?.let { img ->
            AsyncImage(
                model = rememberBubbleImageModel(img, width = 320.dp, height = 120.dp),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            card.title ?: card.site ?: card.url,
            color = AivoraColors.Ink,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        card.description?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                color = AivoraColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            card.site ?: card.url,
            color = AivoraColors.Primary,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Flat progress track — Canvas bar waveforms were too costly while scrolling past voice rows. */
@Composable
private fun VoiceProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = AivoraColors.Primary,
    idleColor: Color = AivoraColors.Primary.copy(alpha = 0.22f),
) {
    val p = progress.coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(4.dp)
            .background(idleColor, RoundedCornerShape(2.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(p.coerceAtLeast(0.02f))
                .height(4.dp)
                .background(activeColor, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun VoiceNoteContent(durationSec: Int, mediaUrl: String? = null) {
    val context = LocalContext.current
    var playing by remember(mediaUrl) { mutableStateOf(false) }
    var ready by remember(mediaUrl) { mutableStateOf(false) }
    var progress by remember(mediaUrl) { mutableStateOf(0f) }
    var positionSec by remember(mediaUrl) { mutableStateOf(0) }
    var loading by remember(mediaUrl) { mutableStateOf(false) }
    val playerHolder = remember(mediaUrl) { arrayOf<MediaPlayer?>(null) }

    // Create MediaPlayer only on first play — scrolling past voice rows must stay cheap.
    DisposableEffect(mediaUrl) {
        onDispose {
            playing = false
            ready = false
            loading = false
            val mp = playerHolder[0]
            playerHolder[0] = null
            runCatching {
                mp?.reset()
                mp?.release()
            }
        }
    }

    LaunchedEffect(playing, mediaUrl) {
        while (playing) {
            val mp = playerHolder[0] ?: break
            val dur = runCatching { mp.duration }.getOrDefault(0).coerceAtLeast(1)
            val pos = runCatching { mp.currentPosition }.getOrDefault(0)
            progress = pos.toFloat() / dur.toFloat()
            positionSec = (pos / 1000).coerceAtLeast(0)
            delay(200)
        }
    }

    fun ensurePlayerAndPlay() {
        val url = mediaUrl ?: return
        if (url.isBlank()) return
        val existing = playerHolder[0]
        if (existing != null && ready) {
            runCatching { existing.start() }
            playing = true
            return
        }
        if (loading) return
        loading = true
        val mp = MediaPlayer()
        playerHolder[0] = mp
        try {
            mp.setDataSource(context, Uri.parse(url))
            mp.setOnPreparedListener {
                ready = true
                loading = false
                runCatching { mp.start() }
                playing = true
            }
            mp.setOnCompletionListener {
                playing = false
                progress = 0f
                positionSec = 0
                runCatching { mp.seekTo(0) }
            }
            mp.setOnErrorListener { _, _, _ ->
                loading = false
                ready = false
                playing = false
                true
            }
            mp.prepareAsync()
        } catch (_: Exception) {
            loading = false
            ready = false
            playerHolder[0] = null
            runCatching { mp.release() }
        }
    }

    fun togglePlay() {
        if (mediaUrl.isNullOrBlank()) return
        val mp = playerHolder[0]
        if (playing && mp != null) {
            runCatching { mp.pause() }
            playing = false
            return
        }
        ensurePlayerAndPlay()
    }

    fun seekToFraction(fraction: Float) {
        val next = fraction.coerceIn(0f, 1f)
        progress = next
        val mp = playerHolder[0]
        if (mp != null && ready) {
            val dur = runCatching { mp.duration }.getOrDefault(0)
            if (dur > 0) {
                val ms = (next * dur).toInt().coerceIn(0, dur)
                runCatching { mp.seekTo(ms) }
                positionSec = ms / 1000
                return
            }
        }
        if (durationSec > 0) {
            positionSec = (next * durationSec).toInt().coerceIn(0, durationSec)
        }
    }

    Column(
        modifier = Modifier.widthIn(min = 180.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AivoraColors.Primary, CircleShape)
                    .clickable(enabled = !mediaUrl.isNullOrBlank(), onClick = { togglePlay() }),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    loading -> Text("…", color = Color.White, fontWeight = FontWeight.Bold)
                    playing -> Text("❚❚", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    else -> Icon(
                        AppIcons.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            VoiceProgressTrack(
                progress = if (playing || progress > 0f) progress else 0f,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (playing) {
                            Modifier.pointerInput(mediaUrl, ready, durationSec) {
                                detectTapGestures { offset ->
                                    val w = size.width.toFloat().coerceAtLeast(1f)
                                    seekToFraction(offset.x / w)
                                }
                            }
                        } else {
                            Modifier
                        },
                    ),
            )
        }
        Text(
            formatVoiceDuration(if (playing || progress > 0f) positionSec else durationSec),
            color = AivoraColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(start = 46.dp),
        )
    }
}

private fun formatVoiceDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun ForwardToSheet(
    chatId: String,
    target: LocalMessage,
    useRemote: Boolean,
    myUserId: String?,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    val chats by InMemoryChatStore.chats.collectAsState()
    val destinations = remember(chats, chatId) { chats.filter { it.id != chatId } }
    AivoraBottomSheet(
        onDismiss = onDismiss,
        title = "Forward to…",
    ) {
        if (destinations.isEmpty()) {
            Text(
                "No other chats yet",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else {
            AivoraSheetActionList(
                actions = destinations.map { dest ->
                    AivoraSheetAction(dest.title, AppIcons.Person) {
                        onDismiss()
                        if (useRemote) {
                            scope.launch {
                                runCatching {
                                    api.postMessagesIdForward(
                                        target.id,
                                        ForwardMessageBody(conversationId = dest.id),
                                    )
                                }.onSuccess { dto ->
                                    val uid = myUserId ?: SessionStore.userId(context)
                                    if (uid != null) {
                                        InMemoryChatStore.upsertMessage(
                                            dest.id,
                                            dto.toLocalMessage(uid),
                                        )
                                    }
                                    Toast.makeText(context, "Forwarded", Toast.LENGTH_SHORT).show()
                                }.onFailure {
                                    Toast.makeText(context, "Could not forward", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            InMemoryChatStore.sendText(
                                dest.id,
                                target.body.ifBlank { "Forwarded message" },
                            )
                            Toast.makeText(context, "Forwarded", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RecordingPulseDot() {
    val pulse = rememberInfiniteTransition(label = "recPulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "recAlpha",
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(AivoraColors.Danger.copy(alpha = pulseAlpha)),
    )
}

/** Same layout as Figma voice bubble — Danger palette while recording. */
@Composable
private fun RecordingVoiceBubble(
    levels: FloatArray,
    elapsedSec: Int,
    modifier: Modifier = Modifier,
) {
    val barColor = AivoraColors.Danger
    val idleColor = AivoraColors.Danger.copy(alpha = 0.28f)
    val durationColor = Color(0xFFB42318)
    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 4.dp,
                ),
            )
            .background(Color(0xFFFFF1F0))
            .border(1.dp, Color(0xFFF5C2C0), RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.Danger),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
            ) {
                val n = levels.size.coerceAtLeast(1)
                val barW = 4.dp.toPx()
                val gap = 3.dp.toPx()
                val maxH = size.height * 0.95f
                var x = 0f
                for (i in 0 until n) {
                    if (x + barW > size.width) break
                    val amp = levels[i].coerceIn(0.12f, 1f)
                    val bh = (amp * maxH).coerceAtLeast(2.dp.toPx())
                    val top = (size.height - bh) / 2f
                    drawRoundRect(
                        color = if (i >= n - 4) barColor else idleColor,
                        topLeft = Offset(x, top),
                        size = Size(barW, bh),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        style = Fill,
                    )
                    x += barW + gap
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "0:00 / ${formatVoiceDuration(elapsedSec)}",
                    color = durationColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                )
                RecordingPulseDot()
                Text(
                    "Rec",
                    color = AivoraColors.TextSecondary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun ChatComposer(
    chatId: String,
    useRemote: Boolean,
    placeholder: String,
    replyPreview: String? = null,
    onClearReply: () -> Unit = {},
    recording: Boolean = false,
    voiceRecorder: VoiceRecorder? = null,
    onCancelRecording: () -> Unit = {},
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onSendText: (String) -> Unit,
    onRequestMic: () -> Unit,
    onFinishRecording: () -> Unit,
) {
    // Draft lives here so keystrokes do not recompose the message LazyColumn (PERF F7).
    var draft by remember(chatId) { mutableStateOf("") }
    var typingSent by remember(chatId) { mutableStateOf(false) }
    val canSend = draft.isNotBlank() || recording
    var recordingSecs by remember { mutableStateOf(0) }
    var liveLevels by remember { mutableStateOf(FloatArray(28) { 0.2f }) }
    LaunchedEffect(draft, useRemote, chatId) {
        if (!useRemote) return@LaunchedEffect
        val hasText = draft.trim().isNotEmpty()
        if (!hasText) {
            if (typingSent) {
                RealtimeClient.setTyping(chatId, false)
                typingSent = false
            }
            return@LaunchedEffect
        }
        delay(300)
        if (draft.trim().isEmpty()) return@LaunchedEffect
        RealtimeClient.setTyping(chatId, true)
        typingSent = true
        delay(1_500)
        RealtimeClient.setTyping(chatId, false)
        typingSent = false
    }
    LaunchedEffect(recording, voiceRecorder) {
        if (!recording || voiceRecorder == null) {
            recordingSecs = 0
            return@LaunchedEffect
        }
        while (true) {
            recordingSecs = voiceRecorder.elapsedSec()
            val amp = voiceRecorder.amplitude()
            val prev = liveLevels
            liveLevels = FloatArray(prev.size) { i ->
                val target = if (i == prev.lastIndex) amp else prev.getOrElse(i + 1) { amp }
                prev[i] * 0.35f + target * 0.65f
            }
            delay(80)
        }
    }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
    ) {
        HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
        if (replyPreview != null && !recording) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AivoraColors.SurfaceMuted)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AivoraColors.Primary),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Replying",
                        color = AivoraColors.Primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        replyPreview,
                        color = AivoraColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onClearReply, modifier = Modifier.size(32.dp)) {
                    Icon(AppIcons.Close, contentDescription = "Cancel reply", tint = AivoraColors.TextSecondary)
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (recording) {
                IconButton(onClick = onCancelRecording, modifier = Modifier.size(44.dp)) {
                    Icon(
                        AppIcons.Delete,
                        contentDescription = "Cancel recording",
                        tint = AivoraColors.Danger,
                    )
                }
                RecordingVoiceBubble(
                    levels = liveLevels,
                    elapsedSec = recordingSecs,
                    modifier = Modifier.weight(1f),
                )
            } else {
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
                        value = draft,
                        onValueChange = { draft = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = AivoraColors.Ink),
                        cursorBrush = SolidColor(AivoraColors.Primary),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (draft.isEmpty()) {
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
                    .clickable {
                        when {
                            recording -> onFinishRecording()
                            draft.trim().isNotEmpty() -> {
                                val body = draft.trim()
                                draft = ""
                                if (typingSent) {
                                    RealtimeClient.setTyping(chatId, false)
                                    typingSent = false
                                }
                                onSendText(body)
                            }
                            else -> onRequestMic()
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    canSend && (recording || draft.isNotBlank()) -> Icon(
                        AppIcons.Send,
                        contentDescription = "Send",
                        tint = AivoraColors.OnPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                    else -> Icon(
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

private val BubbleMineShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 16.dp,
    bottomEnd = 4.dp,
)
private val BubbleOtherShape = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 4.dp,
    bottomEnd = 16.dp,
)
