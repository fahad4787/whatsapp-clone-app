package com.aivora.app.ui.feature.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Dialpad
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
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalChat
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ReportRequestBody
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.components.AivoraBottomNav
import com.aivora.app.ui.components.AivoraBottomSheet
import com.aivora.app.ui.components.AivoraFilterBar
import com.aivora.app.ui.components.AivoraFilterOption
import com.aivora.app.ui.components.AivoraOverflowMenu
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private enum class ChatFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Favourites("Favourites"),
    Groups("Groups"),
    Archived("Archived"),
}

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
    // Chrome reads settled page only — avoids Scaffold recompose every swipe frame.
    val settled = pagerState.settledPage
    val selectionProgress = pagerState.currentPage + pagerState.currentPageOffsetFraction
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }
    var callFilter by remember { mutableStateOf(CallFilter.All) }
    var menuOpen by remember { mutableStateOf(false) }
    var showCreateCallLink by remember { mutableStateOf(false) }
    var showClearCallLog by remember { mutableStateOf(false) }
    var callLogCleared by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val allChats by InMemoryChatStore.chats.collectAsState()
    val unreadTotal = remember(allChats) {
        allChats.filterNot { it.archived }.sumOf { it.unread }
    }

    LaunchedEffect(Unit) {
        val token = com.aivora.app.data.local.SessionStore.accessToken(context)
        if (token.isNullOrBlank()) {
            InMemoryChatStore.useOfflineSeed()
            return@LaunchedEffect
        }
        // Paint cached list immediately (WhatsApp-style cold start).
        InMemoryChatStore.hydrateFromCache(
            com.aivora.app.data.local.ChatListCache.load(context),
        )
        val api = com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
        try {
            if (com.aivora.app.data.local.SessionStore.userId(context) == null) {
                runCatching { api.getMe().id?.let { com.aivora.app.data.local.SessionStore.saveUserId(context, it) } }
            }
            val remote = api.getConversations().items.map { it.toLocalChat() }
            InMemoryChatStore.replaceFromRemote(remote)
            com.aivora.app.data.local.ChatListCache.save(context, remote)
            com.aivora.app.data.realtime.RealtimeClient.startFromSession(context)
        } catch (_: Exception) {
            /* keep cache / last remote list; do not flash seed while logged in */
        }
        // Backup poll only when WS is down (live updates come from RealtimeClient).
        while (true) {
            kotlinx.coroutines.delay(20_000)
            if (com.aivora.app.data.realtime.RealtimeClient.connected.value) continue
            try {
                val remote = api.getConversations().items.map { it.toLocalChat() }
                InMemoryChatStore.replaceFromRemote(remote)
                com.aivora.app.data.local.ChatListCache.save(context, remote)
            } catch (_: Exception) { /* ignore */ }
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

    val pageSnap = remember {
        androidx.compose.animation.core.tween<Float>(
            durationMillis = 200,
            easing = com.aivora.app.ui.motion.AivoraEmphasizedDecel,
        )
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
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
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
                                        OverflowMenuItem("New group", Icons.Default.GroupAdd) {
                                            onNavigate(Routes.NewGroup)
                                        },
                                        OverflowMenuItem("Chat privacy", Icons.Default.Lock) {
                                            onNavigate(Routes.SettingsPrivacy)
                                        },
                                        OverflowMenuItem("Favourites", Icons.Default.Star) {
                                            filter = ChatFilter.Favourites
                                            scope.launch {
                                                pagerState.animateScrollToPage(0, animationSpec = pageSnap)
                                            }
                                        },
                                        OverflowMenuItem("Linked devices", Icons.Outlined.Devices) {
                                            onNavigate(Routes.SettingsDevices)
                                        },
                                        OverflowMenuItem("Settings", Icons.Default.Settings) {
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
                                        OverflowMenuItem("Settings", Icons.Default.Settings) {
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
                                        OverflowMenuItem("Create call link", Icons.Default.Link) {
                                            showCreateCallLink = true
                                        },
                                        OverflowMenuItem("Clear call log", Icons.Default.Delete, danger = true) {
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
                1 -> HomeFab(Icons.Default.CameraAlt, "New status") {
                    haptics.tick()
                    onNavigate(Routes.NewStatus)
                }
                2 -> HomeFab(Icons.Default.Call, "New call") {
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
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = pageSnap,
            ),
            key = { it },
        ) { page ->
            when (page) {
                0 -> ChatsTab(
                    allChats = allChats,
                    query = query,
                    onQueryChange = { query = it },
                    filter = filter,
                    onFilterChange = { filter = it },
                    onNavigate = onNavigate,
                    onOpenChat = { id ->
                        haptics.tick()
                        onNavigate(Routes.ChatId.replace("{id}", id))
                    },
                )
                1 -> StatusTab(
                    query = query,
                    onQueryChange = { query = it },
                    onNavigate = onNavigate,
                )
                2 -> CallsTab(
                    query = query,
                    onQueryChange = { query = it },
                    filter = callFilter,
                    onFilterChange = { callFilter = it },
                    onNavigate = onNavigate,
                    callLogCleared = callLogCleared,
                    onCreateCallLink = { showCreateCallLink = true },
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ChatsTab(
    allChats: List<LocalChat>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ChatFilter,
    onFilterChange: (ChatFilter) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val haptics = rememberAivoraHaptics()
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var menuChat by remember { mutableStateOf<LocalChat?>(null) }

    val chats = remember(allChats, query, filter) {
        allChats
            .asSequence()
            .filter { chat ->
                when (filter) {
                    ChatFilter.All -> !chat.archived
                    ChatFilter.Unread -> !chat.archived && chat.unread > 0
                    ChatFilter.Favourites -> !chat.archived && (chat.favorite || chat.pinned)
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
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 2.dp, bottom = 8.dp),
        ) {
            if (chats.isEmpty()) {
                item {
                    Text(
                        "No chats here",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(chats, key = { it.id }) { chat ->
                // Live row from store so mute/pin/archive icons update after sheet actions.
                val row = allChats.find { it.id == chat.id } ?: chat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenChat(row.id) },
                            onLongClick = {
                                haptics.tick()
                                menuChat = row
                            },
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(row.title, size = 52)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                row.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = AivoraColors.Ink,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                            if (row.pinned) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = AivoraColors.TextSecondary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            if (row.isMuted) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.NotificationsOff,
                                    contentDescription = "Muted",
                                    tint = AivoraColors.TextSecondary,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            row.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AivoraColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            row.time,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 12.sp,
                            color = if (row.unread > 0) AivoraColors.Primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (row.unread > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            UnreadBadge(row.unread)
                        }
                    }
                }
            }
            if (filter == ChatFilter.All) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(Routes.LockedChats) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(AivoraColors.Primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Lock, null, tint = AivoraColors.Primary)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Locked chats", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(
                                "Authenticate to unlock",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
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
) {
    val statuses by InMemoryStatusStore.statuses.collectAsState()
    val mine = remember(statuses) { InMemoryStatusStore.myStatuses() }
    val recent = remember(statuses, query) {
        InMemoryStatusStore.recent().filter {
            query.isBlank() || it.authorName.contains(query, ignoreCase = true)
        }
    }
    val viewed = remember(statuses, query) {
        InMemoryStatusStore.viewed().filter {
            query.isBlank() || it.authorName.contains(query, ignoreCase = true)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            val subtitle = mine.firstOrNull()?.let { "Just posted · ${it.timeLabel}" }
                ?: "Share a moment with your contacts"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val first = mine.firstOrNull()
                        if (first != null) onNavigate(Routes.status(first.id))
                        else onNavigate(Routes.NewStatus)
                    }
                    .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar("Me", hasUnseen = mine.isNotEmpty(), size = 52.dp)
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
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2),
                horizontalArrangement = Arrangement.spacedBy(AivoraDimens.Space2),
            ) {
                StatusActionPill(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Gallery",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Routes.camera("status")) },
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
        items(recent, key = { it.id }) { status ->
            StatusRow(status.authorName, status.timeLabel, unseen = true) {
                onNavigate(Routes.status(status.id))
            }
        }
        if (viewed.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AivoraDimens.ScreenHorizontal,
                            vertical = AivoraDimens.Space2,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Viewed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AivoraColors.Ink,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${viewed.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AivoraColors.TextSecondary,
                    )
                }
            }
            items(viewed, key = { it.id }) { status ->
                StatusRow(status.authorName, status.timeLabel, unseen = false) {
                    onNavigate(Routes.status(status.id))
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
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
private fun StatusRow(name: String, time: String, unseen: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusRingAvatar(name, hasUnseen = unseen, size = 52.dp)
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
        Icon(
            painter = painterResource(AivoraIcons.ChevronRight),
            contentDescription = null,
            tint = AivoraColors.IconSecondary,
            modifier = Modifier.size(20.dp),
        )
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
    onCreateCallLink: () -> Unit = {},
) {
    data class CallRow(
        val name: String,
        val detail: String,
        val missed: Boolean,
        val video: Boolean,
        val time: String,
    )
    val calls = remember {
        listOf(
            CallRow("mubasir", "Missed voice call", true, false, "10:49 pm"),
            CallRow("B Chaudhry", "Outgoing video", false, true, "Yesterday"),
            CallRow("Hamza (You)", "Missed video call", true, true, "Yesterday"),
            CallRow("Ahmad Khan", "Outgoing", false, false, "Monday"),
            CallRow("Fatima Ali", "Incoming", false, false, "Sunday"),
        )
    }
    val filtered = remember(calls, query, filter, callLogCleared) {
        if (callLogCleared) emptyList()
        else calls.filter { row ->
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
            CallQuickAction(Icons.Default.Call, "Call") { onNavigate(Routes.NewCall) }
            CallQuickAction(Icons.Default.Event, "Schedule") { onNavigate(Routes.ScheduledCalls) }
            CallQuickAction(Icons.Default.Dialpad, "Keypad") { onNavigate(Routes.Keypad) }
            CallQuickAction(Icons.Default.StarBorder, "Favorites") { onCreateCallLink() }
        }
        Spacer(Modifier.height(6.dp))
        AivoraFilterBar(
            options = CallFilter.entries.map { AivoraFilterOption(it.label) },
            selectedIndex = CallFilter.entries.indexOf(filter).coerceAtLeast(0),
            onSelect = { onFilterChange(CallFilter.entries[it]) },
        )
        Spacer(Modifier.height(4.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.name + it.time }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onNavigate(
                                if (row.video) Routes.videoCall(row.name) else Routes.audioCall(row.name),
                            )
                        }
                        .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(row.name, size = 48)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            row.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (row.missed) AivoraColors.Danger else AivoraColors.Ink,
                        )
                        Text(
                            row.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AivoraColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            row.time,
                            style = MaterialTheme.typography.labelMedium,
                            color = AivoraColors.TextSecondary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Icon(
                            if (row.video) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = null,
                            tint = AivoraColors.Primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
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
