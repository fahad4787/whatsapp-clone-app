package com.aivora.app.ui.feature.chat

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Unarchive
import android.widget.Toast
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
import coil3.compose.AsyncImage
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.LocalMessage
import com.aivora.app.data.local.LocalReplyPreview
import com.aivora.app.data.local.MessageDeliveryStatus
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.BlockBody
import com.aivora.app.data.model.ConversationSettingsBody
import com.aivora.app.data.model.EmptyRequestBody
import com.aivora.app.data.model.ForwardMessageBody
import com.aivora.app.data.model.ReactionBody
import com.aivora.app.data.model.ReplyPreviewDto
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.realtime.RealtimeClient
import com.aivora.app.data.sendImageMessage
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
import com.aivora.app.ui.media.rememberMediaActions
import com.aivora.app.ui.motion.AivoraNavSlideMs
import com.aivora.app.ui.motion.AivoraTweenFast
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.delay
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
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    val dark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val haptics = rememberAivoraHaptics()
    val chats by InMemoryChatStore.chats.collectAsState()
    val chat = remember(chatId, chats) { InMemoryChatStore.chat(chatId) }
    val messages by InMemoryChatStore.messages(chatId).collectAsState()
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
    var replyTo by remember(chatId) { mutableStateOf<LocalMessage?>(null) }
    var myUserId by remember { mutableStateOf(SessionStore.userId(context)) }
    var peerOnline by remember { mutableStateOf(true) }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val allChats by InMemoryChatStore.chats.collectAsState()
    val listState = rememberLazyListState()
    val wallpaper = if (dark) AivoraColors.ChatWallpaperDark else AivoraColors.ChatWallpaperLight
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val media = rememberMediaActions(context) { uri ->
        haptics.confirm()
        if (useRemote) {
            scope.launch {
                try {
                    val sent = api.sendImageMessage(context, chatId, uri)
                    val uid = myUserId ?: SessionStore.userId(context)
                    if (uid != null) {
                        // Keep local content URI so the sender always sees the photo immediately.
                        InMemoryChatStore.upsertMessage(
                            chatId,
                            sent.toLocalMessage(uid).copy(mediaUri = uri.toString()),
                        )
                    }
                } catch (_: Exception) {
                    InMemoryChatStore.sendMedia(chatId, uri.toString())
                }
            }
        } else {
            InMemoryChatStore.sendMedia(chatId, uri.toString())
            peerTyping = true
        }
    }
    val firstName = chat.title.substringBefore(' ').ifBlank { chat.title }
    val hidePresence = chat.blockedByMe || chat.blockedMe
    val subtitle = when {
        hidePresence -> ""
        peerTyping -> "typing…"
        chat.isGroup -> "tap for group info"
        peerOnline -> "Online"
        else -> "Offline"
    }

    fun applySettingsResult(
        archived: Boolean? = null,
        pinned: Boolean? = null,
        mutedUntil: String? = null,
        clearMute: Boolean = false,
    ) {
        val current = InMemoryChatStore.chat(chatId)
        InMemoryChatStore.upsertChat(
            current.copy(
                archived = archived ?: current.archived,
                pinned = pinned ?: current.pinned,
                favorite = pinned ?: current.favorite,
                mutedUntil = when {
                    clearMute -> null
                    mutedUntil != null -> mutedUntil
                    else -> current.mutedUntil
                },
            ),
        )
    }

    DisposableEffect(chatId) {
        InMemoryChatStore.setViewingChat(chatId)
        onDispose { InMemoryChatStore.setViewingChat(null) }
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

    LaunchedEffect(useRemote, hidePresence) {
        if (!useRemote) return@LaunchedEffect
        if (hidePresence) {
            peerOnline = false
            return@LaunchedEffect
        }
        RealtimeClient.presence.collect { event ->
            if (event.userId != null && event.userId != myUserId) {
                peerOnline = event.online
            }
        }
    }

    // Initial history + slow backup poll (websocket is primary)
    LaunchedEffect(chatId, myUserId, useRemote) {
        if (!useRemote || myUserId == null) return@LaunchedEffect
        while (true) {
            val epoch = InMemoryChatStore.messageEpoch(chatId)
            try {
                val items = api.getConversationsIdMessages(chatId).items
                    .map { it.toLocalMessage(myUserId!!) }
                InMemoryChatStore.replaceMessages(chatId, items, expectedEpoch = epoch)
                runCatching { api.postConversationsIdRead(chatId) }
            } catch (_: Exception) { /* keep last list */ }
            delay(15_000)
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

    // Defer list settle until after slide enter so they don't fight.
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(chatId) {
        entered = false
        delay(AivoraNavSlideMs.toLong())
        entered = true
    }
    LaunchedEffect(messages.size, peerTyping, imeVisible, entered) {
        if (messages.isEmpty()) return@LaunchedEffect
        val index = (messages.lastIndex + if (peerTyping) 1 else 0).coerceAtLeast(0)
        if (entered) listState.animateScrollToItem(index)
        else listState.scrollToItem(index)
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
            menuItems = listOf(
                OverflowMenuItem(
                    if (chat.isGroup) "Group info" else "View contact",
                    Icons.Default.Person,
                ) { onNavigate(Routes.ProfileId.replace("{id}", chatId)) },
                OverflowMenuItem("Search", Icons.Default.Search) {
                    showSearch = true
                },
                OverflowMenuItem(
                    if (chat.isMuted) "Unmute" else "Mute notifications",
                    if (chat.isMuted) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                ) {
                    val nextMuted = !chat.isMuted
                    if (useRemote) {
                        scope.launch {
                            runCatching {
                                api.putConversationsIdSettings(
                                    chatId,
                                    ConversationSettingsBody(muted = nextMuted),
                                )
                            }.onSuccess { res ->
                                applySettingsResult(
                                    mutedUntil = res.mutedUntil,
                                    clearMute = res.mutedUntil.isNullOrBlank(),
                                )
                            }.onFailure {
                                android.util.Log.w("Chat", "mute failed", it)
                                Toast.makeText(context, "Could not update mute", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        if (nextMuted) {
                            applySettingsResult(
                                mutedUntil = java.time.Instant.now().plusSeconds(8 * 3600).toString(),
                            )
                        } else {
                            applySettingsResult(clearMute = true)
                        }
                    }
                },
                OverflowMenuItem(
                    if (chat.pinned) "Unpin" else "Pin",
                    Icons.Default.PushPin,
                ) {
                    val nextPinned = !chat.pinned
                    if (useRemote) {
                        scope.launch {
                            runCatching {
                                api.putConversationsIdSettings(
                                    chatId,
                                    ConversationSettingsBody(pinned = nextPinned),
                                )
                            }.onSuccess { res ->
                                applySettingsResult(pinned = res.pinned)
                            }.onFailure {
                                android.util.Log.w("Chat", "pin failed", it)
                                Toast.makeText(context, "Could not pin (max 3)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        applySettingsResult(pinned = nextPinned)
                    }
                },
                OverflowMenuItem(
                    if (chat.archived) "Unarchive" else "Archive",
                    if (chat.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                ) {
                    val nextArchived = !chat.archived
                    if (useRemote) {
                        scope.launch {
                            runCatching {
                                api.putConversationsIdSettings(
                                    chatId,
                                    ConversationSettingsBody(archived = nextArchived),
                                )
                            }.onSuccess { res ->
                                applySettingsResult(archived = res.archived)
                                if (nextArchived) onBack?.invoke()
                            }.onFailure {
                                android.util.Log.w("Chat", "archive failed", it)
                                Toast.makeText(context, "Could not archive", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        applySettingsResult(archived = nextArchived)
                        if (nextArchived) onBack?.invoke()
                    }
                },
                OverflowMenuItem("Report", Icons.Default.Flag, danger = true) {
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
                OverflowMenuItem("Block", Icons.Default.Block, danger = true) {
                    showBlockConfirm = true
                },
                OverflowMenuItem("Clear chat", Icons.Default.Delete, danger = true) {
                    showClearChat = true
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            ) {
                item(key = "date") {
                    DateSeparator(label = "Today")
                }
                items(visibleMessages, key = { it.id }) { msg ->
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
                        val peerId = chat.peerUserId
                        if (useRemote && !peerId.isNullOrBlank()) {
                            scope.launch {
                                runCatching { api.deleteBlocksId(peerId) }
                                    .onSuccess {
                                        InMemoryChatStore.upsertChat(chat.copy(blockedByMe = false))
                                        Toast.makeText(context, "Contact unblocked", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(context, "Could not unblock", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            InMemoryChatStore.upsertChat(chat.copy(blockedByMe = false))
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
        } else {
            ChatComposer(
                value = text,
                onValueChange = { text = it },
                placeholder = "Message $firstName…",
                replyPreview = replyTo?.let { r ->
                    if (r.deletedForEveryone) "Original message was deleted" else r.body.ifBlank { "Message" }
                },
                onClearReply = { replyTo = null },
                onAttach = {
                    focusManager.clearFocus(force = true)
                    keyboard?.hide()
                    showAttach = true
                },
                onCamera = { onNavigate(Routes.camera("chat:$chatId")) },
                onSendOrMic = {
                    val body = text.trim()
                    if (body.isNotEmpty()) {
                        haptics.confirm()
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
                    } else {
                        haptics.confirm()
                        InMemoryChatStore.sendVoice(chatId, durationSec = 8)
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
                    AivoraSheetAction("Camera", Icons.Default.CameraAlt) {
                        showAttach = false
                        onNavigate(Routes.camera("chat:$chatId"))
                    },
                    AivoraSheetAction("Document", Icons.Default.Description) {
                        showAttach = false
                        haptics.confirm()
                        InMemoryChatStore.sendText(chatId, "📄 Document.pdf")
                        peerTyping = true
                    },
                    AivoraSheetAction("Location", Icons.Default.LocationOn) {
                        showAttach = false
                        onNavigate(Routes.locationShare(chatId))
                    },
                    AivoraSheetAction("Contact", Icons.Default.ContactPage) {
                        showAttach = false
                        haptics.confirm()
                        InMemoryChatStore.sendText(chatId, "👤 Contact card · $firstName")
                        peerTyping = true
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
                            InMemoryChatStore.clearChat(chatId)
                            InMemoryChatStore.removeChat(chatId)
                            onBack?.invoke()
                        } else {
                            Toast.makeText(context, "Could not clear chat", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    InMemoryChatStore.clearChat(chatId)
                    InMemoryChatStore.removeChat(chatId)
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
        val destinations = allChats.filter { it.id != chatId }
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
                val peerId = chat.peerUserId
                if (useRemote && !peerId.isNullOrBlank()) {
                    scope.launch {
                        runCatching {
                            api.postBlocks(BlockBody(userId = peerId))
                        }.onSuccess {
                            InMemoryChatStore.upsertChat(
                                chat.copy(blockedByMe = true),
                            )
                            Toast.makeText(context, "Contact blocked", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, "Could not block", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "No peer to block", Toast.LENGTH_SHORT).show()
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageRow(
    message: LocalMessage,
    isGroup: Boolean,
    dark: Boolean,
    forceSingleTick: Boolean = false,
    onLongPress: () -> Unit = {},
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
                .widthIn(max = if (voiceSecs != null) 280.dp else 260.dp)
                .wrapContentWidth()
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
            if (voiceSecs != null) {
                VoiceNoteContent(
                    durationSec = voiceSecs,
                    seed = message.id.hashCode(),
                )
            } else {
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

@Composable
private fun VoiceNoteContent(durationSec: Int, seed: Int) {
    val bars = remember(seed) {
        val rnd = Random(seed)
        List(28) { 0.25f + rnd.nextFloat() * 0.75f }
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
                .background(AivoraColors.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text("▶", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp),
            ) {
                bars.forEachIndexed { i, h ->
                    val active = i < bars.size / 3
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height((28 * h).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (active) AivoraColors.Primary
                                else AivoraColors.Primary.copy(alpha = 0.28f),
                            ),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "0:00 / ${formatVoiceDuration(durationSec)}",
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
    onAttach: () -> Unit,
    onCamera: () -> Unit,
    onSendOrMic: () -> Unit,
) {
    val canSend = value.isNotBlank()
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            // Keep composer above nav bar and keyboard (edge-to-edge).
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
    ) {
        HorizontalDivider(color = AivoraColors.ChatDivider, thickness = 1.dp)
        if (replyPreview != null) {
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
