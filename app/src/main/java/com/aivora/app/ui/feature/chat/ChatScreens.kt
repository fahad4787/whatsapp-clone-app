package com.aivora.app.ui.feature.chat

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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import com.aivora.app.data.ChatRepository
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
import kotlin.random.Random

private val ReactionEmojis = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

@OptIn(ExperimentalFoundationApi::class)
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
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptics = rememberAivoraHaptics()
    val chats by InMemoryChatStore.chats.collectAsState()
    val chat = remember(chatId, chats) { InMemoryChatStore.chat(chatId) }
    val messages by InMemoryChatStore.messages(chatId).collectAsState()
    // reuse `chats` for forward sheet — avoid a second collectAsState on the same flow
    var text by remember(chatId) { mutableStateOf("") }
    var searchQuery by remember(chatId) { mutableStateOf("") }
    var showSearch by remember(chatId) { mutableStateOf(false) }
    var peerTyping by remember(chatId) { mutableStateOf(false) }
    var typingSent by remember(chatId) { mutableStateOf(false) }
    var showAttach by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showClearChat by remember { mutableStateOf(false) }
    var showReportSent by remember { mutableStateOf(false) }
    var showBlockConfirm by remember { mutableStateOf(false) }
    val visibleMessages = remember(messages, searchQuery, showSearch) {
        if (!showSearch || searchQuery.isBlank()) messages
        else messages.filter { it.body.contains(searchQuery, ignoreCase = true) }
    }
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
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    // Per-chat list state so reopen doesn't flash a previous scroll offset.
    val listState = remember(chatId) { androidx.compose.foundation.lazy.LazyListState() }
    val wallpaper = chatWallpaperColor(chat.wallpaper, dark)
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
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
    var recordingSecs by remember { mutableStateOf(0) }
    var liveLevels by remember { mutableStateOf(FloatArray(28) { 0.2f }) }
    DisposableEffect(Unit) {
        onDispose { voiceRecorder.cancel() }
    }
    LaunchedEffect(recordingVoice) {
        if (!recordingVoice) {
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
            delay(50)
        }
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
    val onlineUsers by RealtimeClient.onlineUsers.collectAsState()
    val roomOnlineMap by RealtimeClient.roomOnline.collectAsState()
    // Snapshot now lists members with the app open (not only this screen open).
    val groupOnlineCount = remember(chatId, roomOnlineMap, myUserId) {
        RealtimeClient.onlineCountInChat(chatId, myUserId)
    }
    val peerOnline = remember(chat.peerUserId, onlineUsers, hidePresence, chat.peerPresenceVisible) {
        if (hidePresence || chat.isGroup || !chat.peerPresenceVisible) false
        else RealtimeClient.isUserOnline(chat.peerUserId)
    }
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

    LaunchedEffect(chatId, messages.size) {
        InMemoryChatStore.markRead(chatId)
        if (useRemote) {
            runCatching { api.postConversationsIdRead(chatId) }
                .onSuccess { InMemoryChatStore.markRead(chatId) }
                .onFailure {
                    android.util.Log.w("Chat", "mark read failed", it)
                }
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

    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
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

    // Disk → paint, then network refresh (websocket is primary)
    LaunchedEffect(chatId, useRemote) {
        if (!useRemote) return@LaunchedEffect
        ChatRepository.hydrateMessagesFromDisk(context, chatId)
        // Refresh group role / privacy flags for composer gate.
        runCatching {
            val remote = api.getConversationsId(chatId)
            InMemoryChatStore.upsertChat(remote.toLocalChat())
        }
    }
    LaunchedEffect(chatId, myUserId, useRemote) {
        if (!useRemote || myUserId == null) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            ChatRepository.refreshMessages(context, chatId, myUserId!!)
            while (true) {
                delay(25_000)
                if (RealtimeClient.connected.value) continue
                ChatRepository.refreshMessages(context, chatId, myUserId!!)
            }
        }
    }
    // Scroll-up → older history (keep anchor so list doesn't jump)
    var loadingOlder by remember(chatId) { mutableStateOf(false) }
    var reachedStart by remember(chatId) { mutableStateOf(false) }
    LaunchedEffect(chatId, myUserId, useRemote, listState) {
        if (!useRemote || myUserId == null) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { first ->
                if (first > 2 || loadingOlder || reachedStart) return@collect
                loadingOlder = true
                val anchorId = messages.firstOrNull()?.id
                val added = ChatRepository.loadOlderMessages(chatId, myUserId!!)
                if (added == 0) {
                    reachedStart = true
                } else if (anchorId != null) {
                    val newIndex = InMemoryChatStore.messages(chatId).value
                        .indexOfFirst { it.id == anchorId }
                    if (newIndex >= 0) {
                        // +1 for date separator item above messages
                        listState.scrollToItem(newIndex + 1)
                    }
                }
                loadingOlder = false
            }
    }

    // Typing only when the composer has real text — focus / keyboard alone must not notify peers.
    LaunchedEffect(text, useRemote, chatId) {
        if (!useRemote) return@LaunchedEffect
        val hasText = text.trim().isNotEmpty()
        if (!hasText) {
            if (typingSent) {
                RealtimeClient.setTyping(chatId, false)
                typingSent = false
            }
            return@LaunchedEffect
        }
        delay(300)
        if (text.trim().isEmpty()) return@LaunchedEffect
        RealtimeClient.setTyping(chatId, true)
        typingSent = true
        delay(1_500)
        RealtimeClient.setTyping(chatId, false)
        typingSent = false
    }

    // Open pinned to latest; follow new messages only (react/star must not jump scroll).
    var didInitialScroll by remember(chatId) { mutableStateOf(false) }
    var prevMsgCount by remember(chatId) { mutableStateOf(-1) }
    LaunchedEffect(chatId) {
        didInitialScroll = false
        prevMsgCount = -1
    }
    LaunchedEffect(messages.size, messages.lastOrNull()?.id, peerTyping, imeVisible) {
        if (messages.isEmpty()) return@LaunchedEffect
        val index = (messages.lastIndex + if (peerTyping) 1 else 0).coerceAtLeast(0)
        val grew = prevMsgCount >= 0 && messages.size > prevMsgCount
        prevMsgCount = messages.size
        if (!didInitialScroll) {
            listState.scrollToItem(index)
            // Second pass after layout so we don't flash mid-thread then jump.
            delay(16)
            listState.scrollToItem(index)
            didInitialScroll = true
            return@LaunchedEffect
        }
        if (listState.isScrollInProgress) return@LaunchedEffect
        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val nearBottom = lastVisible >= index - 3
        if (grew && nearBottom) {
            listState.scrollToItem(index)
        } else if (imeVisible && nearBottom) {
            listState.scrollToItem(index)
        } else if (peerTyping && nearBottom) {
            listState.scrollToItem(index)
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
            onCall = { onNavigate(Routes.audioCall(chat.title)) },
            onVideo = { onNavigate(Routes.videoCall(chat.title)) },
            onOpenProfile = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
            menuItems = ChatControls.overflowMenuItems(
                chat = chat,
                scope = scope,
                context = context,
                api = api,
                useRemote = useRemote,
                includeSearch = true,
                includeClear = true,
                includeReport = !chat.isGroup,
                includeMedia = true,
                onDismiss = { showChatMenu = false },
                onSearch = { showSearch = true },
                onClear = { showClearChat = true },
                onViewInfo = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
                onMedia = { onNavigate(Routes.chatMedia(chatId)) },
                onWallpaper = { showWallpaper = true },
                onBlockRequest = { showBlockConfirm = true },
                onAfterArchive = { archived -> if (archived) onBack?.invoke() },
                onReport = {
                    if (useRemote) {
                        scope.launch {
                            try {
                                val peerId = InMemoryChatStore.chat(chatId).peerUserId
                                api.postReports(
                                    com.aivora.app.data.model.ReportRequestBody(
                                        targetUserId = peerId,
                                        targetConversationId = chatId,
                                        reason = "Reported from chat",
                                        messageIds = emptyList(),
                                        block = false,
                                    ),
                                )
                                showReportSent = true
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not send report", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        showReportSent = true
                    }
                },
            ),
        )

        if (showSearch) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(wallpaper),
    ) {
        LazyColumn(
            state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                item(key = "date", contentType = "date") {
                    DateSeparator(label = "Today")
                }
                items(
                    items = visibleMessages,
                    key = { it.id },
                    contentType = { msg ->
                        when {
                            msg.poll != null -> "poll"
                            msg.location != null -> "location"
                            msg.contact != null -> "contact"
                            msg.event != null -> "event"
                            msg.voiceDurationSec != null -> "voice"
                            msg.mediaKind != null -> msg.mediaKind.name
                            msg.linkPreview != null -> "link"
                            else -> "text"
                        }
                    },
                ) { msg ->
                    if (msg.system) {
                        SystemNotice(msg.body)
                    } else {
                        MessageRow(
                            message = msg,
                            isGroup = chat.isGroup,
                            dark = dark,
                            // WhatsApp: if they blocked you, ticks stay single (never delivered/read).
                            forceSingleTick = chat.blockedMe,
                            onLongPress = {
                                haptics.tick()
                                actionTarget = msg
                            },
                            onOpenViewOnce = {
                                val url = msg.mediaUri
                                if (url != null) {
                                    haptics.tick()
                                    if (useRemote) {
                                        scope.launch {
                                            runCatching { api.postMessagesIdViewOnce(msg.id, EmptyRequestBody()) }
                                                .onSuccess { dto ->
                                                    viewOnceShown = msg to (dto.attachment?.url?.takeIf { it.isNotBlank() } ?: url)
                                                }
                                                .onFailure {
                                                    Toast.makeText(context, "Could not open photo", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                    } else {
                                        viewOnceShown = msg to url
                                    }
                                }
                            },
                            onVote = { optionId ->
                                val uid = myUserId ?: SessionStore.userId(context)
                                if (useRemote && uid != null) {
                                    haptics.tick()
                                    scope.launch {
                                        runCatching { api.postMessagesIdVote(msg.id, VoteBody(optionId = optionId)) }
                                            .onSuccess { InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid)) }
                                            .onFailure {
                                                Toast.makeText(context, "Could not vote", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            },
                            onRsvp = { answer ->
                                val uid = myUserId ?: SessionStore.userId(context)
                                if (useRemote && uid != null) {
                                    haptics.tick()
                                    scope.launch {
                                        runCatching { api.postMessagesIdRsvp(msg.id, RsvpBody(answer = answer)) }
                                            .onSuccess { InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid)) }
                                            .onFailure {
                                                Toast.makeText(context, "Could not RSVP", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                            },
                        )
                    }
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
                value = text,
                onValueChange = { text = it },
                placeholder = "Message $firstName…",
                replyPreview = replyTo?.let { r ->
                    if (r.deletedForEveryone) "Original message was deleted" else r.body.ifBlank { "Message" }
                },
                onClearReply = { replyTo = null },
                recording = recordingVoice,
                recordingSecs = recordingSecs,
                recordingLevels = liveLevels,
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
                onSendOrMic = {
                    when {
                        recordingVoice -> finishVoiceSend()
                        text.trim().isNotEmpty() -> {
                            haptics.confirm()
                            val body = text.trim()
                            val quoting = replyTo
                            text = ""
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
                        }
                        else -> {
                            haptics.confirm()
                            if (context.hasMicPermission()) {
                                if (voiceRecorder.start()) recordingVoice = true
                            } else {
                                requestMic.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                },
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
                    AivoraSheetAction("Gallery", Icons.Default.PhotoLibrary) {
                        showAttach = false
                        media.pickGallery()
                    },
                    AivoraSheetAction("View once photo", Icons.Default.Visibility) {
                        showAttach = false
                        pickViewOnce.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                            ),
                        )
                    },
                    AivoraSheetAction("Camera", Icons.Default.CameraAlt) {
                        showAttach = false
                        onNavigate(Routes.camera("chat:$chatId"))
                    },
                    AivoraSheetAction("Document", Icons.Default.Description) {
                        showAttach = false
                        media.pickDocument()
                    },
                    AivoraSheetAction("Location", Icons.Default.LocationOn) {
                        showAttach = false
                        onNavigate(Routes.locationShare(chatId))
                    },
                    AivoraSheetAction("Contact", Icons.Default.ContactPage) {
                        showAttach = false
                        if (useRemote) {
                            onNavigate(Routes.contactShare(chatId))
                        } else {
                            haptics.confirm()
                            InMemoryChatStore.sendText(chatId, "👤 Contact card · $firstName")
                            peerTyping = true
                        }
                    },
                    AivoraSheetAction("Poll", Icons.Default.Poll) {
                        showAttach = false
                        onNavigate(Routes.pollCreate(chatId))
                    },
                    AivoraSheetAction("Event", Icons.Default.Event) {
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

    actionTarget?.let { target ->
        AivoraBottomSheet(
            onDismiss = { actionTarget = null },
            title = "Message",
        ) {
            AivoraSheetActionList(
                actions = buildList {
                    if (!target.deletedForEveryone) {
                        add(
                            AivoraSheetAction("Reply", Icons.Default.Reply) {
                                replyTo = target
                                actionTarget = null
                            },
                        )
                        add(
                            AivoraSheetAction("React", Icons.Default.EmojiEmotions) {
                                reactTarget = target
                                actionTarget = null
                            },
                        )
                        add(
                            AivoraSheetAction(
                                if (target.starred) "Unstar" else "Star",
                                if (target.starred) Icons.Default.Star else Icons.Default.StarBorder,
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
                            AivoraSheetAction("Forward", Icons.AutoMirrored.Filled.Forward) {
                                forwardTarget = target
                                actionTarget = null
                            },
                        )
                    }
                    if (canEditMessage(target)) {
                        add(
                            AivoraSheetAction("Edit", Icons.Default.Edit) {
                                editText = target.body
                                editTarget = target
                                actionTarget = null
                            },
                        )
                    }
                    if (target.mine && !target.deletedForEveryone && !target.system) {
                        add(
                            AivoraSheetAction("Info", Icons.Default.Info) {
                                actionTarget = null
                                onNavigate(Routes.messageInfo(chatId, target.id))
                            },
                        )
                    }
                    add(
                        AivoraSheetAction("Delete", Icons.Default.Delete, danger = true) {
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
        val destinations = chats.filter { it.id != chatId }
        AivoraBottomSheet(
            onDismiss = { forwardTarget = null },
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
                        AivoraSheetAction(dest.title, Icons.Default.Person) {
                            forwardTarget = null
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
                    model = url,
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

    if (showReportSent) {
        AivoraAlertDialog(
            title = "Report sent",
            body = "Thanks. Our team will review this in the admin panel.",
            confirmLabel = "OK",
            onConfirm = { showReportSent = false },
            onDismiss = { showReportSent = false },
        )
    }

    if (showBlockConfirm) {
        AivoraAlertDialog(
            title = "Block contact?",
            body = "They won’t be able to message you. You can unblock anytime in Settings.",
            confirmLabel = "Block",
            confirmDanger = true,
            onConfirm = {
                showBlockConfirm = false
                scope.launch {
                    val ok = ChatControls.block(api, useRemote, chat)
                    Toast.makeText(
                        context,
                        if (ok) "Contact blocked" else "Could not block",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
            onDismiss = { showBlockConfirm = false },
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
        Box {
            IconButton(onClick = { onMenuExpandedChange(true) }) {
                Icon(
                    painter = painterResource(AivoraIcons.More),
                    contentDescription = "More",
                    tint = AivoraColors.Ink,
                )
            }
            AivoraOverflowMenu(
                expanded = menuExpanded,
                onDismiss = { onMenuExpandedChange(false) },
                items = menuItems,
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

/** WhatsApp-style centered info strip (disappearing messages, etc.). */
@Composable
private fun SystemNotice(body: String) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            body,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AivoraColors.SoftMint)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = AivoraColors.InfoGreen,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageRow(
    message: LocalMessage,
    isGroup: Boolean,
    dark: Boolean,
    forceSingleTick: Boolean = false,
    onLongPress: () -> Unit = {},
    onVote: (optionId: String) -> Unit = {},
    onRsvp: (answer: String) -> Unit = {},
    onOpenViewOnce: () -> Unit = {},
) {
    val mine = message.mine
    val voiceSecs = message.voiceDurationSec
    val displayStatus = if (forceSingleTick && mine) MessageDeliveryStatus.Sent else message.status
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            ),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        // Avatars only in groups — 1:1 WhatsApp style + cheaper scroll.
        if (!mine && isGroup) {
            Avatar(message.senderName ?: "Peer", size = 16)
            Spacer(modifier = Modifier.width(4.dp))
        }
        val bubbleBg = when {
            mine -> AivoraColors.BubbleMine
            dark -> AivoraColors.BubbleOtherDark
            else -> AivoraColors.BubbleOtherLight
        }
        val shape = if (mine) BubbleMineShape else BubbleOtherShape
        Column(
            modifier = Modifier
                .widthIn(max = if (voiceSecs != null) 280.dp else 260.dp)
                .wrapContentWidth()
                .clip(shape)
                .background(bubbleBg)
                .then(
                    if (!mine && !dark) {
                        Modifier.border(1.dp, AivoraColors.ChatDivider, shape)
                    } else {
                        Modifier
                    },
                )
                .padding(
                    horizontal = when {
                        message.mediaUri != null -> 8.dp
                        voiceSecs != null -> 12.dp
                        else -> 12.dp
                    },
                    vertical = if (message.mediaUri != null) 8.dp else 8.dp,
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
                    onClick = if (!mine && message.mediaUri != null) onOpenViewOnce else null,
                )
            } else if (message.poll != null) {
                PollBubble(message.poll, onVote)
            } else if (message.location != null) {
                LocationBubble(message.location)
            } else if (message.contact != null) {
                ContactBubble(message.contact)
            } else if (message.event != null) {
                EventBubble(message.event, onRsvp)
            } else if (voiceSecs != null) {
                VoiceNoteContent(
                    durationSec = voiceSecs,
                    seed = message.id.hashCode(),
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
                            modifier = Modifier
                                .widthIn(min = 160.dp, max = 236.dp)
                                .heightIn(min = 120.dp, max = 196.dp)
                                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                                .background(AivoraColors.PrimarySoft)
                                .clickable {
                                    runCatching {
                                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Video",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                // Prefer decode size over full bitmap while scrolling.
                            )
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.45f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
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
                        Box(
                            modifier = Modifier
                                .widthIn(min = 160.dp, max = 236.dp)
                                .heightIn(min = 120.dp, max = 196.dp)
                                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                                .background(AivoraColors.PrimarySoft),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "Photo",
                                color = AivoraColors.TextSecondary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            AsyncImage(
                                model = uri,
                                contentDescription = "Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
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
                        seed = message.id.hashCode(),
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
                        Icons.Default.Star,
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
                        Icons.Default.Timer,
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
                    // Always use the ticks vector + explicit tint (TicksRead XML theme-tint hides blue).
                    Icon(
                        painter = painterResource(AivoraIcons.Ticks),
                        contentDescription = displayStatus.name,
                        tint = when (displayStatus) {
                            MessageDeliveryStatus.Read -> Color(0xFF53BDEB)
                            MessageDeliveryStatus.Delivered -> AivoraColors.TextSecondary
                            else -> AivoraColors.TextSecondary.copy(alpha = 0.55f)
                        },
                        modifier = Modifier.size(width = 16.dp, height = 14.dp),
                    )
                }
            }
        }
    }
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
            if (opened) Icons.Default.VisibilityOff else Icons.Default.Visibility,
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
                Icons.Default.InsertDriveFile,
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
                model = img,
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

@Composable
private fun VoiceWaveform(
    levels: FloatArray,
    progress: Float,
    modifier: Modifier = Modifier,
    activeColor: Color = AivoraColors.Primary,
    idleColor: Color = AivoraColors.Primary.copy(alpha = 0.28f),
) {
    Canvas(modifier = modifier.height(28.dp).fillMaxWidth()) {
        val n = levels.size.coerceAtLeast(1)
        val gap = 2.dp.toPx()
        val barW = ((size.width - gap * (n - 1)) / n).coerceAtLeast(1f)
        val lit = (progress.coerceIn(0f, 1f) * n).toInt()
        for (i in 0 until n) {
            val h = size.height * levels[i].coerceIn(0.12f, 1f)
            val x = i * (barW + gap)
            val y = (size.height - h) / 2f
            drawRoundRect(
                color = if (i < lit) activeColor else idleColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barW, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun VoiceNoteContent(durationSec: Int, seed: Int, mediaUrl: String? = null) {
    val context = LocalContext.current
    var playing by remember(mediaUrl) { mutableStateOf(false) }
    var ready by remember(mediaUrl) { mutableStateOf(false) }
    var progress by remember(mediaUrl) { mutableStateOf(0f) }
    var positionSec by remember(mediaUrl) { mutableStateOf(0) }
    var playWhenReady by remember(mediaUrl) { mutableStateOf(false) }
    val playerHolder = remember(mediaUrl) { arrayOf<MediaPlayer?>(null) }

    DisposableEffect(mediaUrl) {
        val url = mediaUrl
        if (url.isNullOrBlank()) {
            onDispose { }
        } else {
            ready = false
            playing = false
            progress = 0f
            positionSec = 0
            playWhenReady = false
            val mp = MediaPlayer()
            playerHolder[0] = mp
            try {
                mp.setDataSource(context, Uri.parse(url))
                mp.setOnPreparedListener {
                    ready = true
                    if (playWhenReady) {
                        playWhenReady = false
                        runCatching { mp.start() }
                        playing = true
                    }
                }
                mp.setOnCompletionListener {
                    playing = false
                    progress = 0f
                    positionSec = 0
                    runCatching { mp.seekTo(0) }
                }
                // Non-blocking — play/pause stays instant after this.
                mp.prepareAsync()
            } catch (_: Exception) {
                ready = false
                playerHolder[0] = null
                runCatching { mp.release() }
            }
            onDispose {
                playWhenReady = false
                playing = false
                ready = false
                playerHolder[0] = null
                runCatching {
                    mp.reset()
                    mp.release()
                }
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
            delay(50)
        }
    }

    val bars = remember(seed) {
        val rnd = Random(seed)
        FloatArray(28) { 0.22f + rnd.nextFloat() * 0.78f }
    }

    fun togglePlay() {
        if (mediaUrl.isNullOrBlank()) return
        val mp = playerHolder[0]
        if (playing && mp != null) {
            runCatching { mp.pause() }
            playing = false
            return
        }
        if (ready && mp != null) {
            runCatching { mp.start() }
            playing = true
        } else {
            playWhenReady = true
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.widthIn(min = 200.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AivoraColors.Primary)
                .clickable(enabled = !mediaUrl.isNullOrBlank(), onClick = { togglePlay() }),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when {
                    playing -> "❚❚"
                    playWhenReady && !ready -> "…"
                    else -> "▶"
                },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            VoiceWaveform(
                levels = bars,
                progress = if (playing || progress > 0f) progress else 0f,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "${formatVoiceDuration(positionSec)} / ${formatVoiceDuration(durationSec)}",
                    fontSize = 11.sp,
                    color = AivoraColors.TextSecondary,
                )
                Text("1x", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = AivoraColors.Ink)
            }
        }
    }
}

private fun formatVoiceDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun ChatComposer(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    replyPreview: String? = null,
    onClearReply: () -> Unit = {},
    recording: Boolean = false,
    recordingSecs: Int = 0,
    recordingLevels: FloatArray = FloatArray(0),
    onCancelRecording: () -> Unit = {},
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onSendOrMic: () -> Unit,
) {
    val canSend = value.isNotBlank() || recording
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
                    Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = AivoraColors.TextSecondary)
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
                        Icons.Default.Delete,
                        contentDescription = "Cancel recording",
                        tint = AivoraColors.Danger,
                    )
                }
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(AivoraColors.ComposerFill)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(AivoraColors.Danger.copy(alpha = pulseAlpha)),
                    )
                    Text(
                        formatVoiceDuration(recordingSecs),
                        color = AivoraColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        modifier = Modifier.defaultMinSize(minWidth = 40.dp),
                    )
                    VoiceWaveform(
                        levels = if (recordingLevels.isNotEmpty()) {
                            recordingLevels
                        } else {
                            FloatArray(28) { 0.2f }
                        },
                        progress = 1f,
                        modifier = Modifier.weight(1f),
                        activeColor = AivoraColors.Danger,
                        idleColor = AivoraColors.Danger.copy(alpha = 0.25f),
                    )
                }
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
                when {
                    canSend && (recording || value.isNotBlank()) -> Icon(
                        Icons.Default.Send,
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
