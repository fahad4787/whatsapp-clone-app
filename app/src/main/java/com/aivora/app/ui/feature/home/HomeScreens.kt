package com.aivora.app.ui.feature.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalChat
import com.aivora.app.ui.components.AivoraBottomNav
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.StatusRingAvatar
import com.aivora.app.ui.components.ThemeFilterChip
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val haptics = rememberAivoraHaptics()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = HomeShellState.selectedTab.coerceIn(0, 3),
        pageCount = { 4 },
    )
    val tab = pagerState.currentPage
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }
    var callFilter by remember { mutableStateOf(CallFilter.All) }
    var menuOpen by remember { mutableStateOf(false) }
    val allChats by InMemoryChatStore.chats.collectAsState()
    val unreadTotal = remember(allChats) {
        allChats.filterNot { it.archived }.sumOf { it.unread }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (HomeShellState.selectedTab != page) {
                    HomeShellState.selectedTab = page
                    query = ""
                }
            }
    }
    LaunchedEffect(HomeShellState.selectedTab) {
        val target = HomeShellState.selectedTab.coerceIn(0, 3)
        if (pagerState.currentPage != target && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Aivora",
                        fontWeight = FontWeight.Bold,
                        color = if (tab == 0) AivoraColors.Primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    when (tab) {
                        0, 1 -> {
                            IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            }
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                ChatsOverflowMenu(
                                    expanded = menuOpen,
                                    onDismiss = { menuOpen = false },
                                    onNavigate = { route ->
                                        menuOpen = false
                                        onNavigate(route)
                                    },
                                    onFavourites = {
                                        menuOpen = false
                                        filter = ChatFilter.Favourites
                                        HomeShellState.selectedTab = 0
                                    },
                                )
                            }
                        }
                        2 -> {
                            Box {
                                IconButton(onClick = { menuOpen = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                    DropdownMenuItem(
                                        text = { Text("Clear call log") },
                                        onClick = { menuOpen = false },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Settings") },
                                        onClick = {
                                            menuOpen = false
                                            onNavigate(Routes.SettingsProfile)
                                        },
                                    )
                                }
                            }
                        }
                        else -> {
                            IconButton(onClick = { onNavigate(Routes.SettingsQr) }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
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
                selected = tab,
                unreadCount = unreadTotal,
                onSelect = { index ->
                    if (tab != index) {
                        haptics.tick()
                        query = ""
                        HomeShellState.selectedTab = index
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                },
            )
        },
        floatingActionButton = {
            when (tab) {
                0 -> HomeFab(Icons.Default.Edit, "New chat") {
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
            beyondViewportPageCount = 1,
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
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
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
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ChatsOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit,
    onFavourites: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text("New group") },
            leadingIcon = { Icon(Icons.Default.GroupAdd, null, tint = AivoraColors.Primary) },
            onClick = { onNavigate(Routes.NewGroup) },
        )
        DropdownMenuItem(
            text = { Text("Chat privacy") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = AivoraColors.Primary) },
            onClick = { onNavigate(Routes.SettingsPrivacy) },
        )
        DropdownMenuItem(
            text = { Text("Favourites") },
            leadingIcon = { Icon(Icons.Default.Star, null, tint = AivoraColors.Primary) },
            onClick = onFavourites,
        )
        DropdownMenuItem(
            text = { Text("Linked devices") },
            leadingIcon = { Icon(Icons.Outlined.Devices, null, tint = AivoraColors.Primary) },
            onClick = { onNavigate(Routes.SettingsDevices) },
        )
        DropdownMenuItem(
            text = { Text("Settings") },
            leadingIcon = { Icon(Icons.Default.Settings, null, tint = AivoraColors.Primary) },
            onClick = { onNavigate(Routes.SettingsProfile) },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChatsTab(
    allChats: List<LocalChat>,
    query: String,
    onQueryChange: (String) -> Unit,
    filter: ChatFilter,
    onFilterChange: (ChatFilter) -> Unit,
    onNavigate: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
) {
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
            .toList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeSearchBar(
            value = query,
            onValueChange = onQueryChange,
            placeholder = "Search chats, people, media",
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ChatFilter.entries.forEach { option ->
                ThemeFilterChip(
                    selected = filter == option,
                    onClick = { onFilterChange(option) },
                    label = option.label,
                )
            }
        }
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
                val avatarMod = homeSharedAvatar(
                    sharedTransitionScope,
                    animatedVisibilityScope,
                    chat.id,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(chat.id) }
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(chat.title, size = 44, modifier = avatarMod)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            chat.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            chat.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            chat.time,
                            style = MaterialTheme.typography.labelMedium,
                            fontSize = 11.sp,
                            color = if (chat.unread > 0) AivoraColors.Primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (chat.unread > 0) {
                            Spacer(modifier = Modifier.height(3.dp))
                            UnreadBadge(chat.unread)
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
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AivoraColors.Primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Lock, null, tint = AivoraColors.Primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
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
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(48.dp),
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
            IconButton(onClick = onSpeakClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Mic, contentDescription = "Speak", modifier = Modifier.size(20.dp))
            }
        },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            focusedBorderColor = AivoraColors.Primary.copy(alpha = 0.3f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun homeSharedAvatar(
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
            HomeSearchBar(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Search updates",
            )
            val subtitle = mine.firstOrNull()?.let { "Just posted · ${it.timeLabel}" }
                ?: "Tap to add status update"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val first = mine.firstOrNull()
                        if (first != null) onNavigate(Routes.status(first.id))
                        else onNavigate(Routes.NewStatus)
                    }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar("Me", hasUnseen = mine.isNotEmpty(), size = 48.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My status", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusActionPill(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Gallery",
                    onClick = { onNavigate(Routes.camera("status")) },
                )
                StatusActionPill(
                    icon = Icons.Default.Edit,
                    label = "Text",
                    onClick = { onNavigate(Routes.NewStatus) },
                )
            }
            Text(
                "Recent",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            if (recent.isEmpty() && viewed.isEmpty()) {
                Text(
                    "Status updates from people you have saved appear here for 24 hours.",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
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
                Text(
                    "Viewed updates",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
private fun StatusActionPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AivoraColors.Primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, null, tint = AivoraColors.Primary, modifier = Modifier.size(18.dp))
        Text(label, color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun StatusRow(name: String, time: String, unseen: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusRingAvatar(name, hasUnseen = unseen, size = 48.dp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(time, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val filtered = remember(calls, query, filter) {
        calls.filter { row ->
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
            CallQuickAction(Icons.Default.Dialpad, "Keypad") { onNavigate(Routes.NewCall) }
            CallQuickAction(Icons.Default.StarBorder, "Favorites") {
                onFilterChange(CallFilter.All)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CallFilter.entries.forEach { option ->
                ThemeFilterChip(
                    selected = filter == option,
                    onClick = { onFilterChange(option) },
                    label = option.label,
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.name + it.time }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Routes.NewCall) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(row.name, size = 44)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            row.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = if (row.missed) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            "${row.detail} · ${row.time}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        if (row.video) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = AivoraColors.Primary,
                        modifier = Modifier.size(22.dp),
                    )
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
                SettingsItem("Lists", null, Icons.Default.GroupAdd, onClick = { onNavigate(Routes.SettingsListsNew) }),
                SettingsItem("Starred", null, Icons.Default.Star, onClick = { onNavigate(Routes.SettingsStarred) }),
            ),
            null to listOf(
                SettingsItem("Account", null, Icons.Default.Key, onClick = { onNavigate(Routes.SettingsAccount) }),
                SettingsItem("Privacy", null, Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacy) }),
                SettingsItem("Chats", null, Icons.Default.Edit, onClick = { onNavigate(Routes.SettingsChats) }),
                SettingsItem("Appearance", null, Icons.Default.Settings, onClick = { onNavigate(Routes.SettingsAppearance) }),
                SettingsItem("Notifications", null, Icons.Default.Settings, onClick = { onNavigate(Routes.SettingsNotifications) }),
                SettingsItem("Storage and data", null, Icons.Default.Settings, onClick = { onNavigate(Routes.SettingsStorage) }),
            ),
            null to listOf(
                SettingsItem("Help and feedback", null, Icons.Default.Settings, onClick = { onNavigate(Routes.SettingsPermissions) }),
                SettingsItem("Invite a friend", null, Icons.Default.GroupAdd, onClick = { }),
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "My Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { onNavigate(Routes.SettingsQr) },
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            ) {
                Icon(Icons.Default.QrCode, contentDescription = "QR code")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { onNavigate(Routes.YourDetails) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.Primary.copy(alpha = 0.15f)),
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AivoraColors.Primary)
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable { }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Right now I’m…",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("+", fontWeight = FontWeight.Bold, color = AivoraColors.Primary)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Avatar("You", size = 88)
            Spacer(modifier = Modifier.height(10.dp))
            Text("You", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        SettingsList(filteredSections, modifier = Modifier.weight(1f))
    }
}
