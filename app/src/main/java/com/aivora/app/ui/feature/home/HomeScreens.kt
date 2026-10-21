package com.aivora.app.ui.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.StatusRingAvatar
import com.aivora.app.ui.motion.AivoraTweenFast
import com.aivora.app.ui.motion.rememberAivoraHaptics
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
) {
    val haptics = rememberAivoraHaptics()
    var tab by remember { mutableIntStateOf(0) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    val allChats by InMemoryChatStore.chats.collectAsState()
    val chats = remember(allChats, query) {
        allChats.filter {
            query.isBlank() ||
                it.title.contains(query, ignoreCase = true) ||
                it.preview.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            searching -> "Search"
                            tab == 1 -> "Status"
                            tab == 2 -> "Communities"
                            tab == 3 -> "Calls"
                            else -> "Aivora"
                        },
                        fontWeight = FontWeight.Bold,
                        color = if (tab == 0 && !searching) AivoraColors.Primary else MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    if (searching) {
                        IconButton(onClick = { searching = false; query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Close search")
                        }
                    } else {
                        IconButton(onClick = { onNavigate(Routes.camera("status")) }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                        IconButton(onClick = { searching = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { onNavigate(Routes.SettingsProfile) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            if (!searching) {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { if (tab != 0) { haptics.tick(); tab = 0 } },
                        icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                        label = { Text("Chats") },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { if (tab != 1) { haptics.tick(); tab = 1 } },
                        icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                        label = { Text("Status") },
                    )
                    NavigationBarItem(
                        selected = tab == 2,
                        onClick = { if (tab != 2) { haptics.tick(); tab = 2 } },
                        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        label = { Text("Communities") },
                    )
                    NavigationBarItem(
                        selected = tab == 3,
                        onClick = { if (tab != 3) { haptics.tick(); tab = 3 } },
                        icon = { Icon(Icons.Default.Call, contentDescription = null) },
                        label = { Text("Calls") },
                    )
                }
            }
        },
        floatingActionButton = {
            if (!searching) {
                FloatingActionButton(
                    onClick = {
                        haptics.tick()
                        when (tab) {
                            1 -> onNavigate(Routes.NewStatus)
                            2 -> onNavigate(Routes.SettingsCommunities)
                            3 -> onNavigate(Routes.NewCall)
                            else -> onNavigate(Routes.NewChat)
                        }
                    },
                    containerColor = AivoraColors.Primary,
                    contentColor = AivoraColors.OnPrimary,
                ) {
                    AnimatedContent(
                        targetState = tab == 1,
                        transitionSpec = {
                            (fadeIn(AivoraTweenFast) + scaleIn(animationSpec = AivoraTweenFast)) togetherWith
                                (fadeOut(AivoraTweenFast) + scaleOut(animationSpec = AivoraTweenFast))
                        },
                        label = "fabIcon",
                    ) { camera ->
                        Icon(
                            if (camera) Icons.Default.CameraAlt else Icons.Default.Add,
                            contentDescription = null,
                        )
                    }
                }
            }
        },
    ) { padding ->
        AnimatedContent(
            targetState = if (searching) 0 else tab,
            modifier = Modifier.padding(padding),
            transitionSpec = {
                fadeIn(AivoraTweenFast) togetherWith fadeOut(AivoraTweenFast)
            },
            label = "homeTab",
        ) { current ->
            when {
                searching || current == 0 -> Column(modifier = Modifier.fillMaxSize()) {
                    if (searching) {
                        SearchField(query, { query = it }, "Search chats")
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        if (!searching) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onNavigate(Routes.Channels) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("Channels", fontWeight = FontWeight.SemiBold, color = AivoraColors.Primary)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("Explore", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
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
                                    .clickable {
                                        haptics.tick()
                                        onNavigate(Routes.ChatId.replace("{id}", chat.id))
                                    }
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
                        if (!searching) {
                            item {
                                PersonRow(
                                    "Locked chats",
                                    "Authenticate to unlock",
                                    onClick = { onNavigate(Routes.LockedChats) },
                                )
                                PersonRow(
                                    "Archived",
                                    "Archive hides chats from your main list",
                                    onClick = { onNavigate(Routes.SettingsChatsArchived) },
                                )
                            }
                        }
                    }
                }
                current == 1 -> StatusTab(onNavigate = onNavigate)
                current == 2 -> CommunitiesTab(onNavigate = onNavigate)
                else -> CallsTab(onNavigate = onNavigate)
            }
        }
    }
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
private fun CommunitiesTab(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "A community keeps related groups together with one announcement group.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton("New community", onClick = { onNavigate(Routes.SettingsCommunities) })
        Spacer(Modifier.height(16.dp))
        PersonRow(
            "Local Neighbors",
            "3 groups · Announcement",
            onClick = { onNavigate(Routes.CommunityId.replace("{id}", "1")) },
        )
        PersonRow(
            "Office Community",
            "2 groups",
            onClick = { onNavigate(Routes.CommunityId.replace("{id}", "2")) },
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Find channels", onClick = { onNavigate(Routes.Channels) })
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
