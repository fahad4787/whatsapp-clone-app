package com.aivora.app.ui.feature.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.InMemoryStatusStore
import com.aivora.app.data.local.LocalChat
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.StatusRingAvatar
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

private enum class ChatFilter(val label: String) {
    All("All"),
    Unread("Unread"),
    Favorites("Favorites"),
    Groups("Groups"),
    Archived("Archived"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val haptics = rememberAivoraHaptics()
    var tab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ChatFilter.All) }
    val allChats by InMemoryChatStore.chats.collectAsState()
    val unreadTotal = remember(allChats) {
        allChats.filterNot { it.archived }.sumOf { it.unread }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (tab) {
                            1 -> "Updates"
                            2 -> "Calls"
                            3 -> "Profile"
                            else -> "Aivora"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (tab == 0) AivoraColors.Primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    when (tab) {
                        0 -> {
                            IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                            }
                            IconButton(onClick = { onNavigate(Routes.NewChat) }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }
                        }
                        1 -> IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                        2 -> IconButton(onClick = { onNavigate(Routes.NewCall) }) {
                            Icon(Icons.Default.Add, contentDescription = "New call")
                        }
                        else -> IconButton(onClick = { onNavigate(Routes.SettingsQr) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { if (tab != 0) { haptics.tick(); tab = 0 } },
                    icon = {
                        if (unreadTotal > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(containerColor = AivoraColors.Primary) {
                                        Text(
                                            if (unreadTotal > 99) "99+" else "$unreadTotal",
                                            color = AivoraColors.OnPrimary,
                                        )
                                    }
                                },
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                            }
                        } else {
                            Icon(Icons.Default.Chat, contentDescription = null)
                        }
                    },
                    label = { Text("Chats") },
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { if (tab != 1) { haptics.tick(); tab = 1 } },
                    icon = { Icon(Icons.Outlined.Update, contentDescription = null) },
                    label = { Text("Updates") },
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { if (tab != 2) { haptics.tick(); tab = 2 } },
                    icon = { Icon(Icons.Default.Call, contentDescription = null) },
                    label = { Text("Calls") },
                )
                NavigationBarItem(
                    selected = tab == 3,
                    onClick = { if (tab != 3) { haptics.tick(); tab = 3 } },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile") },
                )
            }
        },
        floatingActionButton = {
            if (tab != 3) {
                FloatingActionButton(
                    onClick = {
                        haptics.tick()
                        when (tab) {
                            1 -> onNavigate(Routes.NewStatus)
                            2 -> onNavigate(Routes.NewCall)
                            else -> onNavigate(Routes.NewChat)
                        }
                    },
                    containerColor = AivoraColors.Primary,
                    contentColor = AivoraColors.OnPrimary,
                ) {
                    Icon(
                        when (tab) {
                            1 -> Icons.Default.CameraAlt
                            2 -> Icons.Default.Call
                            else -> Icons.Default.Add
                        },
                        contentDescription = null,
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding(),
        ) {
            when (tab) {
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
                1 -> StatusTab(onNavigate = onNavigate)
                2 -> CallsTab(onNavigate = onNavigate)
                else -> ProfileTab(onNavigate = onNavigate)
            }
        }
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
                    ChatFilter.Favorites -> !chat.archived && chat.favorite
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
            onSpeakClick = { /* local voice search placeholder */ },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChatFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    onClick = { onFilterChange(option) },
                    label = { Text(option.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AivoraColors.Primary.copy(alpha = 0.16f),
                        selectedLabelColor = AivoraColors.Primary,
                    ),
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            if (chats.isEmpty()) {
                item {
                    Text(
                        "No chats here",
                        modifier = Modifier.padding(24.dp),
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(chat.title, modifier = avatarMod)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            chat.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            chat.preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            chat.time,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (chat.unread > 0) AivoraColors.Primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (chat.unread > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            UnreadBadge(chat.unread)
                        }
                    }
                }
            }
            if (filter == ChatFilter.All) {
                item {
                    PersonRow(
                        "Locked chats",
                        "Authenticate to unlock",
                        onClick = { onNavigate(Routes.LockedChats) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSpeakClick: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onSpeakClick) {
                Icon(Icons.Default.Mic, contentDescription = "Speak")
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            focusedBorderColor = AivoraColors.Primary.copy(alpha = 0.35f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
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
            .size(20.dp)
            .clip(CircleShape)
            .background(AivoraColors.Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$count",
            style = MaterialTheme.typography.labelMedium,
            color = AivoraColors.OnPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatusTab(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    val statuses by InMemoryStatusStore.statuses.collectAsState()
    val mine = remember(statuses) { InMemoryStatusStore.myStatuses() }
    val recent = remember(statuses) { InMemoryStatusStore.recent() }
    val viewed = remember(statuses) { InMemoryStatusStore.viewed() }

    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            val subtitle = mine.firstOrNull()?.let { "${it.timeLabel} · Tap to view" }
                ?: "Tap to add status update"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val first = mine.firstOrNull()
                        if (first != null) onNavigate(Routes.status(first.id))
                        else onNavigate(Routes.NewStatus)
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar("Me", hasUnseen = mine.isNotEmpty())
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("My status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                    Icon(Icons.Default.CameraAlt, null, tint = AivoraColors.Primary)
                }
            }
            Text(
                "Recent updates",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = AivoraColors.Primary,
            )
        }
        items(recent, key = { it.id }) { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Routes.status(status.id)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar(status.authorName, hasUnseen = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(status.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(status.timeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Text(
                "Viewed updates",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(viewed, key = { it.id }) { status ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Routes.status(status.id)) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusRingAvatar(status.authorName, hasUnseen = false)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(status.authorName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(status.timeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                "Status privacy",
                onClick = { onNavigate(Routes.SettingsPrivacyStatus) },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun CallsTab(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            PersonRow(
                "Create call link",
                "Anyone with the link can join this call — no chat needed.",
                onClick = { onNavigate(Routes.NewCall) },
            )
            PersonRow(
                "Scheduled calls",
                "A scheduled call is starting soon",
                onClick = { onNavigate(Routes.ScheduledCalls) },
            )
            Text(
                "Recent",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = AivoraColors.Primary,
            )
        }
        items(
            listOf(
                "Ahmad Khan" to "Outgoing · Yesterday",
                "Fatima Ali" to "Missed video · 2 days ago",
                "Family Group" to "Incoming · Monday",
            ),
        ) { (name, sub) ->
            PersonRow(name, sub, onClick = { onNavigate(Routes.NewCall) }) {
                IconButton(onClick = { onNavigate(Routes.NewCall) }) {
                    Icon(Icons.Default.Call, null, tint = AivoraColors.Primary)
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(Routes.SettingsProfile) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar("You", size = 64)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("You", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Hey there! I am using Aivora.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Account", "Security, change number", Icons.Default.Person, onClick = { onNavigate(Routes.SettingsAccount) }),
                    SettingsItem("Chats", "Theme, wallpapers, history", Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChats) }),
                    SettingsItem("Privacy", "Last seen, profile photo", Icons.Default.MoreVert, onClick = { onNavigate(Routes.SettingsPrivacy) }),
                    SettingsItem("Storage and data", "Network usage", Icons.Default.MoreVert, onClick = { onNavigate(Routes.SettingsStorage) }),
                ),
            ),
        )
    }
}
