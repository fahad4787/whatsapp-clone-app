package com.aivora.app.ui.feature.home

import com.aivora.app.ui.icons.AppIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.StatusRepository
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalChat
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ReportRequestBody
import com.aivora.app.data.realtime.PresenceLifecycle
import com.aivora.app.data.realtime.RealtimeClient
import com.aivora.app.ui.components.AivoraBottomNav
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraFilterBar
import com.aivora.app.ui.components.AivoraFilterOption
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.components.AivoraOverflowMenu
import com.aivora.app.ui.perf.CallListAdapter
import com.aivora.app.ui.perf.CallListItem
import com.aivora.app.ui.perf.ChatListAdapter
import com.aivora.app.ui.perf.LockedChatsFooterAdapter
import com.aivora.app.ui.perf.RecyclerHost
import androidx.recyclerview.widget.ConcatAdapter
import com.aivora.app.ui.components.AivoraSheetActionList
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.ClearCallLogDialog
import com.aivora.app.ui.components.CreateCallLinkDialog
import com.aivora.app.ui.components.OverflowMenuItem
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.StatusRingAvatar
import com.aivora.app.ui.feature.chat.ChatControls
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.launch

private enum class ChatFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Favourites("Favourites"),
    Groups("Groups"),
    Archived("Archived"),
}

@androidx.compose.runtime.Immutable
private data class CallRow(
    val id: String,
    val name: String,
    val detail: String,
    val missed: Boolean,
    val video: Boolean,
    val time: String,
    val conversationId: String?,
)

private enum class CallFilter(val label: String) {
    All("All"),
    Missed("Missed"),
    Voice("Voice"),
    Video("Video"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
) {
    val haptics = rememberAivoraHaptics()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = HomeShellState.selectedTab.coerceIn(0, 3),
        pageCount = { 4 },
    )
    // Chrome reads settled page only — swipe progress stays inside bottomBar (PERF F6).
    val settled = pagerState.settledPage
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }
    var callFilter by remember { mutableStateOf(CallFilter.All) }
    var menuOpen by remember { mutableStateOf(false) }
    var showCreateCallLink by remember { mutableStateOf(false) }
    var showClearCallLog by remember { mutableStateOf(false) }
    var callLogCleared by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // Unread badge only — full chat list collected inside ChatsTab (PERF F7).
    val unreadTotal by remember {
        InMemoryChatStore.chats
            .map { list -> list.filterNot { it.archived }.sumOf { it.unread } }
            .distinctUntilChanged()
    }.collectAsState(initial = 0)

    LaunchedEffect(Unit) {
        val token = SessionStore.accessToken(context)
        if (token.isNullOrBlank()) {
            InMemoryChatStore.useOfflineSeed()
            return@LaunchedEffect
        }
        // PERF C2: prefs + message-file hydrate off Main; StateFlow still updates UI.
        // StartupWarm often already filled the list — these are no-ops when warm.
        withContext(Dispatchers.IO) {
            InMemoryChatStore.hydrateFromCache(
                com.aivora.app.data.local.ChatListCache.load(context),
            )
            ChatRepository.hydrateRecentFromDisk(context)
        }
        // WS after first paint (PresenceLifecycle.allowConnect); idempotent if already up.
        PresenceLifecycle.allowConnect(context)
        RealtimeClient.startFromSession(context)
        // Defer HTTP so first home frames are not competing with list fetch (PERF F8).
        kotlinx.coroutines.delay(100)
        val api = ChatRepository.api()
        try {
            if (SessionStore.userId(context) == null) {
                runCatching { api.getMe().id?.let { SessionStore.saveUserId(context, it) } }
            }
            ChatRepository.refreshConversations(context)
            ChatRepository.hydrateRecentFromDiskAsync(context)
            // Prefetch uncached top chats in background (first-open black screen).
            launch { ChatRepository.warmRecentThreads(context) }
        } catch (_: Exception) {
            /* keep cache / last remote list; do not flash seed while logged in */
        }
        // Backup poll only while foreground + WS down.
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                kotlinx.coroutines.delay(20_000)
                if (RealtimeClient.connected.value) continue
                ChatRepository.refreshConversations(context)
            }
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                if (HomeShellState.selectedTab != page) {
                    HomeShellState.selectedTab = page
                    query = ""
                    menuOpen = false
                }
            }
    }

    // Instant tab snap — animated pager fling was a big source of input latency (PERF F8).
    val pageSnap = remember {
        androidx.compose.animation.core.snap<Float>()
    }

    if (showCreateCallLink) {
        CreateCallLinkDialog(onDismiss = { showCreateCallLink = false })
    }
    if (showClearCallLog) {
        ClearCallLogDialog(
            onDismiss = { showClearCallLog = false },
            onConfirm = { callLogCleared = true },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (settled) {
                            1 -> "Updates"
                            else -> "Aivora"
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = AivoraColors.Ink,
                    )
                },
                actions = {
                    when (settled) {
                        0 -> {
                            IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                                Icon(AppIcons.CameraAlt, contentDescription = "Camera")
                            }
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(
                                        painter = painterResource(AivoraIcons.More),
                                        contentDescription = "Menu",
                                    )
                                }
                                AivoraOverflowMenu(
                                    expanded = menuOpen,
                                    onDismiss = { menuOpen = false },
                                    items = listOf(
                                        OverflowMenuItem("New group", AppIcons.GroupAdd) {
                                            onNavigate(Routes.NewGroup)
                                        },
                                        OverflowMenuItem("Chat privacy", AppIcons.Lock) {
                                            onNavigate(Routes.SettingsPrivacy)
                                        },
                                        OverflowMenuItem("Favourites", AppIcons.Star) {
                                            filter = ChatFilter.Favourites
                                            scope.launch {
                                                pagerState.animateScrollToPage(0, animationSpec = pageSnap)
                                            }
                                        },
                                        OverflowMenuItem("Linked devices", AppIcons.Devices) {
                                            onNavigate(Routes.SettingsDevices)
                                        },
                                        OverflowMenuItem("Settings", AppIcons.Settings) {
                                            onNavigate(Routes.SettingsProfile)
                                        },
                                    ),
                                )
                            }
                        }
                        1 -> {
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(
                                        painter = painterResource(AivoraIcons.More),
                                        contentDescription = "Menu",
                                    )
                                }
                                AivoraOverflowMenu(
                                    expanded = menuOpen,
                                    onDismiss = { menuOpen = false },
                                    items = listOf(
                                        OverflowMenuItem("Status privacy", AppIcons.Lock) {
                                            onNavigate(Routes.SettingsPrivacyStatus)
                                        },
                                        OverflowMenuItem("Settings", AppIcons.Settings) {
                                            onNavigate(Routes.SettingsProfile)
                                        },
                                    ),
                                )
                            }
                        }
                        2 -> {
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(painter = painterResource(AivoraIcons.More), contentDescription = "Menu")
                                }
                                AivoraOverflowMenu(
                                    expanded = menuOpen,
                                    onDismiss = { menuOpen = false },
                                    items = listOf(
                                        OverflowMenuItem("Create call link", AppIcons.Link) {
                                            showCreateCallLink = true
                                        },
                                        OverflowMenuItem("Clear call log", AppIcons.Delete, danger = true) {
                                            showClearCallLog = true
                                        },
                                    ),
                                )
                            }
                        }
                        else -> {
                            IconButton(onClick = { onNavigate(Routes.SettingsQr) }) {
                                Icon(painter = painterResource(AivoraIcons.More), contentDescription = "Menu")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                expandedHeight = 52.dp,
            )
        },
        bottomBar = {
            // Read offset here only — do not lift into HomeScreen body (list thrash on swipe).
            val selectionProgress =
                pagerState.currentPage + pagerState.currentPageOffsetFraction
            AivoraBottomNav(
                selected = settled,
                selectionProgress = selectionProgress,
                unreadCount = unreadTotal,
                onSelect = { index ->
                    if (pagerState.settledPage != index) {
                        haptics.tick()
                        query = ""
                        scope.launch {
                            // Instant jump — no swipe through Updates/Calls when hopping tabs.
                            pagerState.scrollToPage(index)
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            when (settled) {
                0 -> HomeFabRes(AivoraIcons.Edit, "New chat") {
                    haptics.tick()
                    onNavigate(Routes.NewChat)
                }
                1 -> HomeFabRes(AivoraIcons.Camera, "Camera status") {
                    haptics.tick()
                    onNavigate(Routes.camera("status"))
                }
                2 -> HomeFabRes(AivoraIcons.Call, "New call") {
                    haptics.tick()
                    onNavigate(Routes.NewCall)
                }
            }
        },
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
            beyondViewportPageCount = 0,
            userScrollEnabled = true,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = pageSnap,
            ),
            key = { it },
        ) { page ->
            when (page) {
                0 -> ChatsTab(
                    query = query,
                    onQueryChange = { query = it },
                    filter = filter,
                    onFilterChange = { filter = it },
                    onNavigate = onNavigate,
                    onOpenChat = { id ->
                        haptics.tick()
                        onNavigate(Routes.ChatId.replace("{id}", id))
                    },
                    selected = pagerState.currentPage == 0,
                )
                1 -> StatusTab(
                    query = query,
                    onQueryChange = { query = it },
                    onNavigate = onNavigate,
                    selected = pagerState.currentPage == 1,
                )
                2 -> CallsTab(
                    query = query,
                    onQueryChange = { query = it },
                    filter = callFilter,
                    onFilterChange = { callFilter = it },
                    onNavigate = onNavigate,
                    callLogCleared = callLogCleared,
                    selected = pagerState.currentPage == 2,
                )
                else -> ProfileTab(
                    query = query,
                    onQueryChange = { query = it },
                    onNavigate = onNavigate,
                )
            }
        }
    }
}

@Composable
private fun HomeFab(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AivoraColors.Primary,
        contentColor = AivoraColors.OnPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
        ),
        modifier = Modifier.size(AivoraDimens.FabSize),
        shape = CircleShape,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HomeFabRes(
    @androidx.annotation.DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AivoraColors.Primary,
        contentColor = AivoraColors.OnPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
        ),
        modifier = Modifier.size(AivoraDimens.FabSize),
        shape = CircleShape,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = AivoraColors.OnPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTab(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ChatFilter,
    onFilterChange: (ChatFilter) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    selected: Boolean = true,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAivoraHaptics()
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var menuChat by remember { mutableStateOf<LocalChat?>(null) }
    // PERF M6: stop collecting chat list while another home tab is settled.
    var allChats by remember { mutableStateOf(InMemoryChatStore.chats.value) }
    LaunchedEffect(selected) {
        if (!selected) return@LaunchedEffect
        InMemoryChatStore.chats.collect { allChats = it }
    }

    val chats = remember(allChats, query, filter) {
        allChats
            .asSequence()
            .filter { chat ->
                when (filter) {
                    ChatFilter.All -> !chat.archived
                    ChatFilter.Unread -> !chat.archived && chat.unread > 0
                    ChatFilter.Favourites -> !chat.archived && chat.favorite
                    ChatFilter.Groups -> !chat.archived && chat.isGroup
                    ChatFilter.Archived -> chat.archived
                }
            }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    it.preview.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<LocalChat> { it.pinned }
                    .thenByDescending { it.unread > 0 },
            )
            .toList()
    }
    val barFilters = listOf(
        ChatFilter.All,
        ChatFilter.Unread,
        ChatFilter.Groups,
        ChatFilter.Archived,
    )
    val unreadBadge = remember(allChats) {
        allChats.count { !it.archived && it.unread > 0 }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeSearchBar(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search chats, people, media",
        )
        Spacer(Modifier.height(6.dp))
        AivoraFilterBar(
            options = listOf(
                AivoraFilterOption("All"),
                AivoraFilterOption("Unread", badge = unreadBadge.takeIf { it > 0 }),
                AivoraFilterOption("Groups"),
                AivoraFilterOption("Archived"),
            ),
            selectedIndex = barFilters.indexOf(filter).coerceAtLeast(0),
            onSelect = { onFilterChange(barFilters[it]) },
        )
        Spacer(Modifier.height(4.dp))
        val openChatState = rememberUpdatedState(onOpenChat)
        val navigateState = rememberUpdatedState(onNavigate)
        val chatAdapter = remember {
            ChatListAdapter(
                onOpen = { id -> openChatState.value(id) },
                onLongPress = { chat ->
                    haptics.tick()
                    menuChat = chat
                },
            )
        }
        val lockedFooter = remember {
            LockedChatsFooterAdapter { navigateState.value(Routes.LockedChats) }
        }
        lockedFooter.visible = filter == ChatFilter.All
        val homeConcat = remember { ConcatAdapter(chatAdapter, lockedFooter) }
        if (chats.isEmpty() && filter != ChatFilter.All) {
            Text(
                "No chats here",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                color = AivoraColors.TextSecondary,
            )
        } else {
            RecyclerHost(
                modifier = Modifier.fillMaxSize(),
                onRecyclerReady = { rv ->
                    rv.adapter = homeConcat
                    val d = rv.resources.displayMetrics.density
                    rv.setPadding(0, (2 * d).toInt(), 0, (8 * d).toInt())
                },
                update = { rv ->
                    if (rv.adapter !== homeConcat) rv.adapter = homeConcat
                    lockedFooter.visible = filter == ChatFilter.All
                    chatAdapter.submitList(chats)
                },
            )
        }
    }

    menuChat?.let { target ->
        val live = allChats.find { it.id == target.id } ?: target
        AivoraBottomSheet(
            onDismiss = { menuChat = null },
            title = live.title,
        ) {
            AivoraSheetActionList(
                actions = ChatControls.sheetActions(
                    chat = live,
                    scope = scope,
                    context = context,
                    api = api,
                    useRemote = useRemote,
                    includeSearch = false,
                    includeClear = false,
                    includeReport = !live.isGroup,
                    onDismiss = { menuChat = null },
                    onViewInfo = {
                        onNavigate(Routes.ProfileId.replace("{id}", live.id))
                    },
                    onReport = {
                        scope.launch {
                            runCatching {
                                api.postReports(
                                    ReportRequestBody(
                                        targetUserId = live.peerUserId,
                                        targetConversationId = live.id,
                                        reason = "Reported from chats list",
                                        messageIds = emptyList(),
                                        block = false,
                                    ),
                                )
                            }.onSuccess {
                                Toast.makeText(context, "Report sent", Toast.LENGTH_SHORT).show()
                            }.onFailure {
                                Toast.makeText(context, "Could not send report", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onAfterArchive = { archived ->
                        if (archived && filter != ChatFilter.Archived) {
                            // Stay on list; archived chat leaves All/Unread/Groups naturally.
                        }
                    },
                ),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onSpeakClick: () -> Unit = {},
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space1)
            .height(AivoraDimens.SearchHeight),
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = {
            Icon(
                painter = painterResource(AivoraIcons.Search),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = AivoraColors.TextTertiary,
            )
        },
        trailingIcon = {
            IconButton(onClick = onSpeakClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    painter = painterResource(AivoraIcons.Mic),
                    contentDescription = "Speak",
                    modifier = Modifier.size(22.dp),
                    tint = AivoraColors.TextTertiary,
                )
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
        shape = RoundedCornerShape(AivoraDimens.RadiusSm),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = AivoraColors.SearchFill.copy(alpha = 0.7f),
            unfocusedContainerColor = AivoraColors.SearchFill.copy(alpha = 0.7f),
            focusedBorderColor = AivoraColors.Primary.copy(alpha = 0.25f),
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
        ),
    )
}

@Composable
private fun HomeChatRow(
    chat: LocalChat,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
) {
    val open by rememberUpdatedState(onOpen)
    val longPress by rememberUpdatedState(onLongPress)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(chat.id) {
                detectTapGestures(
                    onTap = { open() },
                    onLongPress = { longPress() },
                )
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(chat.title, size = 52)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = AivoraColors.Ink,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (chat.pinned) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        AppIcons.PushPin,
                        contentDescription = "Pinned",
                        tint = AivoraColors.TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
                if (chat.isMuted) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        AppIcons.NotificationsOff,
                        contentDescription = "Muted",
                        tint = AivoraColors.TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                chat.preview,
                fontSize = 14.sp,
                color = AivoraColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                chat.time,
                fontSize = 12.sp,
                color = if (chat.unread > 0) AivoraColors.Primary else AivoraColors.TextSecondary,
            )
            if (chat.unread > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                UnreadBadge(chat.unread)
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(AivoraColors.Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$count",
            fontSize = 10.sp,
            color = AivoraColors.OnPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = 10.sp,
        )
    }
}

@Composable
private fun StatusTab(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
    selected: Boolean = true,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    val gallery = com.aivora.app.ui.media.rememberMediaActions(context) { uri ->
        com.aivora.app.data.local.PendingMedia.set(uri.toString())
        onNavigate(Routes.NewStatus)
    }
    // PERF H4: hydrate/refresh when Updates is selected — not on every Activity RESUMED.
    // Warm memory: skip disk; defer HTTP so first frame stays painted (cache-first).
    LaunchedEffect(selected, useRemote) {
        if (!selected) return@LaunchedEffect
        val warm = InMemoryStatusStore.statuses.value.isNotEmpty()
        if (!warm) {
            withContext(Dispatchers.IO) {
                StatusRepository.hydrateFromDisk(context)
            }
        } else {
            delay(400)
        }
        if (useRemote) StatusRepository.refresh(context, api)
    }
    // PERF M6: cancel status flow while Updates tab is not settled.
    var statuses by remember { mutableStateOf(InMemoryStatusStore.statuses.value) }
    LaunchedEffect(selected) {
        if (!selected) return@LaunchedEffect
        InMemoryStatusStore.statuses.collect { statuses = it }
    }
    val mine = remember(statuses) { InMemoryStatusStore.myStatuses() }
    // One row per author (WhatsApp-style), not one row per status item.
    val recent = remember(statuses, query) {
        InMemoryStatusStore.recent()
            .filter { query.isBlank() || it.authorName.contains(query, ignoreCase = true) }
            .groupBy { it.authorId }
            .map { (_, items) -> items.first() }
    }
    val viewed = remember(statuses, query) {
        InMemoryStatusStore.viewed()
            .filter { query.isBlank() || it.authorName.contains(query, ignoreCase = true) }
            .groupBy { it.authorId }
            .map { (_, items) -> items.first() }
            .filter { author -> recent.none { it.authorId == author.authorId } }
    }
    val myName = "You"

    AivoraNoOverscroll {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val subtitle = when {
                mine.isEmpty() -> "Tap to add a status update"
                mine.size == 1 -> mine.first().timeLabel
                else -> "${mine.size} updates · ${mine.first().timeLabel}"
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val oldest = mine.minByOrNull { it.createdAtMs }
                        if (oldest != null) onNavigate(Routes.status(oldest.id))
                        else onNavigate(Routes.NewStatus)
                    }
                    .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar(
                    name = myName,
                    hasUnseen = mine.isNotEmpty(),
                    segmentCount = mine.size.coerceAtLeast(1),
                    size = 52.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "My status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AivoraColors.Ink,
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AivoraColors.TextSecondary,
                    )
                }
                if (mine.isNotEmpty()) {
                    IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                        Icon(
                            AppIcons.CameraAlt,
                            contentDescription = "Add status",
                            tint = AivoraColors.Primary,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2),
                horizontalArrangement = Arrangement.spacedBy(AivoraDimens.Space2),
            ) {
                StatusActionPill(
                    icon = AppIcons.PhotoLibrary,
                    label = "Gallery",
                    modifier = Modifier.weight(1f),
                    onClick = { gallery.pickGallery() },
                )
                StatusActionPill(
                    iconRes = AivoraIcons.Edit,
                    label = "Text",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.NewStatus) },
                )
            }
            Text(
                "Recent",
                modifier = Modifier.padding(
                    horizontal = AivoraDimens.ScreenHorizontal,
                    vertical = AivoraDimens.Space3,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AivoraColors.Ink,
            )
            if (recent.isEmpty() && viewed.isEmpty()) {
                Text(
                    "Status updates from people you have saved appear here for 24 hours.",
                    modifier = Modifier.padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 4.dp),
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        items(recent, key = { it.authorId }) { status ->
            val authorItems = InMemoryStatusStore.statusesForAuthor(status.authorId, mine = false)
            val unseenCount = authorItems.count { !it.viewed }
            val openId = authorItems.firstOrNull { !it.viewed }?.id
                ?: authorItems.firstOrNull()?.id
                ?: status.id
            StatusRow(
                name = status.authorName,
                time = if (authorItems.size > 1) {
                    "${authorItems.size} updates · ${status.timeLabel}"
                } else {
                    status.timeLabel
                },
                unseen = unseenCount > 0,
                segmentCount = authorItems.size,
            ) {
                onNavigate(Routes.status(openId))
            }
        }
        if (viewed.isNotEmpty()) {
            item {
                Text(
                    "Viewed",
                    modifier = Modifier.padding(
                        horizontal = AivoraDimens.ScreenHorizontal,
                        vertical = AivoraDimens.Space2,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AivoraColors.Ink,
                )
            }
            items(viewed, key = { "v-${it.authorId}" }) { status ->
                val authorItems = InMemoryStatusStore.statusesForAuthor(status.authorId, mine = false)
                StatusRow(
                    name = status.authorName,
                    time = status.timeLabel,
                    unseen = false,
                    segmentCount = authorItems.size,
                ) {
                    onNavigate(Routes.status(authorItems.firstOrNull()?.id ?: status.id))
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
    }
}

@Composable
private fun StatusActionPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    @androidx.annotation.DrawableRes iconRes: Int? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(AivoraDimens.RadiusFull))
            .background(AivoraColors.PrimarySoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = AivoraColors.Primary,
                modifier = Modifier.size(18.dp),
            )
        } else if (icon != null) {
            Icon(icon, null, tint = AivoraColors.Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            color = AivoraColors.Primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun StatusRow(
    name: String,
    time: String,
    unseen: Boolean,
    segmentCount: Int = 1,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusRingAvatar(
            name = name,
            hasUnseen = unseen,
            segmentCount = segmentCount,
            size = 52.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.Ink,
            )
            Text(time, style = MaterialTheme.typography.bodyMedium, color = AivoraColors.TextSecondary)
        }
    }
}

@Composable
private fun CallsTab(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: CallFilter,
    onFilterChange: (CallFilter) -> Unit,
    onNavigate: (String) -> Unit,
    callLogCleared: Boolean = false,
    selected: Boolean = true,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    val items by com.aivora.app.data.local.InMemoryCallHistoryStore.items.collectAsState()
    // Only show Loading when we have nothing to paint — WhatsApp never blanks a warm tab.
    var loading by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryCallHistoryStore.items.value.isEmpty())
    }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()

    // PERF M6: fetch call history only while Calls tab is selected.
    // Keep last list on screen; refresh in place (no Loading wipe).
    LaunchedEffect(selected, useRemote, callLogCleared) {
        if (!selected) return@LaunchedEffect
        if (!useRemote || callLogCleared) {
            com.aivora.app.data.local.InMemoryCallHistoryStore.clear()
            loading = false
            return@LaunchedEffect
        }
        val warm = com.aivora.app.data.local.InMemoryCallHistoryStore.items.value
        loading = warm.isEmpty()
        val fresh = runCatching { api.getCallsHistory().items }.getOrDefault(warm)
        com.aivora.app.data.local.InMemoryCallHistoryStore.replace(fresh)
        loading = false
    }

    fun formatCallTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            val instant = java.time.Instant.parse(iso)
            val z = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            val today = java.time.LocalDate.now()
            when (z.toLocalDate()) {
                today -> z.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
                today.minusDays(1) -> "Yesterday"
                else -> z.format(java.time.format.DateTimeFormatter.ofPattern("EEE"))
            }
        }.getOrDefault(iso.take(16))
    }

    val rows = remember(items) {
        items.map { h ->
            val detail = when {
                h.missed && h.video -> "Missed video call"
                h.missed -> "Missed voice call"
                h.direction == "outgoing" && h.video -> "Outgoing video"
                h.direction == "outgoing" -> "Outgoing"
                h.video -> "Incoming video"
                else -> "Incoming"
            }.let { base ->
                if (h.durationSeconds > 0) {
                    val m = h.durationSeconds / 60
                    val s = h.durationSeconds % 60
                    "$base · ${m}:${s.toString().padStart(2, '0')}"
                } else base
            }
            CallRow(
                id = h.id.orEmpty(),
                name = h.peerName ?: "Unknown",
                detail = detail,
                missed = h.missed,
                video = h.video,
                time = formatCallTime(h.startedAt),
                conversationId = h.conversationId,
            )
        }
    }

    val filtered = remember(rows, query, filter, callLogCleared) {
        if (callLogCleared) emptyList()
        else rows.filter { row ->
            val matchesQuery = query.isBlank() ||
                row.name.contains(query, ignoreCase = true) ||
                row.detail.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                CallFilter.All -> true
                CallFilter.Missed -> row.missed
                CallFilter.Voice -> !row.video
                CallFilter.Video -> row.video
            }
            matchesQuery && matchesFilter
        }
    }

    fun redial(row: CallRow) {
        val chatId = row.conversationId
        if (chatId.isNullOrBlank() || !useRemote) {
            onNavigate(if (row.video) Routes.videoCall(row.name) else Routes.audioCall(row.name))
            return
        }
        scope.launch {
            Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()
            val result = runCatching {
                api.postCalls(
                    com.aivora.app.data.model.CallStartBody(
                        conversationId = chatId,
                        media = if (row.video) "video" else "audio",
                    ),
                )
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
            com.aivora.app.data.call.ActiveCallSession.setActive(
                callId = callId,
                token = token,
                livekitUrl = url,
                media = if (row.video) "video" else "audio",
                peerName = row.name,
                asCaller = true,
            )
            onNavigate(if (row.video) Routes.videoCall(row.name) else Routes.audioCall(row.name))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeSearchBar(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search calls, contacts",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CallQuickAction(AppIcons.Call, "New call") { onNavigate(Routes.NewCall) }
            CallQuickAction(AppIcons.Event, "Schedule") { onNavigate(Routes.ScheduledCalls) }
        }
        Spacer(Modifier.height(6.dp))
        AivoraFilterBar(
            options = CallFilter.entries.map { AivoraFilterOption(it.label) },
            selectedIndex = CallFilter.entries.indexOf(filter).coerceAtLeast(0),
            onSelect = { onFilterChange(CallFilter.entries[it]) },
        )
        Spacer(Modifier.height(4.dp))
        val callItems = remember(filtered) {
            filtered.map {
                CallListItem(
                    id = it.id.ifBlank { it.name + it.time },
                    name = it.name,
                    detail = it.detail,
                    missed = it.missed,
                    video = it.video,
                    time = it.time,
                )
            }
        }
        val callRowsById = remember(filtered) {
            filtered.associateBy { it.id.ifBlank { it.name + it.time } }
        }
        val clickRef = remember { arrayOf<(CallListItem) -> Unit>({}) }
        clickRef[0] = { item -> callRowsById[item.id]?.let { redial(it) } }
        val callAdapter = remember {
            CallListAdapter { item -> clickRef[0](item) }
        }
        when {
            loading -> Text(
                "Loading…",
                modifier = Modifier.padding(AivoraDimens.ScreenHorizontal, 24.dp),
                color = AivoraColors.TextSecondary,
            )
            filtered.isEmpty() -> Text(
                "No calls yet",
                modifier = Modifier.padding(AivoraDimens.ScreenHorizontal, 24.dp),
                color = AivoraColors.TextSecondary,
            )
            else -> RecyclerHost(
                modifier = Modifier.fillMaxSize(),
                onRecyclerReady = { rv -> rv.adapter = callAdapter },
                update = { rv ->
                    if (rv.adapter !== callAdapter) rv.adapter = callAdapter
                    callAdapter.submitList(callItems)
                },
            )
        }
    }
}

@Composable
private fun CallQuickAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ProfileTab(
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigate: (String) -> Unit,
) {
    val filteredSections = remember(query) {
        val all = listOf(
            null to listOf(
                SettingsItem(
                    title = "Lists",
                    iconRes = AivoraIcons.Group,
                    onClick = { onNavigate(Routes.SettingsListsNew) },
                ),
                SettingsItem(
                    title = "Starred",
                    iconRes = AivoraIcons.Star,
                    onClick = { onNavigate(Routes.SettingsStarred) },
                ),
            ),
            null to listOf(
                SettingsItem(
                    title = "Account",
                    iconRes = AivoraIcons.Key,
                    onClick = { onNavigate(Routes.SettingsAccount) },
                ),
                SettingsItem(
                    title = "Privacy",
                    iconRes = AivoraIcons.Lock,
                    onClick = { onNavigate(Routes.SettingsPrivacy) },
                ),
                SettingsItem(
                    title = "Chats",
                    iconRes = AivoraIcons.Chat,
                    badge = "1",
                    onClick = { onNavigate(Routes.SettingsChats) },
                ),
                SettingsItem(
                    title = "Appearance",
                    iconRes = AivoraIcons.Palette,
                    onClick = { onNavigate(Routes.SettingsAppearance) },
                ),
                SettingsItem(
                    title = "Notifications",
                    iconRes = AivoraIcons.Notifications,
                    onClick = { onNavigate(Routes.SettingsNotifications) },
                ),
                SettingsItem(
                    title = "Storage and data",
                    iconRes = AivoraIcons.Storage,
                    onClick = { onNavigate(Routes.SettingsStorage) },
                ),
            ),
            null to listOf(
                SettingsItem(
                    title = "Help and feedback",
                    iconRes = AivoraIcons.Help,
                    onClick = { onNavigate(Routes.HelpFeedback) },
                ),
                SettingsItem(
                    title = "Invite a friend",
                    iconRes = AivoraIcons.Favorite,
                    onClick = { onNavigate(Routes.InviteFriend) },
                ),
            ),
        )
        if (query.isBlank()) all
        else all.mapNotNull { (header, items) ->
            val match = items.filter { it.title.contains(query, ignoreCase = true) }
            if (match.isEmpty()) null else header to match
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeSearchBar(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search profile, settings",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "My Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.Ink,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onNavigate(Routes.SettingsQr) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.SurfaceMuted),
            ) {
                Icon(
                    painter = painterResource(AivoraIcons.Qr),
                    contentDescription = "QR code",
                    tint = AivoraColors.Ink,
                )
            }
            Spacer(modifier = Modifier.width(AivoraDimens.Space2))
            IconButton(
                onClick = { onNavigate(Routes.YourDetails) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.PrimarySoft),
            ) {
                Icon(
                    painter = painterResource(AivoraIcons.Edit),
                    contentDescription = "Edit",
                    tint = AivoraColors.Primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(AivoraDimens.RadiusFull))
                .background(AivoraColors.SurfaceMuted)
                .clickable { }
                .padding(horizontal = 14.dp, vertical = AivoraDimens.Space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Right now I’m…",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
            )
            Spacer(modifier = Modifier.width(AivoraDimens.Space2))
            Icon(
                painter = painterResource(AivoraIcons.Add),
                contentDescription = null,
                tint = AivoraColors.TextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AivoraDimens.Space4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar("Leslie", size = 96)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Leslie",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AivoraColors.Ink,
            )
        }
        SettingsList(filteredSections, modifier = Modifier.weight(1f))
    }
}
