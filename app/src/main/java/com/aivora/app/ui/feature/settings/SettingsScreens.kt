package com.aivora.app.ui.feature.settings

import com.aivora.app.ui.icons.AppIcons

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraScrollColumn
import com.aivora.app.ui.components.EmptyState
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.RadioGroup
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.icons.AivoraIconChrome
import com.aivora.app.ui.icons.AivoraIconSlot
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
import kotlinx.coroutines.launch

@Composable
private fun SettingsMutedCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = AivoraDimens.CardHorizontal)
            .fillMaxWidth()
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .background(AivoraColors.SurfaceMuted),
        content = content,
    )
}

@Composable
private fun SettingsToggleBlock(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    footnote: String,
) {
    SettingsMutedCard {
        SettingsRow(
            SettingsItem(title, toggle = checked, onToggle = onCheckedChange),
        )
    }
    SettingsFootnote(footnote)
}

@Composable
private fun SettingsGreenActionRow(
    title: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AivoraIconSlot(
            chrome = AivoraIconChrome.List,
            painterRes = iconRes,
            imageVector = icon,
            tint = AivoraColors.Primary,
        )
        Spacer(Modifier.size(14.dp))
        Text(
            title,
            color = AivoraColors.Primary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(AivoraIcons.ChevronRight),
            contentDescription = null,
            tint = AivoraColors.IconSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = AivoraColors.Ink,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsFootnote(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = AivoraColors.TextSecondary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
    )
}

@Composable
private fun SettingsHeroIntro(
    title: String,
    body: String,
    icon: ImageVector? = null,
    @DrawableRes iconRes: Int? = null,
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        AivoraIconSlot(
            chrome = AivoraIconChrome.Hero,
            painterRes = iconRes,
            imageVector = icon,
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = AivoraColors.TextSecondary)
    }
}


@Composable
fun SettingsProfileScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val context = androidx.compose.ui.platform.LocalContext.current
    val notif by com.aivora.app.data.local.NotificationPrefs.flow.collectAsState()
    val sections = listOf(
        null to listOf(
            SettingsItem("Edit profile", "Name, about, avatar, username", AppIcons.Person, onClick = { onNavigate(Routes.YourDetails) }),
            SettingsItem("Account", "Security, email, passkeys, number", AppIcons.AccountCircle, onClick = { onNavigate(Routes.SettingsAccount) }),
            SettingsItem("Privacy", "Last seen, profile photo, calls, locks", iconRes = AivoraIcons.Lock, onClick = { onNavigate(Routes.SettingsPrivacy) }),
            SettingsItem("Chats", "Theme, backup, history, transfer", iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChats) }),
            SettingsItem("Lists", "Filter chats into custom lists", AppIcons.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
            SettingsItem("New list", "Create a Work, Family, or custom filter", AppIcons.Menu, onClick = { onNavigate(Routes.SettingsListsNew) }),
            SettingsItem(
                "Notifications",
                "Message and call alerts",
                iconRes = AivoraIcons.Notifications,
                toggle = notif.enabled,
                onToggle = { com.aivora.app.data.local.NotificationPrefs.setEnabled(context, it) },
                onClick = { onNavigate(Routes.SettingsNotifications) },
            ),
            SettingsItem("Storage and data", "Network usage, download, clear", iconRes = AivoraIcons.Storage, onClick = { onNavigate(Routes.SettingsStorage) }),
            SettingsItem("Linked devices", "Use Aivora on other devices", AppIcons.Devices, onClick = { onNavigate(Routes.SettingsDevices) }),
            SettingsItem("Starred messages", null, iconRes = AivoraIcons.Star, onClick = { onNavigate(Routes.SettingsStarred) }),
            SettingsItem("Communities", null, iconRes = AivoraIcons.Group, onClick = { onNavigate(Routes.SettingsCommunities) }),
            SettingsItem("QR code", "My QR code", iconRes = AivoraIcons.Qr, onClick = { onNavigate(Routes.SettingsQr) }),
            SettingsItem("Permissions", "Camera, mic, contacts, location", AppIcons.Security, onClick = { onNavigate(Routes.SettingsPermissions) }),
            SettingsItem("Two-step verification", null, iconRes = AivoraIcons.Key, onClick = { onNavigate(Routes.SettingsTwoStep) }),
            SettingsItem("Blocked contacts", null, AppIcons.Block, onClick = { onNavigate(Routes.SettingsBlocked) }),
            SettingsItem("Add account", "Add a second account", AppIcons.PersonAdd, onClick = { onNavigate(Routes.SettingsAccountAdd) }),
            SettingsItem("Delete account", null, AppIcons.Delete, danger = true, onClick = { onNavigate(Routes.SettingsDeleteAccount) }),
        ),
    )

    AivoraScaffold(title = "Settings", onBack = onBack) {
        SettingsList(sections)
    }
}

@Composable
fun SettingsAccountScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var twoStepTrailing by remember { mutableStateOf("…") }
    LaunchedEffect(Unit) {
        runCatching { api.getMeTwoStep() }
            .onSuccess { twoStepTrailing = if (it.enabled) "On" else "Off" }
            .onFailure { twoStepTrailing = "Off" }
    }
    val sections = listOf(
        "Security" to listOf(
            SettingsItem("Security notifications", onClick = { onNavigate(Routes.SettingsAccountSecurityNotifications) }),
            SettingsItem("Passkeys", onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email address", trailing = "Not added", onClick = { onNavigate(Routes.SettingsAccountEmail) }),
            SettingsItem("Two-step verification", trailing = twoStepTrailing, onClick = { onNavigate(Routes.SettingsTwoStep) }),
        ),
        "Account" to listOf(
            SettingsItem("Change number", onClick = { onNavigate(Routes.SettingsAccountChangeNumber) }),
            SettingsItem("Request account info", onClick = { onNavigate(Routes.SettingsAccountRequestInfo) }),
            SettingsItem("Add account", onClick = { onNavigate(Routes.SettingsAccountAdd) }),
            SettingsItem("Delete account", danger = true, onClick = { onNavigate(Routes.SettingsDeleteAccount) }),
        ),
    )
    AivoraScaffold(title = "Account", onBack = onBack) {
        SettingsList(sections)
    }
}

@Composable
fun SettingsChatsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val composer by com.aivora.app.data.local.ChatComposerPrefs.flow.collectAsState()
    val themeMode by com.aivora.app.data.local.AppearancePrefs.themeFlow.collectAsState()
    val defaultWp by com.aivora.app.data.local.AppearancePrefs.wallpaperFlow.collectAsState()
    val fontScale by com.aivora.app.data.local.FontScaleStore.scaleFlow.collectAsState()
    val sections = listOf(
        "Display" to listOf(
            SettingsItem(
                "Theme",
                subtitle = com.aivora.app.data.local.AppearancePrefs.themeLabel(themeMode),
                onClick = { onNavigate(Routes.SettingsAppearance) },
            ),
            SettingsItem(
                "Default chat theme",
                subtitle = com.aivora.app.data.local.AppearancePrefs.wallpaperLabel(defaultWp),
                onClick = { onNavigate(Routes.SettingsAppearance) },
            ),
            SettingsItem(
                "Chat font size",
                trailing = com.aivora.app.data.local.FontScaleStore.label(fontScale),
                onClick = { onNavigate(Routes.SettingsChatsFontSize) },
            ),
        ),
        "Chat settings" to listOf(
            SettingsItem(
                "Enter is send",
                subtitle = "Enter key sends your message",
                toggle = composer.enterIsSend,
                onToggle = { com.aivora.app.data.local.ChatComposerPrefs.setEnterIsSend(context, it) },
            ),
            SettingsItem(
                "Media visibility",
                subtitle = "Show photos and videos you send or open in your phone’s gallery",
                toggle = composer.mediaVisibility,
                onToggle = { com.aivora.app.data.local.ChatComposerPrefs.setMediaVisibility(context, it) },
            ),
            SettingsItem("Keep chats archived", subtitle = "Archived chats stay archived when new messages arrive", onClick = { onNavigate(Routes.SettingsChatsArchived) }),
        ),
        "Chat history" to listOf(
            SettingsItem("Chat backup", onClick = { onNavigate(Routes.SettingsChatsBackup) }),
            SettingsItem("Transfer chats", onClick = { onNavigate(Routes.SettingsChatsTransfer) }),
            SettingsItem("Chat history", onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Export chat", onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Chat transcripts", onClick = { onNavigate(Routes.SettingsChatsTranscripts) }),
            SettingsItem("Clear all chats", danger = true, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Delete all chats", danger = true, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
        ),
    )
    AivoraScaffold(title = "Chats", onBack = onBack) {
        SettingsList(sections)
    }
}


private fun privacyWhoLabel(key: String): String = when (key) {
    "everyone" -> "Everyone"
    "contacts" -> "My contacts"
    "nobody" -> "Nobody"
    "contacts_except" -> "My contacts except…"
    "only_share" -> "Only share with…"
    else -> key
}

@Composable
fun SettingsPrivacyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var privacy by remember {
        mutableStateOf(cached ?: com.aivora.app.data.model.PrivacySettingsDto())
    }
    var disappearDefault by remember { mutableIntStateOf(0) }

    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }
            .onSuccess {
                com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
                privacy = it
            }
        runCatching { api.getMePreferences() }
            .onSuccess { disappearDefault = it.defaultDisappearingSeconds }
    }

    val sections = listOf(
        "Who can…" to listOf(
            SettingsItem(
                "Last seen and online",
                trailing = privacyWhoLabel(privacy.lastSeen),
                onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) },
            ),
            SettingsItem(
                "Profile photo",
                trailing = privacyWhoLabel(privacy.profilePhoto),
                onClick = { onNavigate(Routes.SettingsPrivacyProfilePhoto) },
            ),
            SettingsItem(
                "About",
                trailing = privacyWhoLabel(privacy.about),
                onClick = { onNavigate(Routes.SettingsPrivacyAbout) },
            ),
            SettingsItem(
                "Links",
                trailing = privacyWhoLabel(privacy.links),
                onClick = { onNavigate(Routes.SettingsPrivacyLinks) },
            ),
            SettingsItem(
                "Status",
                trailing = privacyWhoLabel(privacy.status),
                onClick = { onNavigate(Routes.SettingsPrivacyStatus) },
            ),
            SettingsItem(
                "Read receipts",
                subtitle = "If turned off, you won’t send or receive read receipts",
                toggle = privacy.readReceipts,
                onToggle = { next ->
                    val prev = privacy
                    privacy = privacy.copy(readReceipts = next)
                    if (!useRemote) return@SettingsItem
                    scope.launch {
                        runCatching {
                            api.putMePrivacy(
                                com.aivora.app.data.model.PrivacySettingsBody(readReceipts = next),
                            )
                        }.onSuccess {
                            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
                            privacy = it
                        }.onFailure {
                            privacy = prev
                            android.widget.Toast.makeText(
                                context,
                                "Could not update",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
            ),
        ),
        null to listOf(
            SettingsItem(
                "Disappearing messages",
                trailing = defaultTimerLabel(disappearDefault),
                onClick = { onNavigate(Routes.SettingsPrivacyDefaultTimer) },
            ),
            SettingsItem(
                "Groups",
                trailing = privacyWhoLabel(privacy.groups),
                onClick = { onNavigate(Routes.SettingsPrivacyGroups) },
            ),
            SettingsItem("Live location", trailing = "None", onClick = { onNavigate(Routes.SettingsPrivacyLiveLocation) }),
            SettingsItem("Calls", trailing = "Silence unknown callers", onClick = { onNavigate(Routes.SettingsPrivacyCalls) }),
            SettingsItem("App lock", onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
            SettingsItem("Chat lock", onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
            SettingsItem("Advanced", onClick = { onNavigate(Routes.SettingsPrivacyAdvanced) }),
            SettingsItem("Privacy checkup", onClick = { onNavigate(Routes.SettingsPrivacyCheckup) }),
            SettingsItem("Blocked contacts", trailing = "None", onClick = { onNavigate(Routes.SettingsBlocked) }),
        ),
    )

    AivoraScaffold(title = "Privacy", onBack = onBack) {
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyLastSeenScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val lastSeenKeys = listOf("everyone", "contacts", "nobody")
    val onlineKeys = listOf("everyone", "same_as_last_seen", "nobody")
    val cachedPrivacy = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var lastSeen by remember {
        mutableIntStateOf(
            cachedPrivacy?.let { lastSeenKeys.indexOf(it.lastSeen).takeIf { i -> i >= 0 } } ?: 1,
        )
    }
    var online by remember {
        mutableIntStateOf(
            cachedPrivacy?.let { onlineKeys.indexOf(it.online).takeIf { i -> i >= 0 } } ?: 1,
        )
    }
    androidx.compose.runtime.LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }
            .onSuccess {
                com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
                lastSeenKeys.indexOf(it.lastSeen).takeIf { i -> i >= 0 }?.let { i -> lastSeen = i }
                onlineKeys.indexOf(it.online).takeIf { i -> i >= 0 }?.let { i -> online = i }
            }
            .onFailure {
                android.widget.Toast.makeText(context, "Could not load privacy settings", android.widget.Toast.LENGTH_SHORT).show()
            }
    }
    fun savePrivacy(body: com.aivora.app.data.model.PrivacySettingsBody, revert: () -> Unit) {
        if (!useRemote) return
        scope.launch {
            runCatching { api.putMePrivacy(body) }
                .onSuccess {
                    com.aivora.app.data.local.InMemoryPrivacyStore.dto?.let { prev ->
                        com.aivora.app.data.local.InMemoryPrivacyStore.replace(
                            prev.copy(
                                lastSeen = body.lastSeen ?: prev.lastSeen,
                                online = body.online ?: prev.online,
                                status = body.status ?: prev.status,
                                statusIds = body.statusIds ?: prev.statusIds,
                            ),
                        )
                    }
                }
                .onFailure {
                    revert()
                    android.widget.Toast.makeText(context, "Could not update privacy", android.widget.Toast.LENGTH_SHORT).show()
                }
        }
    }
    AivoraScaffold(title = "Last seen and online", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my last seen")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
                "Nobody" to null,
            ),
            selected = lastSeen,
            onSelect = { i ->
                val prev = lastSeen
                lastSeen = i
                savePrivacy(com.aivora.app.data.model.PrivacySettingsBody(lastSeen = lastSeenKeys[i])) { lastSeen = prev }
            },
        )
        Spacer(Modifier.height(16.dp))
        SettingsSectionLabel("Who can see when I’m online")
        RadioGroup(
            options = listOf(
                "Everyone" to null,
                "Same as last seen" to null,
                "Nobody" to null,
            ),
            selected = online,
            onSelect = { i ->
                val prev = online
                online = i
                savePrivacy(com.aivora.app.data.model.PrivacySettingsBody(online = onlineKeys[i])) { online = prev }
            },
        )
        SettingsFootnote(
            "If you don’t share your last seen or online status, you won’t be able to see other people’s last seen or online status.",
        )
    }
}


@Composable
fun SettingsPrivacyProfilePhotoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val keys = listOf("everyone", "contacts", "nobody")
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var selected by remember {
        mutableIntStateOf(cached?.let { keys.indexOf(it.profilePhoto).takeIf { i -> i >= 0 } } ?: 0)
    }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess {
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
            keys.indexOf(it.profilePhoto).takeIf { i -> i >= 0 }?.let { selected = it }
        }
    }
    AivoraScaffold(title = "Profile photo", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my profile photo")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
                "Nobody" to null,
            ),
            selected = selected,
            onSelect = { i ->
                val prev = selected
                selected = i
                if (!useRemote) return@RadioGroup
                scope.launch {
                    runCatching {
                        api.putMePrivacy(
                            com.aivora.app.data.model.PrivacySettingsBody(profilePhoto = keys[i]),
                        )
                    }.onSuccess { dto ->
                        com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
                    }.onFailure {
                        selected = prev
                        android.widget.Toast.makeText(context, "Could not update", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        SettingsFootnote("Controls who can see your profile photo.")
    }
}


@Composable
fun SettingsPrivacyLinksScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val keys = listOf("everyone", "contacts", "nobody")
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var selected by remember {
        mutableIntStateOf(cached?.let { keys.indexOf(it.links).takeIf { i -> i >= 0 } } ?: 1)
    }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess {
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
            keys.indexOf(it.links).takeIf { i -> i >= 0 }?.let { selected = it }
        }
    }
    AivoraScaffold(title = "Links", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can add me via QR or profile link")
        RadioGroup(
            options = listOf(
                "Everyone" to null,
                "My contacts" to null,
                "Nobody" to null,
            ),
            selected = selected,
            onSelect = { i ->
                val prev = selected
                selected = i
                if (!useRemote) return@RadioGroup
                scope.launch {
                    runCatching {
                        api.putMePrivacy(
                            com.aivora.app.data.model.PrivacySettingsBody(links = keys[i]),
                        )
                    }.onSuccess { dto ->
                        com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
                    }.onFailure {
                        selected = prev
                        android.widget.Toast.makeText(context, "Could not update", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        SettingsFootnote(
            "Contacts means people you already have a direct chat with. Nobody blocks new chats from your QR code.",
        )
    }
}


@Composable
fun SettingsPrivacyAboutScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val keys = listOf("everyone", "contacts", "nobody")
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var selected by remember {
        mutableIntStateOf(cached?.let { keys.indexOf(it.about).takeIf { i -> i >= 0 } } ?: 0)
    }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess {
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
            keys.indexOf(it.about).takeIf { i -> i >= 0 }?.let { selected = it }
        }
    }
    AivoraScaffold(title = "About", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my about info")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
                "Nobody" to null,
            ),
            selected = selected,
            onSelect = { i ->
                val prev = selected
                selected = i
                if (!useRemote) return@RadioGroup
                scope.launch {
                    runCatching {
                        api.putMePrivacy(
                            com.aivora.app.data.model.PrivacySettingsBody(about = keys[i]),
                        )
                    }.onSuccess { dto ->
                        com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
                    }.onFailure {
                        selected = prev
                        android.widget.Toast.makeText(context, "Could not update", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        SettingsFootnote("About is a short bio under your profile photo.")
    }
}


@Composable
fun SettingsPrivacyStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val keys = listOf("everyone", "contacts", "contacts_except", "only_share")
    val cachedPrivacy = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var selected by remember {
        mutableIntStateOf(
            cachedPrivacy?.let { keys.indexOf(it.status).takeIf { i -> i >= 0 } } ?: 1,
        )
    }
    var statusIds by remember {
        mutableStateOf(cachedPrivacy?.statusIds?.toSet().orEmpty())
    }
    var pickMode by remember { mutableStateOf(false) }
    var people by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryPeopleStore.directory.value)
    }

    fun save(modeIndex: Int, ids: Set<String> = statusIds) {
        if (!useRemote) return
        scope.launch {
            runCatching {
                api.putMePrivacy(
                    com.aivora.app.data.model.PrivacySettingsBody(
                        status = keys[modeIndex],
                        statusIds = ids.toList(),
                    ),
                )
            }.onSuccess {
                com.aivora.app.data.local.InMemoryPrivacyStore.dto?.let { prev ->
                    com.aivora.app.data.local.InMemoryPrivacyStore.replace(
                        prev.copy(status = keys[modeIndex], statusIds = ids.toList()),
                    )
                }
            }.onFailure {
                android.widget.Toast.makeText(context, "Could not save", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess { dto ->
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
            selected = keys.indexOf(dto.status).takeIf { it >= 0 } ?: 1
            statusIds = dto.statusIds.toSet()
        }
        runCatching { api.getPeopleSearch(q = null, limit = 100).items }.onSuccess {
            com.aivora.app.data.local.InMemoryPeopleStore.put("", it)
            people = it
        }
    }

    AivoraScaffold(title = "Status", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my status updates")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
                "My contacts except…" to null,
                "Only share with…" to null,
            ),
            selected = selected,
            onSelect = { i ->
                selected = i
                if (i == 2 || i == 3) {
                    pickMode = true
                } else {
                    pickMode = false
                    save(i)
                }
            },
            navigableIndices = setOf(2, 3),
        )
        if (pickMode && (selected == 2 || selected == 3)) {
            Spacer(Modifier.height(12.dp))
            SettingsSectionLabel(if (selected == 2) "Except" else "Only share with")
            people.forEach { person ->
                val id = person.id ?: return@forEach
                val name = person.displayName?.takeIf { it.isNotBlank() } ?: person.phone ?: id
                val checked = id in statusIds
                SettingsRow(
                    SettingsItem(
                        name,
                        subtitle = person.phone,
                        toggle = checked,
                        onToggle = { on ->
                            statusIds = if (on) statusIds + id else statusIds - id
                        },
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Save",
                onClick = {
                    save(selected, statusIds)
                    pickMode = false
                },
            )
        }
        SettingsFootnote("Changes to your privacy settings won’t affect status updates you’ve already sent.")
    }
}


@Composable
fun SettingsPrivacyGroupsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val keys = listOf("everyone", "contacts", "contacts_except")
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var selected by remember {
        mutableIntStateOf(cached?.let { keys.indexOf(it.groups).takeIf { i -> i >= 0 } } ?: 0)
    }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess {
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
            keys.indexOf(it.groups).takeIf { i -> i >= 0 }?.let { selected = it }
        }
    }
    AivoraScaffold(title = "Groups", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can add me to groups")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
                "My contacts except…" to null,
            ),
            selected = selected,
            onSelect = { i ->
                val prev = selected
                selected = i
                if (!useRemote) return@RadioGroup
                scope.launch {
                    runCatching {
                        api.putMePrivacy(
                            com.aivora.app.data.model.PrivacySettingsBody(groups = keys[i]),
                        )
                    }.onSuccess { dto ->
                        com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
                    }.onFailure {
                        selected = prev
                        android.widget.Toast.makeText(context, "Could not update", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
        )
        SettingsFootnote(
            "Admins who can’t add you to a group will have the option of inviting you privately instead.",
        )
    }
}


@Composable
fun SettingsPrivacyCallsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var silence by remember { mutableStateOf(cached?.silenceUnknownCallers ?: false) }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }.onSuccess {
            com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
            silence = it.silenceUnknownCallers
        }
    }
    AivoraScaffold(title = "Calls", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            SettingsRow(
                SettingsItem(
                    "Silence unknown callers",
                    toggle = silence,
                    onToggle = { on ->
                        val prev = silence
                        silence = on
                        if (!useRemote) return@SettingsItem
                        scope.launch {
                            runCatching {
                                api.putMePrivacy(
                                    com.aivora.app.data.model.PrivacySettingsBody(
                                        silenceUnknownCallers = on,
                                    ),
                                )
                            }.onSuccess { dto ->
                                com.aivora.app.data.local.InMemoryPrivacyStore.replace(dto)
                            }.onFailure {
                                silence = prev
                                android.widget.Toast.makeText(context, "Could not update", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                ),
            )
        }
        SettingsFootnote(
            "Calls from people you don’t already have a chat with won’t ring or show the full-screen answer UI. " +
                "They can still show up as missed in Calls history. Existing chats and group calls still ring.\n\n" +
                "This doesn’t block messaging — use Block unknown under Advanced for that.",
        )
    }
}


private val DefaultTimerSeconds = listOf(0, 86_400, 604_800, 7_776_000)

private fun defaultTimerLabel(seconds: Int): String = when (seconds) {
    86_400 -> "24 hours"
    604_800 -> "7 days"
    7_776_000 -> "90 days"
    else -> "Off"
}

@Composable
fun SettingsPrivacyDefaultTimerScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    var selected by remember { mutableIntStateOf(0) }
    LaunchedEffect(useRemote) {
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePreferences() }
            .onSuccess { prefs ->
                val idx = DefaultTimerSeconds.indexOf(prefs.defaultDisappearingSeconds)
                selected = if (idx >= 0) idx else 0
            }
    }
    AivoraScaffold(title = "Default message timer", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Disappearing messages")
        Text(
            "Start new individual chats with messages that disappear after your chosen duration.",
            style = MaterialTheme.typography.bodySmall,
            color = AivoraColors.TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(8.dp))
        RadioGroup(
            options = listOf(
            "Off" to null,
            "24 hours" to null,
            "7 days" to null,
                "90 days" to null,
            ),
            selected = selected,
            onSelect = { i ->
                val prev = selected
                selected = i
                if (!useRemote) return@RadioGroup
                scope.launch {
                    runCatching {
                        api.patchMePreferences(
                            com.aivora.app.data.model.UserPreferencesPatchBody(
                                defaultDisappearingSeconds = DefaultTimerSeconds[i],
                            ),
                        )
                    }.onFailure {
                        selected = prev
                        android.widget.Toast.makeText(
                            context,
                            "Could not save",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
        )
        SettingsFootnote(
            "This won’t delete existing message history or change existing chats. You can change the timer in each chat.",
        )
    }
}


@Composable
fun SettingsQrScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPhoto by remember {
        mutableStateOf(com.aivora.app.data.local.QrPrefs.showNamePhoto(context))
    }
    val sections = listOf(
        null to listOf(
            SettingsItem(
                "My QR code",
                "Show my name and photo on my QR page",
                iconRes = AivoraIcons.Qr,
                onClick = { onNavigate(Routes.SettingsQrMy) },
            ),
            SettingsItem(
                "Scan QR code",
                "Camera or photo",
                iconRes = AivoraIcons.Camera,
                onClick = { onNavigate(Routes.SettingsQrScan) },
            ),
            SettingsItem(
                "Show name and photo on QR page",
                null,
                AppIcons.Visibility,
                toggle = showPhoto,
                onToggle = {
                    showPhoto = it
                    com.aivora.app.data.local.QrPrefs.setShowNamePhoto(context, it)
                },
            ),
        ),
    )

    AivoraScaffold(title = "QR code", onBack = onBack) {
        InfoBanner("Anyone who opens your QR link can start a chat with you in Aivora.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsStarredScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    var starred by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryStarredStore.items.value)
    }
    var loading by remember {
        mutableStateOf(
            useRemote && com.aivora.app.data.local.InMemoryStarredStore.items.value.isEmpty(),
        )
    }
    val myUserId = remember {
        com.aivora.app.data.local.SessionStore.userId(context).orEmpty()
    }

    androidx.compose.runtime.LaunchedEffect(useRemote) {
        if (!useRemote) {
            loading = false
            return@LaunchedEffect
        }
        val warm = com.aivora.app.data.local.InMemoryStarredStore.items.value
        if (warm.isNotEmpty()) {
            starred = warm
            loading = false
        } else {
            loading = true
        }
        runCatching { api.getMeStarred() }
            .onSuccess { list ->
                val uid = myUserId.ifBlank {
                    runCatching { api.getMe().id }.getOrNull().orEmpty()
                }
                val mapped = list.items.map { it.toLocalMessage(uid) }
                com.aivora.app.data.local.InMemoryStarredStore.replace(mapped)
                starred = mapped
            }
            .onFailure {
                if (starred.isEmpty()) starred = emptyList()
            }
        loading = false
    }

    AivoraScaffold(title = "Starred messages", onBack = onBack) {
        when {
            loading -> {
                Text(
                    "Loading…",
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(24.dp),
                )
            }
            starred.isEmpty() -> {
                EmptyState(
                    title = "No starred messages",
                    body = "Press and hold a message in chat, then choose Star.",
                    icon = AppIcons.StarBorder,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )
            }
            else -> {
                AivoraNoOverscroll {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(starred, key = { it.id }, contentType = { "star" }) { msg ->
                        val chatId = msg.conversationId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!chatId.isNullOrBlank()) {
                                        onNavigate(Routes.ChatId.replace("{id}", chatId))
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(AivoraIcons.Star),
                                contentDescription = null,
                                tint = AivoraColors.Primary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    msg.body.ifBlank { "Starred message" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                )
                                Text(
                                    msg.time,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AivoraColors.TextSecondary,
                                )
                            }
                        }
                        HorizontalDivider(color = AivoraColors.Divider)
                    }
                }
                }
            }
        }
    }
}


private fun mediaPrefLabel(key: String): String = when (key) {
    "photos" -> "Photos"
    "photos_audio" -> "Photos & audio"
    "all" -> "All media"
    "none" -> "No media"
    else -> key
}

private fun qualityPrefLabel(key: String): String = when (key) {
    "auto" -> "Auto (recommended)"
    "high" -> "High quality"
    "data_saver" -> "Data saver"
    else -> key
}

@Composable
fun SettingsStorageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var usedLabel by remember { mutableStateOf("…") }
    var lessData by remember { mutableStateOf(false) }
    var mobile by remember { mutableStateOf("photos") }
    var wifi by remember { mutableStateOf("all") }
    var roaming by remember { mutableStateOf("none") }
    var quality by remember { mutableStateOf("auto") }
    var pick by remember { mutableStateOf<String?>(null) }

    fun applyPrefs(dto: com.aivora.app.data.model.AutoDownloadDto) {
        mobile = dto.mobile
        wifi = dto.wifi
        roaming = dto.roaming
        lessData = dto.lessDataCalls
        quality = dto.photoUploadQuality
        com.aivora.app.data.local.AutoDownloadPrefs.apply(context, dto)
    }

    fun patch(body: com.aivora.app.data.model.AutoDownloadPatchBody) {
        scope.launch {
            runCatching { api.patchMeAutoDownload(body) }
                .onSuccess { applyPrefs(it) }
                .onFailure {
                    android.widget.Toast.makeText(
                        context,
                        it.message ?: "Could not save",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
        }
    }

    LaunchedEffect(Unit) {
        runCatching { api.getMeStorage() }
            .onSuccess { usedLabel = formatBytes(it.usedBytes) }
            .onFailure { usedLabel = "—" }
        runCatching { api.getMeAutoDownload() }
            .onSuccess { applyPrefs(it) }
    }

    if (pick != null) {
        val options = when (pick) {
            "quality" -> listOf("auto", "high", "data_saver")
            else -> listOf("photos", "photos_audio", "all", "none")
        }
        val title = when (pick) {
            "mobile" -> "Mobile data"
            "wifi" -> "Wi-Fi"
            "roaming" -> "Roaming"
            else -> "Photo upload quality"
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pick = null },
            title = { Text(title) },
            text = {
                Column {
                    options.forEach { key ->
                        val label = if (pick == "quality") qualityPrefLabel(key) else mediaPrefLabel(key)
                        Text(
                            label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (pick) {
                                        "mobile" -> patch(
                                            com.aivora.app.data.model.AutoDownloadPatchBody(mobile = key),
                                        )
                                        "wifi" -> patch(
                                            com.aivora.app.data.model.AutoDownloadPatchBody(wifi = key),
                                        )
                                        "roaming" -> patch(
                                            com.aivora.app.data.model.AutoDownloadPatchBody(roaming = key),
                                        )
                                        "quality" -> patch(
                                            com.aivora.app.data.model.AutoDownloadPatchBody(
                                                photoUploadQuality = key,
                                            ),
                                        )
                                    }
                                    pick = null
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { pick = null }) { Text("Cancel") }
            },
        )
    }

    val proxyCfg by com.aivora.app.data.local.ProxyPrefs.flow.collectAsState()
    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Manage storage",
                subtitle = usedLabel,
                onClick = { onNavigate(Routes.SettingsManageStorage) },
            ),
            SettingsItem(
                "Clear cache",
                subtitle = "Photos cache, exports, and temp files. Chat backups stay.",
                onClick = {
                    val freed = com.aivora.app.data.CacheCleaner.clear(context)
                    android.widget.Toast.makeText(
                        context,
                        "Cleared ${formatBytes(freed)}",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
            ),
        ),
        "Network" to listOf(
            SettingsItem(
                "Network usage",
                subtitle = "Media on device · $usedLabel",
                onClick = { onNavigate(Routes.SettingsManageStorage) },
            ),
            SettingsItem(
                "Use less data for calls",
                toggle = lessData,
                onToggle = {
                    lessData = it
                    patch(com.aivora.app.data.model.AutoDownloadPatchBody(lessDataCalls = it))
                },
            ),
            SettingsItem(
                "Proxy",
                trailing = com.aivora.app.data.local.ProxyPrefs.trailingLabel(proxyCfg),
                onClick = { onNavigate(Routes.SettingsProxy) },
            ),
        ),
        "Media auto-download" to listOf(
            SettingsItem(
                "When using mobile data",
                trailing = mediaPrefLabel(mobile),
                onClick = { pick = "mobile" },
            ),
            SettingsItem(
                "When connected on Wi-Fi",
                trailing = mediaPrefLabel(wifi),
                onClick = { pick = "wifi" },
            ),
            SettingsItem(
                "When roaming",
                trailing = mediaPrefLabel(roaming),
                onClick = { pick = "roaming" },
            ),
        ),
        "Media upload quality" to listOf(
            SettingsItem(
                "Photo upload quality",
                trailing = qualityPrefLabel(quality),
                onClick = { pick = "quality" },
            ),
        ),
    )

    AivoraScaffold(title = "Storage and data", onBack = onBack) {
        SettingsList(sections)
    }
}

@Composable
fun SettingsProxyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val saved by com.aivora.app.data.local.ProxyPrefs.flow.collectAsState()
    var enabled by remember(saved) { mutableStateOf(saved.enabled) }
    var host by remember(saved) { mutableStateOf(saved.host) }
    var portText by remember(saved) { mutableStateOf(saved.port.toString()) }
    var type by remember(saved) { mutableStateOf(saved.type) }
    var testing by remember { mutableStateOf(false) }
    var pickType by remember { mutableStateOf(false) }

    fun persist(showToast: Boolean = true) {
        val port = portText.toIntOrNull()?.coerceIn(1, 65535) ?: 1080
        portText = port.toString()
        com.aivora.app.data.local.ProxyPrefs.save(
            context,
            com.aivora.app.data.local.ProxyPrefs.Config(
                enabled = enabled,
                host = host.trim(),
                port = port,
                type = type,
            ),
        )
        if (showToast) {
            android.widget.Toast.makeText(context, "Proxy saved", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    if (pickType) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickType = false },
            title = { Text("Proxy type") },
            text = {
                Column {
                    listOf(
                        com.aivora.app.data.local.ProxyPrefs.TYPE_SOCKS to "SOCKS5",
                        com.aivora.app.data.local.ProxyPrefs.TYPE_HTTP to "HTTP",
                    ).forEach { (key, label) ->
                        Text(
                            label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    type = key
                                    pickType = false
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (key == type) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { pickType = false }) { Text("Cancel") }
            },
        )
    }

    AivoraScaffold(title = "Proxy", onBack = onBack) {
        InfoBanner(
            "Routes API and realtime traffic through your SOCKS5 or HTTP proxy. " +
                "Leave host empty and disable to use the direct connection.",
        )
        SettingsToggleBlock(
            title = "Use proxy",
            checked = enabled,
            onCheckedChange = { enabled = it },
            footnote = "Applies to new HTTP and WebSocket connections immediately after Save.",
        )
        SettingsList(
            listOf(
        null to listOf(
                    SettingsItem(
                        "Proxy type",
                        trailing = if (type == com.aivora.app.data.local.ProxyPrefs.TYPE_HTTP) "HTTP" else "SOCKS5",
                        onClick = { pickType = true },
                    ),
                ),
            ),
        )
        OutlinedTextField(
            value = host,
            onValueChange = { host = it.take(253) },
            label = { Text("Hostname") },
            placeholder = { Text("127.0.0.1 or proxy.example.com") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AivoraColors.Primary,
                cursorColor = AivoraColors.Primary,
            ),
        )
        OutlinedTextField(
            value = portText,
            onValueChange = { raw ->
                portText = raw.filter { it.isDigit() }.take(5)
            },
            label = { Text("Port") },
            placeholder = { Text("1080") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AivoraColors.Primary,
                cursorColor = AivoraColors.Primary,
            ),
        )
        Spacer(Modifier.height(12.dp))
        SettingsGreenActionRow(
            title = "Save",
            onClick = { persist() },
        )
        SettingsGreenActionRow(
            title = if (testing) "Testing…" else "Test connection",
            onClick = {
                if (testing) return@SettingsGreenActionRow
                persist(showToast = false)
                if (enabled && host.isBlank()) {
                    android.widget.Toast.makeText(
                        context,
                        "Enter a hostname first",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                    return@SettingsGreenActionRow
                }
                testing = true
                scope.launch {
                    val ok = runCatching {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val url = com.aivora.app.BuildConfig.API_BASE_URL.trimEnd('/') + "/health"
                            val req = okhttp3.Request.Builder().url(url).get().build()
                            com.aivora.app.data.api.NetworkModule.okHttp().newCall(req).execute().use { resp ->
                                resp.isSuccessful
                            }
                        }
                    }.getOrDefault(false)
                    testing = false
                    android.widget.Toast.makeText(
                        context,
                        if (ok) "Proxy reachable" else "Could not reach server via proxy",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
            },
        )
        Spacer(Modifier.height(16.dp))
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1024) String.format("%.1f GB", mb / 1024.0)
    else if (mb >= 10) String.format("%.0f MB", mb)
    else String.format("%.1f MB", mb)
}


@Composable
fun SettingsManageStorageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var query by remember { mutableStateOf("") }
    var usedBytes by remember { mutableStateOf(0L) }
    var chats by remember { mutableStateOf<List<com.aivora.app.data.model.StorageChatDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        runCatching { api.getMeStorage() }
            .onSuccess {
                usedBytes = it.usedBytes
                chats = it.chats
            }
        loading = false
    }
    val usedLabel = formatBytes(usedBytes)
    AivoraScaffold(title = "Manage storage", onBack = onBack) {
        AivoraNoOverscroll {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(
                        if (loading) "…" else usedLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        "Media used in your chats (server estimate)",
                        style = MaterialTheme.typography.bodySmall,
                        color = AivoraColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    StorageLegendRow(AivoraColors.Primary, "Aivora media", usedLabel)
                }
            }
            item {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search chats",
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                ) {
                    Text(
                        "Chats",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "Largest first",
                        style = MaterialTheme.typography.bodySmall,
                        color = AivoraColors.TextSecondary,
                    )
                }
            }
            if (!loading && chats.isEmpty()) {
                item {
                    Text(
                        "No media sized yet — sizes appear after media messages include attachment size.",
                        color = AivoraColors.TextSecondary,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            items(
                items = chats.filter {
                    (it.title ?: it.chatId().orEmpty()).contains(query, ignoreCase = true)
                },
                key = { it.chatId() ?: it.title ?: it.hashCode().toString() },
            ) { row ->
                val name = row.title?.takeIf { it.isNotBlank() } ?: "Chat"
                PersonRow(
                    name = name,
                    onClick = {
                        row.chatId()?.let { onNavigate(Routes.ChatId.replace("{id}", it)) }
                    },
                    trailing = {
                        Text(formatBytes(row.bytes), color = AivoraColors.TextSecondary, fontSize = 13.sp)
                    },
                )
            }
        }
        }
    }
}

@Composable
private fun StorageLegendRow(color: Color, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = AivoraColors.TextSecondary)
    }
}


@Composable
fun SettingsTwoStepScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var enabled by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var pinPrompt by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { api.getMeTwoStep() }
            .onSuccess {
                enabled = it.enabled
                email = it.email
            }
            .onFailure { error = it.message }
        loading = false
    }

    if (pinPrompt) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pinPrompt = false; pin = "" },
            title = { Text("Enter PIN") },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (pin.length != 6 || busy) return@TextButton
                        busy = true
                        scope.launch {
                            runCatching {
                                api.postMeTwoStepOff(
                                    com.aivora.app.data.model.TwoStepPinBody(pin = pin),
                                )
                            }.onSuccess {
                                enabled = false
                                pinPrompt = false
                                pin = ""
                            }.onFailure { error = it.message ?: "Could not disable" }
                            busy = false
                        }
                    },
                ) { Text(if (busy) "…" else "Disable") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pinPrompt = false; pin = "" }) {
                    Text("Cancel")
                }
            },
        )
    }

    AivoraScaffold(title = "Two-step verification", onBack = onBack) {
        SettingsHeroIntro(
            iconRes = AivoraIcons.Lock,
            title = when {
                loading -> "Loading…"
                enabled -> "Two-step verification is enabled"
                else -> "Protect your account"
            },
            body = if (enabled) {
                "Your six-digit account PIN adds a verification step when you register your phone number again."
            } else {
                "Create a 6-digit PIN that you’ll enter after SMS verification when signing in on a new device."
            },
        )
        if (error != null) {
            Text(
                error!!,
                color = AivoraColors.Danger,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            )
        }
        if (!enabled && !loading) {
            PrimaryButton(
                text = "Turn on",
                onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        } else if (enabled) {
            SettingsList(
                listOf(
        null to listOf(
                        SettingsItem(
                            "Recovery email",
                            subtitle = when {
                                email.isNullOrBlank() -> "Not added"
                                else -> email
                            },
                            icon = AppIcons.Email,
                            onClick = { onNavigate(Routes.SettingsAccountEmail) },
                        ),
                    ),
                    null to listOf(
                        SettingsItem(
                            "Change PIN",
                            iconRes = AivoraIcons.Edit,
                            onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) },
                        ),
                        SettingsItem(
                            "Disable",
                            iconRes = AivoraIcons.Lock,
                            danger = true,
                            onClick = { pinPrompt = true },
                        ),
                    ),
                ),
            )
        }
    }
}


@Composable
fun SettingsBlockedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    var blocked by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryBlockedStore.items.value)
    }
    var loading by remember {
        mutableStateOf(
            useRemote && com.aivora.app.data.local.InMemoryBlockedStore.items.value.isEmpty(),
        )
    }

    fun reload(forceLoading: Boolean = false) {
        if (!useRemote) {
            loading = false
            return
        }
        scope.launch {
            val warm = com.aivora.app.data.local.InMemoryBlockedStore.items.value
            if (warm.isNotEmpty()) {
                blocked = warm
                loading = forceLoading && warm.isEmpty()
            } else {
                loading = true
            }
            val fresh = runCatching { api.getBlocks().items }.getOrDefault(warm)
            com.aivora.app.data.local.InMemoryBlockedStore.replace(fresh)
            blocked = fresh
            loading = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(useRemote) { reload() }

    AivoraScaffold(title = "Blocked contacts", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        when {
            loading -> {
                Text(
                    "Loading…",
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(24.dp),
                )
            }
            blocked.isEmpty() -> {
                EmptyState(
                    title = "No blocked contacts",
                    body = "Block someone from a chat menu. They can’t message you until you unblock.",
                    icon = AppIcons.Block,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(
                        items = blocked.filter { !it.id.isNullOrBlank() },
                        key = { it.id!! },
                    ) { item ->
                        val name = item.user?.displayName
                            ?: item.user?.phoneDisplay
                            ?: item.user?.phone
                            ?: "Contact"
                        PersonRow(
                            name = name,
                            subtitle = "Blocked · Tap to unblock",
                            onClick = {
                                val id = item.id ?: return@PersonRow
                                scope.launch {
                                    runCatching { api.deleteBlocksId(id) }
                                        .onSuccess {
                                            com.aivora.app.data.local.InMemoryBlockedStore.removeId(id)
                                            blocked =
                                                com.aivora.app.data.local.InMemoryBlockedStore.items.value
                                        }
                                }
                            },
                        )
                    }
                }
            }
        }
        SettingsFootnote(
            "Blocked contacts can’t call or message you. Blocking doesn’t delete chat history.",
        )
    }
}


@Composable
fun SettingsDevicesScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var devices by remember { mutableStateOf<List<com.aivora.app.data.model.DeviceDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun reload() {
        scope.launch {
            loading = devices.isEmpty()
            runCatching { api.getMeDevices().items }
                .onSuccess {
                    devices = it
                    error = null
                }
                .onFailure { error = it.message ?: "Could not load devices" }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    AivoraScaffold(title = "Linked devices", onBack = onBack) {
        SettingsList(
            listOf(
        null to listOf(
                    SettingsItem(
                        "Link a device",
                        "Enter the code from the other device",
                        iconRes = AivoraIcons.Qr,
                        onClick = { onNavigate(Routes.SettingsDevicesLink) },
                    ),
                ),
            ),
        )
        when {
            loading -> Text(
                "Loading…",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            error != null && devices.isEmpty() -> Text(
                error!!,
                color = AivoraColors.Danger,
                modifier = Modifier.padding(24.dp),
            )
            devices.isEmpty() -> Text(
                "No devices yet. Sign in on another phone to see it here.",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            else -> {
                val items = devices.map { d ->
                    val name = d.deviceName?.takeIf { it.isNotBlank() }
                        ?: d.platform?.replaceFirstChar { it.uppercase() }
                        ?: "Device"
                    val whenActive = d.lastActiveAt?.take(16)?.replace('T', ' ') ?: "unknown"
                    val subtitle = buildString {
                        append(d.platform ?: "device")
                        append(" · last active ")
                        append(whenActive)
                        if (d.isCurrent) append(" · this device")
                    }
                    SettingsItem(
                        title = name,
                        subtitle = subtitle,
                        icon = AppIcons.Devices,
                        trailing = if (d.isCurrent) null else "Sign out",
                        onClick = {
                            val id = d.id ?: return@SettingsItem
                            if (d.isCurrent) return@SettingsItem
                            scope.launch {
                                runCatching { api.deleteMeDevicesId(id) }
                                    .onSuccess { reload() }
                                    .onFailure { error = it.message ?: "Revoke failed" }
                            }
                        },
                    )
                }
                SettingsList(listOf(null to items))
            }
        }
    }
}


@Composable
fun SettingsDevicesLinkScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf<String?>(null) }

    AivoraScaffold(title = "Link a device", onBack = onBack) {
        InfoBanner(
            "On the other device open Aivora → Link this device, then scan its QR or enter the 8-character code here.",
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Pairing code",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(8)
                    error = null
                    success = null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AivoraDimens.RadiusMd),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AivoraColors.SurfaceMuted,
                    focusedContainerColor = AivoraColors.SurfaceMuted,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AivoraColors.Primary,
                ),
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = AivoraColors.Danger, style = MaterialTheme.typography.bodySmall)
            }
            if (success != null) {
                Spacer(Modifier.height(8.dp))
                Text(success!!, color = AivoraColors.Primary, style = MaterialTheme.typography.bodySmall)
            }
        }
        PrimaryButton(
            text = if (busy) "Linking…" else "Link device",
            onClick = {
                if (busy || code.length < 6) {
                    error = "Enter the full code"
                    return@PrimaryButton
                }
                busy = true
                error = null
                scope.launch {
                    runCatching {
                        api.postAuthLinkApprove(
                            com.aivora.app.data.model.DeviceLinkCodeBody(code = code),
                        )
                    }.onSuccess {
                        success = "Device linked. It should finish signing in shortly."
                        code = ""
                        android.widget.Toast.makeText(
                            context,
                            "Device linked",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }.onFailure {
                        error = it.message ?: "Could not link — check the code"
                    }
                    busy = false
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        LinkButton(
            text = "Scan QR code instead",
            onClick = { onNavigate(Routes.SettingsQrScan) },
        )
        SettingsFootnote("Scan the QR shown on the other device, or type the code above.")
    }
}

@Composable
fun DeviceLinkPairScreen(
    onBack: (() -> Unit)? = null,
    onLinked: () -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.api.NetworkModule.api() }
    var code by remember { mutableStateOf<String?>(null) }
    var expiresIn by remember { mutableIntStateOf(180) }
    var error by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Starting…") }
    var qrBmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    fun startLink() {
        scope.launch {
            error = null
            status = "Starting…"
            qrBmp = null
            runCatching {
                api.postAuthLinkStart(
                    com.aivora.app.data.model.DeviceLinkStartBody(
                        deviceName = android.os.Build.MODEL,
                        platform = "android",
                    ),
                )
            }.onSuccess {
                val c = it.code
                code = c
                expiresIn = it.expiresIn
                status = "Waiting for approval on your phone…"
                if (!c.isNullOrBlank()) {
                    qrBmp = withContext(Dispatchers.Default) {
                        com.aivora.app.data.QrCodec.encodeBitmap(
                            com.aivora.app.data.QrCodec.linkPayload(c),
                        )
                    }
                }
            }.onFailure {
                error = it.message ?: "Could not start linking"
                status = "Failed"
            }
        }
    }

    LaunchedEffect(Unit) { startLink() }

    LaunchedEffect(code) {
        val c = code ?: return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(2000)
            val poll = runCatching { api.getAuthLinkApprovedCode(c) }.getOrNull() ?: continue
            when (poll.status) {
                "pending" -> {
                    expiresIn = poll.expiresIn ?: expiresIn
                    status = "Waiting for approval… (${expiresIn}s)"
                }
                "approved" -> {
                    val access = poll.access
                    val refresh = poll.refresh
                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        com.aivora.app.data.local.SessionStore.save(
                            context,
                            access,
                            refresh,
                            poll.phone,
                        )
                        runCatching {
                            val me = api.getMe()
                            me.id?.let {
                                com.aivora.app.data.local.SessionStore.saveUserId(context, it)
                            }
                        }
                        com.aivora.app.data.push.PushRegistrar.refresh(context)
                        status = "Linked"
                        onLinked()
                        return@LaunchedEffect
                    }
                }
                else -> {
                    error = "Link ${poll.status ?: "failed"}"
                    return@LaunchedEffect
                }
            }
        }
    }

    AivoraScaffold(title = "Link this device", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "Scan this QR on your phone",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Settings → Linked devices → Link a device → Scan QR",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
            )
            Spacer(Modifier.height(20.dp))
            val bmp = qrBmp
            if (bmp != null) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Device link QR",
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AivoraColors.SurfaceMuted),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("····", color = AivoraColors.TextSecondary)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Or enter this code",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                code ?: "········",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
            )
            Spacer(Modifier.height(16.dp))
            Text(status, color = AivoraColors.TextSecondary)
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = AivoraColors.Danger)
            }
        }
        PrimaryButton(
            text = "New code",
            onClick = {
                val old = code
                if (old != null) {
                    scope.launch {
                        runCatching {
                            api.postAuthLinkCancel(
                                com.aivora.app.data.model.DeviceLinkCodeBody(code = old),
                            )
                        }
                    }
                }
                code = null
                qrBmp = null
                startLink()
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsPermissionsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var tick by remember { mutableIntStateOf(0) }

    fun granted(permission: String): Boolean =
        androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            permission,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            android.net.Uri.fromParts("package", context.packageName, null),
        )
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun label(ok: Boolean) = if (ok) "Allowed" else "Not allowed — tap to open settings"

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) tick += 1
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    // Re-read permission state when returning from system settings.
    @Suppress("UNUSED_EXPRESSION")
    tick

    val notifOk = if (android.os.Build.VERSION.SDK_INT >= 33) {
        granted(android.Manifest.permission.POST_NOTIFICATIONS)
    } else {
        true
    }
    val photosOk = if (android.os.Build.VERSION.SDK_INT >= 33) {
        granted(android.Manifest.permission.READ_MEDIA_IMAGES) ||
            granted(android.Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        granted(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Camera",
                label(granted(android.Manifest.permission.CAMERA)),
                AppIcons.Photo,
                onClick = { openAppSettings() },
            ),
            SettingsItem(
                "Microphone",
                label(granted(android.Manifest.permission.RECORD_AUDIO)),
                iconRes = AivoraIcons.Mic,
                onClick = { openAppSettings() },
            ),
            SettingsItem(
                "Contacts",
                label(granted(android.Manifest.permission.READ_CONTACTS)),
                AppIcons.Person,
                onClick = { openAppSettings() },
            ),
            SettingsItem(
                "Location",
                label(
                    granted(android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                        granted(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                ),
                AppIcons.LocationOn,
                onClick = { openAppSettings() },
            ),
            SettingsItem(
                "Notifications",
                label(notifOk),
                iconRes = AivoraIcons.Notifications,
                onClick = { openAppSettings() },
            ),
            SettingsItem(
                "Photos and videos",
                label(photosOk),
                AppIcons.Photo,
                onClick = { openAppSettings() },
            ),
        ),
    )

    AivoraScaffold(title = "Permissions", onBack = onBack) {
        InfoBanner("Shows system permission status. Tap a row to change it in Android settings.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsCommunitiesScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var communities by remember {
        mutableStateOf<List<com.aivora.app.data.model.CommunityDto>>(emptyList())
    }
    var loading by remember { mutableStateOf(true) }
    var creating by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            loading = communities.isEmpty()
            runCatching { api.getCommunities() }
                .onSuccess { communities = it.items }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    AivoraScaffold(title = "Communities", onBack = onBack) {
        InfoBanner("A community keeps related groups together with one announcement group.")
        when {
            loading -> Text(
                "Loading…",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            else -> SettingsList(
                listOf(
                    null to buildList {
                        add(
                            SettingsItem(
                                "Create community",
                                "You become admin",
                                iconRes = AivoraIcons.Group,
                                onClick = {
                                    if (!creating) {
                                        creating = true
                                        scope.launch {
                                            runCatching {
                                                api.postCommunities(
                                                    com.aivora.app.data.model.CommunityCreateBody(
                                                        name = "My community",
                                                        description = "Neighborhood groups and announcements.",
                                                    ),
                                                )
                                            }.onSuccess { c ->
                                                val id = c.id
                                                if (id != null) {
                                                    communities = listOf(c) + communities
                                                    onNavigate(Routes.CommunityId.replace("{id}", id))
                                                } else {
                                                    reload()
                                                }
                                            }.onFailure {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    it.message ?: "Create failed",
                                                    android.widget.Toast.LENGTH_SHORT,
                                                ).show()
                                            }
                                            creating = false
                                        }
                                    }
                                },
                            ),
                        )
                        communities.mapNotNullTo(this) { c ->
                            val id = c.id ?: return@mapNotNullTo null
                            SettingsItem(
                                c.name ?: "Community",
                                subtitle = "${c.memberCount} members · ${c.groupCount} groups",
                                iconRes = AivoraIcons.Group,
                                onClick = {
                                    onNavigate(Routes.CommunityId.replace("{id}", id))
                                },
                            )
                        }
                    },
                ),
            )
        }
    }
}


@Composable
fun SettingsDeleteAccountScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val storedPhone = remember { com.aivora.app.data.local.SessionStore.phone(context).orEmpty() }
    var dial by remember {
        mutableStateOf(
            when {
                storedPhone.startsWith("+92") -> "+92"
                storedPhone.startsWith("+") && storedPhone.length > 3 -> storedPhone.take(3)
                else -> "+92"
            },
        )
    }
    var phone by remember {
        mutableStateOf(
            storedPhone.removePrefix(dial).filter { it.isDigit() },
        )
    }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val canDelete = phone.length >= 7 && !busy
    AivoraScaffold(title = "Delete account", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Deleting your account is permanent",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Text(
                "This action cannot be undone. You won’t be able to restore this account after deletion.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Spacer(Modifier.height(12.dp))
            SettingsMutedCard {
                SettingsRow(SettingsItem("Delete your Aivora account", icon = AppIcons.Person))
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                SettingsRow(SettingsItem("Remove you from your groups", iconRes = AivoraIcons.Group))
                HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                SettingsRow(SettingsItem("Delete your app data and chat history", iconRes = AivoraIcons.Chat))
            }
            SettingsFootnote("Messages other people already have in their chats won’t be removed.")
            SettingsSectionLabel("Confirm your phone number")
            SettingsMutedCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(dial, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it.filter { c -> c.isDigit() }
                            error = null
                        },
                        placeholder = { Text("Phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }
            SettingsFootnote("Enter the number registered to this account.")
            if (error != null) {
                Text(
                    error!!,
                    color = AivoraColors.Danger,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            SettingsMutedCard {
                SettingsRow(
                    SettingsItem(
                        "Change number instead",
                        subtitle = "Keep your account with a new number",
                        icon = AppIcons.Sync,
                        onClick = { onNavigate(Routes.SettingsAccountChangeNumber) },
                    ),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            text = if (busy) "Deleting…" else "Delete my account",
            onClick = {
                busy = true
                error = null
                scope.launch {
                    runCatching {
                        api.postMeDelete(
                            mapOf(
                                "country" to dial.filter { it.isDigit() },
                                "national_number" to phone,
                            ),
                        )
                    }.onSuccess { res ->
                        if (res.isSuccessful || res.code() == 204) {
                            com.aivora.app.data.local.SessionStore.clear(context)
                            onNavigate(Routes.SettingsAccountDeleted)
                        } else {
                            error = when (res.code()) {
                                400 -> "Phone number doesn’t match"
                                else -> "Delete failed (${res.code()})"
                            }
                        }
                    }.onFailure { error = it.message ?: "Delete failed" }
                    busy = false
                }
            },
            enabled = canDelete,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsAccountAddScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var confirmSwitch by remember { mutableStateOf(false) }

    if (confirmSwitch) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmSwitch = false },
            title = { Text("Switch number?") },
            text = {
                Text(
                    "Aivora stores one signed-in account on this device. Continuing signs you out of the current number so you can verify a different one. Multi-account switching is not available yet.",
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        confirmSwitch = false
                        com.aivora.app.data.local.SessionStore.clear(context)
                        onNavigate(Routes.Login)
                    },
                ) { Text("Sign out & continue") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmSwitch = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    AivoraScaffold(title = "Add account", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.PersonAdd,
            title = "One account per install",
            body = "You can’t keep two Aivora accounts signed in at once yet. To use a different number, sign out of this one first. Passkeys and email below are for securing this account — not a second login.",
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem(
                        "Sign in with another number",
                        "Signs out of this account, then opens verification",
                        iconRes = AivoraIcons.Call,
                        onClick = { confirmSwitch = true },
                    ),
                    SettingsItem(
                        "Passkeys",
                        "Not available yet — opens the passkeys note",
                        AppIcons.Fingerprint,
                        onClick = { onNavigate(Routes.SettingsAccountPasskeys) },
                    ),
                    SettingsItem(
                        "Email for this account",
                        "Add or change the recovery email on this number",
                        AppIcons.Email,
                        onClick = { onNavigate(Routes.SettingsAccountEmail) },
                    ),
                ),
            ),
        )
    }
}


@Composable
fun SettingsAccountChangeNumberScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var step by remember { mutableIntStateOf(0) }
    var dial by remember { mutableStateOf("+92") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var e164 by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var devHint by remember { mutableStateOf(false) }

    AivoraScaffold(title = "Change number", onBack = onBack) {
        if (step == 0) {
            SettingsHeroIntro(
                icon = AppIcons.Sync,
                title = "Move to a new phone number",
                body = "Changing your number moves your account information, profile, groups and settings to your new number.\n\nBefore you continue, make sure your new number can receive SMS messages or calls.",
            )
            SettingsMutedCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(dial, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it.filter { c -> c.isDigit() }.take(12)
                            error = null
                        },
                        placeholder = { Text("New phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }
            if (error != null) {
                Text(
                    error!!,
                    color = AivoraColors.Danger,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = if (busy) "Sending…" else "Send code",
                enabled = phone.length >= 7 && !busy,
                onClick = {
                    busy = true
                    error = null
                    scope.launch {
                        runCatching {
                            api.postMeChangeNumberRequest(
                                com.aivora.app.data.model.OtpRequestBody(
                                    country = dial.filter { it.isDigit() },
                                    nationalNumber = phone,
                                ),
                            )
                        }.onSuccess { res ->
                            e164 = res.phone ?: "+${dial.filter { it.isDigit() }}$phone"
                            devHint = res.otpDevBypass
                            step = 1
                        }.onFailure { error = it.message ?: "Could not send code" }
                        busy = false
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else {
            SettingsHeroIntro(
                icon = AppIcons.Sync,
                title = "Enter the code",
                body = "Sent to ${e164 ?: "$dial $phone"}",
            )
            if (devHint) {
                InfoBanner("Dev: use 000000 or the code from the server/admin OTP log.")
            }
            SettingsMutedCard {
                OutlinedTextField(
                    value = otp,
                    onValueChange = {
                        otp = it.filter { c -> c.isDigit() }.take(6)
                        error = null
                    },
                    placeholder = { Text("6-digit code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
            if (error != null) {
                Text(
                    error!!,
                    color = AivoraColors.Danger,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = if (busy) "Verifying…" else "Verify",
                enabled = otp.length == 6 && !busy,
                onClick = {
                    val target = e164 ?: return@PrimaryButton
                    busy = true
                    error = null
                    scope.launch {
                        runCatching {
                            api.postMeChangeNumberVerify(
                                mapOf("phone" to target, "code" to otp),
                            )
                        }.onSuccess { user ->
                            val newPhone = user.phone ?: target
                            val access = com.aivora.app.data.local.SessionStore.accessToken(context)
                            val refresh = com.aivora.app.data.local.SessionStore.refreshToken(context)
                            if (access != null && refresh != null) {
                                com.aivora.app.data.local.SessionStore.save(
                                    context,
                                    access,
                                    refresh,
                                    newPhone,
                                )
                            }
                            onBack?.invoke()
                        }.onFailure { error = it.message ?: "Invalid code" }
                        busy = false
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}


@Composable
fun SettingsAccountDeletedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Create new account", null, AppIcons.Person, onClick = { onNavigate(Routes.Login) }),
        ),
    )

    AivoraScaffold(title = "Account deleted", onBack = onBack) {
        InfoBanner("Account deleted")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountEmailScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var email by remember { mutableStateOf("") }
    var verified by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var awaitingCode by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var useBypassHint by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        runCatching { api.getMeEmail() }
            .onSuccess {
                email = it.email.orEmpty()
                verified = it.verified
            }
            .onFailure { error = it.message }
        loading = false
    }

    AivoraScaffold(title = "Email address", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (verified) "Verified recovery email" else "Add your email address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Used for two-step PIN reset and optional email sign-in. It won’t be shown to your contacts.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "Email address",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.trim()
                    if (verified) verified = false
                    awaitingCode = false
                },
                enabled = !verified && !busy && !loading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AivoraDimens.RadiusMd),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = AivoraColors.SurfaceMuted,
                    focusedContainerColor = AivoraColors.SurfaceMuted,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AivoraColors.Primary,
                ),
            )
            if (awaitingCode) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Verification code",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter { c -> c.isDigit() }.take(6) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AivoraDimens.RadiusMd),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = AivoraColors.SurfaceMuted,
                        focusedContainerColor = AivoraColors.SurfaceMuted,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = AivoraColors.Primary,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (useBypassHint) {
                        "Staging: use code 000000 (or admin OTP log)."
                    } else {
                        "Check your inbox for the verification code."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                )
            } else if (!verified) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "We’ll ask you to verify this address.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                )
            }
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = AivoraColors.Danger, style = MaterialTheme.typography.bodySmall)
            }
        }
        if (verified) {
            PrimaryButton(
                text = if (busy) "Removing…" else "Remove email",
                onClick = {
                    if (busy) return@PrimaryButton
                    busy = true
                    error = null
                    scope.launch {
                        runCatching { api.deleteMeEmail() }
                            .onSuccess {
                                email = ""
                                verified = false
                                awaitingCode = false
                                code = ""
                            }
                            .onFailure { error = it.message ?: "Could not remove" }
                        busy = false
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else if (awaitingCode) {
            PrimaryButton(
                text = if (busy) "Verifying…" else "Verify",
                onClick = {
                    if (busy || code.length < 4 || email.isBlank()) return@PrimaryButton
                    busy = true
                    error = null
                    scope.launch {
                        runCatching {
                            api.postMeEmailVerify(
                                com.aivora.app.data.model.AccountEmailVerifyBody(
                                    email = email,
                                    code = code,
                                ),
                            )
                        }.onSuccess {
                            verified = it.verified
                            email = it.email.orEmpty()
                            awaitingCode = false
                            code = ""
                            android.widget.Toast.makeText(
                                context,
                                "Email verified",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }.onFailure { error = it.message ?: "Invalid code" }
                        busy = false
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        } else {
            PrimaryButton(
                text = if (busy) "Sending…" else "Send verification",
                onClick = {
                    if (busy || !email.contains("@")) return@PrimaryButton
                    busy = true
                    error = null
                    scope.launch {
                        runCatching {
                            api.postMeEmailRequest(
                                com.aivora.app.data.model.AccountEmailRequestBody(email = email),
                            )
                        }.onSuccess {
                            awaitingCode = true
                            useBypassHint = it.otpDevBypass
                        }.onFailure { error = it.message ?: "Could not send code" }
                        busy = false
                    }
                },
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
    }
}


@Composable
fun SettingsAccountPasskeysScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Passkeys", onBack = onBack) {
        SettingsHeroIntro(
            iconRes = AivoraIcons.Key,
            title = "Passkeys coming later",
            body = "FIDO2 passkeys are not available yet. Use two-step verification, app lock, or email sign-in for account security today.",
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "Open app lock",
            onClick = { onNavigate(Routes.SettingsPrivacyAppLock) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsAccountRequestInfoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var busy by remember { mutableStateOf(false) }

    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Request report",
                subtitle = if (busy) "Preparing…" else "Account information report (.json)",
                icon = AppIcons.Info,
                onClick = {
                    if (!useRemote) {
                        android.widget.Toast.makeText(
                            context,
                            "Sign in to request a report",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                        return@SettingsItem
                    }
                    if (busy) return@SettingsItem
                    busy = true
                    scope.launch {
                        val ok = com.aivora.app.data.AccountInfoExport.downloadAndShare(context, api)
                        busy = false
                        if (ok) {
                            android.widget.Toast.makeText(
                                context,
                                "Report ready — choose where to save",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
            ),
        ),
    )

    AivoraScaffold(title = "Request account info", onBack = onBack) {
        InfoBanner("Download a copy of your account information and settings. Chat messages are not included.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountSecurityNotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var show by remember {
        mutableStateOf(com.aivora.app.data.local.SecurityNotifPrefs.show(context))
    }
    AivoraScaffold(title = "Security notifications", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.Security,
            title = "Your chats are end-to-end encrypted",
            body = "End-to-end encryption keeps your personal messages and calls between you and the people you choose. Not even Aivora can read or listen to them.",
        )
        Column(
            modifier = Modifier
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            SettingsRow(
                SettingsItem(
                    "Show security notifications on this device",
                    toggle = show,
                    onToggle = {
                        show = it
                        com.aivora.app.data.local.SecurityNotifPrefs.setShow(context, it)
                    },
                ),
            )
        }
        SettingsFootnote(
            "Preference is saved on this device. Identity-key change alerts will use this setting when E2EE ships.",
        )
    }
}


@Composable
fun SettingsAccountTwoStepNewPinScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var alreadyOn by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        runCatching { api.getMeTwoStep() }.onSuccess { alreadyOn = it.enabled }
    }

    AivoraScaffold(title = "New PIN", onBack = onBack) {
        InfoBanner("Enter a 6-digit PIN for two-step verification.")
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            if (alreadyOn) {
                Text("Current PIN", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it.filter { c -> c.isDigit() }.take(6) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
            }
            Text("New PIN", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("Confirm PIN", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it.filter { c -> c.isDigit() }.take(6) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = AivoraColors.Danger)
            }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = if (busy) "Saving…" else "Save PIN",
            enabled = pin.length == 6 && confirm == pin && (!alreadyOn || current.length == 6) && !busy,
            onClick = {
                if (pin != confirm) {
                    error = "PINs don’t match"
                    return@PrimaryButton
                }
                busy = true
                error = null
                scope.launch {
                    runCatching {
                        api.patchMeTwoStep(
                            com.aivora.app.data.model.TwoStepEnableBody(
                                pin = pin,
                                current = current.takeIf { alreadyOn },
                            ),
                        )
                    }.onSuccess {
                        onNavigate(Routes.SettingsTwoStep)
                    }.onFailure { error = it.message ?: "Could not save PIN" }
                    busy = false
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsChatsArchivedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val composer by com.aivora.app.data.local.ChatComposerPrefs.flow.collectAsState()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    var confirmArchiveAll by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    if (confirmArchiveAll) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { if (!busy) confirmArchiveAll = false },
            title = { Text("Archive all chats?") },
            text = { Text("All chats move to Archived. You can unarchive them later.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        busy = true
                        scope.launch {
                            val chats = com.aivora.app.data.local.InMemoryChatStore.chats.value
                                .filter { !it.archived }
                            var ok = 0
                            for (chat in chats) {
                                com.aivora.app.data.local.InMemoryChatStore.upsertChat(
                                    chat.copy(archived = true),
                                    promote = false,
                                )
                                if (useRemote) {
                                    runCatching {
                                        api.putConversationsIdSettings(
                                            chat.id,
                                            com.aivora.app.data.model.ConversationSettingsBody(archived = true),
                                        )
                                    }.onSuccess { ok++ }
                                } else {
                                    ok++
                                }
                            }
                            busy = false
                            confirmArchiveAll = false
                            android.widget.Toast.makeText(
                                context,
                                if (chats.isEmpty()) "Nothing to archive"
                                else "Archived $ok chat${if (ok == 1) "" else "s"}",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    enabled = !busy,
                ) { Text(if (busy) "Working…" else "Archive all") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { confirmArchiveAll = false },
                    enabled = !busy,
                ) { Text("Cancel") }
            },
        )
    }

    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Keep chats archived",
                "New messages stay archived.",
                iconRes = AivoraIcons.Chat,
                toggle = composer.keepArchived,
                onToggle = { com.aivora.app.data.local.ChatComposerPrefs.setKeepArchived(context, it) },
            ),
            SettingsItem(
                "Archive all chats",
                null,
                iconRes = AivoraIcons.Chat,
                onClick = { confirmArchiveAll = true },
            ),
        ),
    )

    AivoraScaffold(title = "Archived chats", onBack = onBack) {
        InfoBanner("Archive hides chats from your main list without deleting messages.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var videos by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.includeVideos(context))
    }
    var wifiOnly by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.wifiOnly(context))
    }
    var lastLabel by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.formatLastBackup(context))
    }
    var sizeLabel by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.formatSize(context))
    }
    var frequency by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.frequency(context))
    }
    var busy by remember { mutableStateOf(false) }

    fun refreshMeta() {
        lastLabel = com.aivora.app.data.local.BackupPrefs.formatLastBackup(context)
        sizeLabel = com.aivora.app.data.local.BackupPrefs.formatSize(context)
        frequency = com.aivora.app.data.local.BackupPrefs.frequency(context)
    }

    AivoraScaffold(title = "Chat backup", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.CloudUpload,
            title = "Chat backup",
            body = "Download a zip of your chats, or turn on automatic backups to keep recent zips on this device. Signing in on a new phone restores live chats from the server.",
        )
        Column(
            modifier = Modifier
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            SettingsRow(
                SettingsItem(
                    "Last successful backup",
                    subtitle = lastLabel,
                ),
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = AivoraColors.Divider,
            )
            SettingsRow(SettingsItem("Total size", trailing = sizeLabel))
        }
        PrimaryButton(
            text = if (busy) "Backing up…" else "Back up",
            onClick = {
                if (!useRemote || busy) {
                    if (!useRemote) {
                        android.widget.Toast.makeText(
                            context,
                            "Sign in to back up chats",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                    return@PrimaryButton
                }
                busy = true
                scope.launch {
                    val ok = com.aivora.app.data.ChatBackup.downloadAndShare(
                        context = context,
                        api = api,
                        includeMedia = videos,
                    )
                    busy = false
                    if (ok) {
                        refreshMeta()
                        android.widget.Toast.makeText(
                            context,
                            "Backup ready — choose where to save",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            SettingsRow(
                SettingsItem(
                    "Save location",
                    subtitle = "Share sheet / Files",
                    onClick = { onNavigate(Routes.SettingsChatsBackupLocation) },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Automatic backups",
                    trailing = frequency,
                    onClick = { onNavigate(Routes.SettingsChatsBackupFrequency) },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Include media",
                    toggle = videos,
                    onToggle = {
                        videos = it
                        com.aivora.app.data.local.BackupPrefs.setIncludeVideos(context, it)
                    },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Wi-Fi only",
                    toggle = wifiOnly,
                    onToggle = {
                        wifiOnly = it
                        com.aivora.app.data.local.BackupPrefs.setWifiOnly(context, it)
                    },
                ),
            )
        }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                .background(AivoraColors.SurfaceMuted),
        ) {
            SettingsRow(
                SettingsItem(
                    "Open backup",
                    subtitle = "Browse zip · share transcripts",
                    icon = AppIcons.Folder,
                    onClick = { onNavigate(Routes.SettingsChatsBackupOpen) },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "End-to-end encrypted backup",
                    subtitle = "Not available yet",
                    iconRes = AivoraIcons.Lock,
                    onClick = { onNavigate(Routes.SettingsChatsBackupEncrypted) },
                ),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}


@Composable
fun SettingsChatsBackupEncryptedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Encrypted backup", onBack = onBack) {
        InfoBanner(
            "Encrypted cloud backup is not available yet. Use Back up to download a portable zip of your chats.",
        )
    }
}


@Composable
fun SettingsChatsBackupFrequencyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selected by remember {
        mutableStateOf(com.aivora.app.data.local.BackupPrefs.frequency(context))
    }
    val options = listOf(
        com.aivora.app.data.local.BackupPrefs.FREQ_DAILY,
        com.aivora.app.data.local.BackupPrefs.FREQ_WEEKLY,
        com.aivora.app.data.local.BackupPrefs.FREQ_MONTHLY,
        com.aivora.app.data.local.BackupPrefs.FREQ_OFF,
    )
    val sections = listOf(
        null to options.map { label ->
            SettingsItem(
                label,
                trailing = if (label == selected) "✓" else null,
                icon = AppIcons.Timer,
                onClick = {
                    selected = label
                    com.aivora.app.data.local.BackupPrefs.setFrequency(context, label)
                    android.widget.Toast.makeText(
                        context,
                        if (label == com.aivora.app.data.local.BackupPrefs.FREQ_OFF) {
                            "Automatic backups off"
                        } else {
                            "Automatic backups: $label"
                        },
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        },
    )

    AivoraScaffold(title = "Backup frequency", onBack = onBack) {
        InfoBanner(
            "When enabled, Aivora downloads a zip into this app’s private storage on a schedule " +
                "(Wi-Fi only respected). The system may delay runs while idle. Tap Back up anytime to share a zip.",
        )
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupLocationScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Backup location", onBack = onBack) {
        InfoBanner(
            "Manual Back up opens the share sheet (Files, Drive, etc.). " +
                "Automatic backups stay in Aivora’s private app storage on this phone (last 5 kept).",
        )
    }
}


@Composable
fun SettingsChatsBackupOpenScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var sourceUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var sourceFile by remember { mutableStateOf<java.io.File?>(null) }
    var inspection by remember {
        mutableStateOf<com.aivora.app.data.BackupZipBrowser.Inspection?>(null)
    }
    var busy by remember { mutableStateOf(false) }
    var shareBusy by remember { mutableStateOf(false) }
    val localAutos = remember {
        com.aivora.app.data.BackupZipBrowser.localAutoBackups(context)
    }

    fun loadUri(uri: android.net.Uri) {
        busy = true
        sourceUri = uri
        sourceFile = null
        scope.launch {
            inspection = com.aivora.app.data.BackupZipBrowser.inspectUri(context, uri)
            busy = false
        }
    }

    fun loadFile(file: java.io.File) {
        busy = true
        sourceUri = null
        sourceFile = file
        scope.launch {
            inspection = com.aivora.app.data.BackupZipBrowser.inspectFile(file)
            busy = false
        }
    }

    val pick = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) loadUri(uri)
    }

    AivoraScaffold(title = "Open backup", onBack = onBack) {
        InfoBanner(
            "Browse an Aivora backup zip and share individual chat transcripts. " +
                "Live restore still happens by signing in — this doesn’t re-upload chats to the server.",
        )
        PrimaryButton(
            text = "Choose backup zip",
            onClick = { pick.launch("application/zip") },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        if (localAutos.isNotEmpty() && inspection == null) {
            Text(
                "Automatic backups on this device",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp),
            )
            SettingsMutedCard {
                localAutos.take(5).forEach { file ->
                    SettingsRow(
                        SettingsItem(
                            file.name,
                            subtitle = android.text.format.DateFormat.getMediumDateFormat(context)
                                .format(java.util.Date(file.lastModified())),
                            onClick = { loadFile(file) },
                        ),
                    )
                }
            }
        }
        when {
            busy -> Text(
                "Reading backup…",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            inspection?.error != null -> Text(
                inspection!!.error!!,
                color = AivoraColors.Danger,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            inspection != null -> {
                val ins = inspection!!
                val meta = buildString {
                    ins.manifest?.format?.let { append(it) }
                    ins.manifest?.exportedAt?.let {
                        if (isNotEmpty()) append(" · ")
                        append(it.take(19).replace('T', ' '))
                    }
                    if (isEmpty()) append("${ins.chats.size} chats")
                    else append(" · ${ins.chats.size} chats")
                }
                Text(
                    meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
                if (ins.chats.isEmpty()) {
                    Text(
                        "No chat transcripts found in this zip.",
                        color = AivoraColors.TextSecondary,
                        modifier = Modifier.padding(24.dp),
                    )
                } else {
                    AivoraScrollColumn(modifier = Modifier.weight(1f)) {
                        SettingsMutedCard {
                            ins.chats.forEach { chat ->
                                SettingsRow(
                                    SettingsItem(
                                        chat.folder,
                                        subtitle = if (shareBusy) "Sharing…" else "Share .txt transcript",
                                        iconRes = AivoraIcons.Chat,
                                        onClick = {
                                            if (!shareBusy) {
                                                shareBusy = true
                                                scope.launch {
                                                    val ok = com.aivora.app.data.BackupZipBrowser.shareChatTxt(
                                                        context = context,
                                                        source = sourceUri,
                                                        sourceFile = sourceFile,
                                                        chat = chat,
                                                    )
                                                    shareBusy = false
                                                    if (!ok) {
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            "Could not share that chat",
                                                            android.widget.Toast.LENGTH_SHORT,
                                                        ).show()
                                                    }
                                                }
                                            }
                                        },
                                    ),
                                )
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
                LinkButton(
                    text = "Choose another zip",
                    onClick = {
                        inspection = null
                        sourceUri = null
                        sourceFile = null
                        pick.launch("application/zip")
                    },
                )
            }
        }
    }
}


@Composable
fun SettingsChatsExportScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val chats by com.aivora.app.data.local.InMemoryChatStore.chats.collectAsState(
        initial = emptyList(),
    )
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val selected = chats.firstOrNull { it.id == selectedId }

    fun runExport(includeMedia: Boolean) {
        val chat = selected ?: return
        if (!useRemote || busy) return
        busy = true
        scope.launch {
            val ok = com.aivora.app.data.ChatExport.downloadAndShare(
                context = context,
                api = api,
                conversationId = chat.id,
                chatTitle = chat.title,
                includeMedia = includeMedia,
            )
            busy = false
            if (ok) {
                android.widget.Toast.makeText(context, "Chat exported", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (selected == null) {
        val sections = listOf(
            null to chats
                .filter { !it.archived }
                .map { chat ->
                    SettingsItem(
                        chat.title,
                        subtitle = if (chat.isGroup) "Group" else "Chat",
                        iconRes = if (chat.isGroup) AivoraIcons.Group else AivoraIcons.Chat,
                        onClick = { selectedId = chat.id },
                    )
                },
        )
        AivoraScaffold(title = "Export chat", onBack = onBack) {
            InfoBanner(
                if (!useRemote) "Sign in to export chats from the server."
                else if (chats.isEmpty()) "No chats to export yet."
                else "Choose a chat to export.",
            )
            if (useRemote && chats.isNotEmpty()) {
                SettingsList(sections)
            }
        }
        return
    }

    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Without media",
                subtitle = if (busy) "Exporting…" else "Text file (.txt)",
                iconRes = AivoraIcons.Chat,
                onClick = { runExport(false) },
            ),
            SettingsItem(
                "Include media",
                subtitle = if (busy) "Exporting…" else "Zip with attachments",
                icon = AppIcons.Photo,
                onClick = { runExport(true) },
            ),
        ),
    )

    AivoraScaffold(
        title = selected.title,
        onBack = { selectedId = null },
    ) {
        InfoBanner("Export “${selected.title}” without media or with media.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsFontSizeScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val current by com.aivora.app.data.local.FontScaleStore.scaleFlow.collectAsState()
    fun pick(scale: Float) {
        com.aivora.app.data.local.FontScaleStore.set(context, scale)
    }
    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Small",
                null,
                iconRes = AivoraIcons.Chat,
                selected = current <= 0.95f,
                onClick = { pick(com.aivora.app.data.local.FontScaleStore.SMALL) },
            ),
            SettingsItem(
                "Medium",
                null,
                iconRes = AivoraIcons.Chat,
                selected = current > 0.95f && current < 1.1f,
                onClick = { pick(com.aivora.app.data.local.FontScaleStore.MEDIUM) },
            ),
            SettingsItem(
                "Large",
                null,
                iconRes = AivoraIcons.Chat,
                selected = current >= 1.1f,
                onClick = { pick(com.aivora.app.data.local.FontScaleStore.LARGE) },
            ),
        ),
    )

    AivoraScaffold(title = "Font size", onBack = onBack) {
        InfoBanner("Message text size — applies across the app.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsHistoryScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var confirmClear by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    if (confirmClear || confirmDelete) {
        val deleting = confirmDelete
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                if (!busy) {
                    confirmClear = false
                    confirmDelete = false
                }
            },
            title = { Text(if (deleting) "Delete all chats?" else "Clear all chats?") },
            text = {
                Text(
                    if (deleting) {
                        "Removes every chat from your list. You leave groups; direct chats are deleted for you."
                    } else {
                        "Clears messages in every chat. Chats stay in your list."
                    },
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (busy) return@TextButton
                        busy = true
                        scope.launch {
                            val chats = com.aivora.app.data.local.InMemoryChatStore.chats.value.toList()
                            chats.forEach { chat ->
                                runCatching { api.postConversationsIdClear(chat.id) }
                                if (deleting) {
                                    runCatching { api.postConversationsIdLeave(chat.id) }
                                    com.aivora.app.data.local.InMemoryChatStore.removeChat(chat.id, context)
                                } else {
                                    com.aivora.app.data.local.InMemoryChatStore.clearChat(chat.id, context)
                                }
                            }
                            busy = false
                            confirmClear = false
                            confirmDelete = false
                            android.widget.Toast.makeText(
                                context,
                                if (deleting) "Chats deleted" else "Chats cleared",
                                android.widget.Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                ) { Text(if (busy) "…" else if (deleting) "Delete" else "Clear") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (!busy) {
                            confirmClear = false
                            confirmDelete = false
                        }
                    },
                ) { Text("Cancel") }
            },
        )
    }

    val sections = listOf(
        null to listOf(
            SettingsItem("Export chat", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem(
                "Chat transcripts",
                "Generate a zip of selected chats",
                iconRes = AivoraIcons.Chat,
                onClick = { onNavigate(Routes.SettingsChatsTranscripts) },
            ),
            SettingsItem("Archive all chats", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Clear all chats", null, AppIcons.Delete, onClick = { confirmClear = true }),
            SettingsItem("Delete all chats", null, AppIcons.Delete, danger = true, onClick = { confirmDelete = true }),
        ),
    )

    AivoraScaffold(title = "Chat history", onBack = onBack) {
        InfoBanner("Manage chat history on this device.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsTranscriptsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val chats by com.aivora.app.data.local.InMemoryChatStore.chats.collectAsState(
        initial = emptyList(),
    )
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val visible = remember(chats) { chats.filter { !it.archived } }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var busy by remember { mutableStateOf(false) }

    AivoraScaffold(title = "Chat transcripts", onBack = onBack) {
        InfoBanner(
            if (!useRemote) "Sign in to generate transcripts from the server."
            else "Select one or more chats. Generates a text-only zip (no media) you can share or save.",
        )
        if (!useRemote) return@AivoraScaffold
        if (visible.isEmpty()) {
            Text(
                "No chats to export yet.",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            return@AivoraScaffold
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LinkButton(
                text = if (selected.size == visible.size) "Clear all" else "Select all",
                onClick = {
                    selected = if (selected.size == visible.size) emptySet()
                    else visible.map { it.id }.toSet()
                },
            )
            Text(
                "${selected.size} selected",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
            )
        }
        AivoraScrollColumn(modifier = Modifier.weight(1f)) {
            SettingsMutedCard {
                visible.forEach { chat ->
                    val on = chat.id in selected
                    SettingsRow(
                        SettingsItem(
                            chat.title,
                            subtitle = if (chat.isGroup) "Group" else "Chat",
                            iconRes = if (chat.isGroup) AivoraIcons.Group else AivoraIcons.Chat,
                            selected = on,
                            onClick = {
                                selected = if (on) selected - chat.id else selected + chat.id
                            },
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryButton(
            text = if (busy) "Generating…" else "Generate transcripts",
            onClick = {
                if (busy || selected.isEmpty()) {
                    if (selected.isEmpty()) {
                        android.widget.Toast.makeText(
                            context,
                            "Select at least one chat",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                    return@PrimaryButton
                }
                busy = true
                val refs = visible
                    .filter { it.id in selected }
                    .map {
                        com.aivora.app.data.ChatTranscripts.ChatRef(id = it.id, title = it.title)
                    }
                scope.launch {
                    val ok = com.aivora.app.data.ChatTranscripts.downloadAndShareZip(
                        context = context,
                        api = api,
                        chats = refs,
                    )
                    busy = false
                    if (ok) {
                        android.widget.Toast.makeText(
                            context,
                            "Transcripts ready",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
    }
}


@Composable
fun SettingsChatsTransferScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Transfer chats", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.PhoneForwarded,
            title = "Move chats to a new phone",
            body = "Peer-to-peer QR transfer is not available yet. Create a chat backup on this phone, move the zip yourself, then sign in with the same number on the new phone.",
        )
        SettingsMutedCard {
            SettingsRow(
                SettingsItem(
                    "Use the same phone number",
                    subtitle = "Register that number on your new phone",
                    icon = AppIcons.Person,
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Back up before you leave this phone",
                    subtitle = "Chat backup writes a zip you can copy off-device",
                    icon = AppIcons.Folder,
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Not a live phone-to-phone sync",
                    subtitle = "No QR handshake between devices yet",
                    icon = AppIcons.Info,
                ),
            )
        }
        SettingsFootnote("This guides you to local backup — not a cloud migrate or account merge.")
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "How to transfer",
            onClick = { onNavigate(Routes.TransferNewPhone) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsListsNewScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var name by remember { mutableStateOf("") }
    val chats = remember { com.aivora.app.data.local.InMemoryChatStore.chats.value.filter { !it.archived } }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AivoraScaffold(title = "New list", onBack = onBack) {
        InfoBanner("Name the list and pick at least one chat. Max 500 chats.")
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 40) name = it },
            label = { Text("List name") },
            placeholder = { Text("Favourites, Work, Family…") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AivoraColors.Primary,
                cursorColor = AivoraColors.Primary,
            ),
        )
        Text(
            "Chats",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        AivoraScrollColumn(modifier = Modifier.weight(1f)) {
            chats.forEach { chat ->
                val on = chat.id in selected
                SettingsRow(
                    SettingsItem(
                        chat.title,
                        subtitle = chat.preview,
                        selected = on,
                        onClick = {
                            selected = if (on) selected - chat.id else selected + chat.id
                        },
                    ),
                )
            }
            if (chats.isEmpty()) {
                Text(
                    "No chats yet — start a conversation first.",
                    modifier = Modifier.padding(24.dp),
                    color = AivoraColors.TextSecondary,
                )
            }
        }
        if (error != null) {
            Text(error!!, color = AivoraColors.Danger, modifier = Modifier.padding(horizontal = 24.dp))
        }
        PrimaryButton(
            text = if (busy) "Saving…" else "Create list",
            enabled = !busy && name.isNotBlank() && selected.isNotEmpty(),
            onClick = {
                busy = true
                error = null
                scope.launch {
                    runCatching {
                        api.postMeLists(
                            com.aivora.app.data.model.ChatListCreateBody(
                                name = name.trim(),
                                conversationIds = selected.toList(),
                            ),
                        )
                    }.onSuccess {
                        com.aivora.app.data.local.ChatListsStore.upsert(it)
                        busy = false
                        onNavigate(Routes.listsEdit(it.id))
                    }.onFailure {
                        busy = false
                        error = it.message ?: "Could not create list"
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsListsEditIdScreen(
    listId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var list by remember {
        mutableStateOf(com.aivora.app.data.local.ChatListsStore.get(listId))
    }
    var name by remember { mutableStateOf(list?.name.orEmpty()) }
    var selected by remember {
        mutableStateOf(list?.conversationIds?.toSet().orEmpty())
    }
    var pickChats by remember { mutableStateOf(false) }
    var renameOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val allChats = remember { com.aivora.app.data.local.InMemoryChatStore.chats.value }

    LaunchedEffect(listId) {
        runCatching { api.getMeLists() }
            .onSuccess {
                com.aivora.app.data.local.ChatListsStore.replace(it.items)
                val found = it.items.find { l -> l.id == listId }
                list = found
                name = found?.name.orEmpty()
                selected = found?.conversationIds?.toSet().orEmpty()
            }
    }

    if (renameOpen) {
        var draft by remember { mutableStateOf(name) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { renameOpen = false },
            title = { Text("Rename list") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 40) draft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        val next = draft.trim()
                        if (next.isBlank()) return@TextButton
                        renameOpen = false
                        scope.launch {
                            runCatching {
                                api.patchMeListsId(
                                    listId,
                                    com.aivora.app.data.model.ChatListPatchBody(name = next),
                                )
                            }.onSuccess {
                                com.aivora.app.data.local.ChatListsStore.upsert(it)
                                list = it
                                name = it.name
                            }
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { renameOpen = false }) { Text("Cancel") }
            },
        )
    }

    if (pickChats) {
        var draft by remember { mutableStateOf(selected) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickChats = false },
            title = { Text("Chats in list") },
            text = {
                Column(modifier = Modifier.height(320.dp).verticalScroll(rememberScrollState())) {
                    allChats.filter { !it.archived }.forEach { chat ->
                        val on = chat.id in draft
                        Text(
                            chat.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    draft = if (on) draft - chat.id else draft + chat.id
                                }
                                .padding(vertical = 10.dp),
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (on) AivoraColors.Primary else AivoraColors.Ink,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        pickChats = false
                        scope.launch {
                            runCatching {
                                api.patchMeListsId(
                                    listId,
                                    com.aivora.app.data.model.ChatListPatchBody(
                                        conversationIds = draft.toList(),
                                    ),
                                )
                            }.onSuccess {
                                com.aivora.app.data.local.ChatListsStore.upsert(it)
                                list = it
                                selected = it.conversationIds.toSet()
                            }
                        }
                    },
                ) { Text("Save") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pickChats = false }) { Text("Cancel") }
            },
        )
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete list?") },
            text = { Text("Deleting this list does not delete any chats.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        confirmDelete = false
                        scope.launch {
                            runCatching { api.deleteMeListsId(listId) }
                                .onSuccess {
                                    com.aivora.app.data.local.ChatListsStore.remove(listId)
                                    onBack?.invoke()
                                }
                        }
                    },
                ) { Text("Delete") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            },
        )
    }

    val memberChats = allChats.filter { it.id in selected }
    AivoraScaffold(title = name.ifBlank { "Edit list" }, onBack = onBack) {
        InfoBanner("${selected.size} chat${if (selected.size == 1) "" else "s"} in this list.")
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Rename list", trailing = name, iconRes = AivoraIcons.Edit, onClick = { renameOpen = true }),
                    SettingsItem("Add chats", iconRes = AivoraIcons.Chat, onClick = { pickChats = true }),
                    SettingsItem(
                        "Delete list",
                        "Deleting this list does not delete any chats.",
                        AppIcons.Delete,
                        danger = true,
                        onClick = { confirmDelete = true },
                    ),
                ),
            ),
        )
        memberChats.forEach { chat ->
            SettingsRow(
                SettingsItem(
                    chat.title,
                    subtitle = chat.preview,
                    onClick = { onNavigate(Routes.ChatId.replace("{id}", chat.id)) },
                ),
            )
        }
    }
}


@Composable
fun SettingsListsReorderScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    val storeLists by com.aivora.app.data.local.ChatListsStore.lists.collectAsState()
    var ordered by remember { mutableStateOf(storeLists) }
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var persistError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { api.getMeLists() }
            .onSuccess { com.aivora.app.data.local.ChatListsStore.replace(it.items) }
    }
    LaunchedEffect(storeLists) {
        if (dragIndex == null) ordered = storeLists
    }

    fun persistOrder(next: List<com.aivora.app.data.model.ChatListDto>) {
        val normalized = next.mapIndexed { i, l -> l.copy(sortOrder = i) }
        ordered = normalized
        com.aivora.app.data.local.ChatListsStore.replace(normalized)
        scope.launch {
            runCatching {
                api.putMeListsOrder(
                    com.aivora.app.data.model.ChatListOrderBody(ids = normalized.map { it.id }),
                )
            }.onSuccess {
                persistError = false
                com.aivora.app.data.local.ChatListsStore.replace(it.items)
            }.onFailure {
                persistError = true
                android.widget.Toast.makeText(
                    context,
                    "Could not save order",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    val rowPx = with(density) { com.aivora.app.ui.theme.AivoraDimens.SettingsRowHeight.toPx() }

    AivoraScaffold(title = "Lists", onBack = onBack) {
        InfoBanner("Long-press a list, then drag to reorder. Tap a name to edit.")
        if (persistError) {
            Text(
                "Order may be out of sync — drag again to retry.",
                color = AivoraColors.Danger,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        AivoraScrollColumn(modifier = Modifier.weight(1f)) {
            Text(
                "Default on Home",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 6.dp),
            )
            SettingsMutedCard {
                SettingsRow(SettingsItem("Favourites", subtitle = "Starred chats", iconRes = AivoraIcons.Star))
                SettingsRow(SettingsItem("Unread", iconRes = AivoraIcons.Chat))
                SettingsRow(SettingsItem("Groups", iconRes = AivoraIcons.Group))
            }
            Text(
                "Your lists",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
            )
            if (ordered.isEmpty()) {
                SettingsMutedCard {
                    SettingsRow(
                        SettingsItem(
                            "Create a list",
                            "Filter chats into Work, Family, and more",
                            AppIcons.Menu,
                            onClick = { onNavigate(Routes.SettingsListsNew) },
                        ),
                    )
                }
            } else {
                SettingsMutedCard {
                    ordered.forEachIndexed { index, list ->
                        val dragging = dragIndex == index
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    if (dragging) {
                                        translationY = dragOffset
                                        shadowElevation = 6.dp.toPx()
                                        alpha = 0.96f
                                    }
                                }
                                .zIndex(if (dragging) 1f else 0f)
                                .height(AivoraDimens.SettingsRowHeight)
                                .background(
                                    if (dragging) AivoraColors.LightSurface else Color.Transparent,
                                )
                                .pointerInput(list.id) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            dragIndex = ordered.indexOfFirst { it.id == list.id }
                                                .takeIf { it >= 0 }
                                            dragOffset = 0f
                                        },
                                        onDragEnd = {
                                            dragIndex = null
                                            dragOffset = 0f
                                            persistOrder(ordered)
                                        },
                                        onDragCancel = {
                                            dragIndex = null
                                            dragOffset = 0f
                                        },
                                        onDrag = { change, amount ->
                                            change.consume()
                                            val from = dragIndex
                                                ?: return@detectDragGesturesAfterLongPress
                                            dragOffset += amount.y
                                            val shift = (dragOffset / rowPx).toInt()
                                            val to = (from + shift).coerceIn(0, ordered.lastIndex)
                                            if (to != from) {
                                                ordered = ordered.toMutableList().also { mut ->
                                                    val item = mut.removeAt(from)
                                                    mut.add(to, item)
                                                }
                                                dragIndex = to
                                                dragOffset -= (to - from) * rowPx
                                            }
                                        },
                                    )
                                }
                                .clickable(enabled = dragIndex == null) {
                                    onNavigate(Routes.listsEdit(list.id))
                                }
                                .padding(horizontal = AivoraDimens.Space4),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = AppIcons.Menu,
                                contentDescription = "Drag to reorder",
                                tint = AivoraColors.IconSecondary,
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    list.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AivoraColors.Ink,
                                )
                                Text(
                                    "${list.conversationIds.size} chats",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AivoraColors.TextSecondary,
                                )
                            }
                            Icon(
                                painter = painterResource(AivoraIcons.ChevronRight),
                                contentDescription = null,
                                tint = AivoraColors.IconSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                SettingsMutedCard {
                    SettingsRow(
                        SettingsItem(
                            "New list",
                            icon = AppIcons.Menu,
                            onClick = { onNavigate(Routes.SettingsListsNew) },
                        ),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}


@Composable
fun SettingsPrivacyAdvancedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    var protectIp by remember { mutableStateOf(true) }
    var disablePreviews by remember { mutableStateOf(false) }
    var blockUnknown by remember { mutableStateOf(false) }

    fun patch(body: com.aivora.app.data.model.UserPreferencesPatchBody, revert: () -> Unit) {
        if (!useRemote) return
        scope.launch {
            runCatching { api.patchMePreferences(body) }
                .onSuccess {
                    protectIp = it.protectIpCalls
                    disablePreviews = it.disableLinkPreviews
                    blockUnknown = it.blockUnknown
                    com.aivora.app.data.local.CallPrivacyPrefs.setProtectIpCalls(
                        context,
                        it.protectIpCalls,
                    )
                }
                .onFailure {
                    revert()
                    android.widget.Toast.makeText(
                        context,
                        "Could not save",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                }
        }
    }

    LaunchedEffect(useRemote) {
        protectIp = com.aivora.app.data.local.CallPrivacyPrefs.protectIpCalls()
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePreferences() }
            .onSuccess {
                protectIp = it.protectIpCalls
                disablePreviews = it.disableLinkPreviews
                blockUnknown = it.blockUnknown
                com.aivora.app.data.local.CallPrivacyPrefs.setProtectIpCalls(
                    context,
                    it.protectIpCalls,
                )
            }
    }

    AivoraScaffold(title = "Advanced", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsToggleBlock(
            title = "Protect IP address in calls",
            checked = protectIp,
            onCheckedChange = {
                val prev = protectIp
                protectIp = it
                com.aivora.app.data.local.CallPrivacyPrefs.setProtectIpCalls(context, it)
                patch(
                    com.aivora.app.data.model.UserPreferencesPatchBody(protectIpCalls = it),
                ) {
                    protectIp = prev
                    com.aivora.app.data.local.CallPrivacyPrefs.setProtectIpCalls(context, prev)
                }
            },
            footnote = "When on, call media uses TURN relay only so your local IP isn’t shared with the other party. Needs working TURN on the call server.",
        )
        SettingsToggleBlock(
            title = "Disable link previews",
            checked = disablePreviews,
            onCheckedChange = {
                val prev = disablePreviews
                disablePreviews = it
                patch(
                    com.aivora.app.data.model.UserPreferencesPatchBody(disableLinkPreviews = it),
                ) { disablePreviews = prev }
            },
            footnote = "Link previews aren’t generated for links you share when this is enabled.",
        )
        SettingsToggleBlock(
            title = "Block messages from unknown accounts",
            checked = blockUnknown,
            onCheckedChange = {
                val prev = blockUnknown
                blockUnknown = it
                patch(
                    com.aivora.app.data.model.UserPreferencesPatchBody(blockUnknown = it),
                ) { blockUnknown = prev }
            },
            footnote = "When on, people you don’t already have a chat with can’t start a new conversation with you (QR / link / search). Existing chats keep working. You can still message anyone.",
        )
    }
}


@Composable
fun SettingsPrivacyAppLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var unlock by remember {
        mutableStateOf(com.aivora.app.data.local.AppLockPrefs.enabled(context))
    }
    var error by remember { mutableStateOf<String?>(null) }
    val canLock = remember {
        com.aivora.app.ui.security.canUseAppLock(context)
    }
    AivoraScaffold(title = "App lock", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsToggleBlock(
            title = "Unlock with device authentication",
            checked = unlock,
            onCheckedChange = { next ->
                error = null
                if (!next) {
                    unlock = false
                    com.aivora.app.data.local.AppLockPrefs.setEnabled(context, false)
                    return@SettingsToggleBlock
                }
                if (!canLock) {
                    error = "Set a screen lock or enroll biometrics in system settings first."
                    return@SettingsToggleBlock
                }
                com.aivora.app.ui.security.promptUnlock(
                    activity = context as? androidx.fragment.app.FragmentActivity,
                    onSuccess = {
                        unlock = true
                        com.aivora.app.data.local.AppLockPrefs.setEnabled(context, true)
                        com.aivora.app.data.local.AppLockPrefs.setNeedsUnlock(context, false)
                        android.widget.Toast.makeText(
                            context,
                            "App lock on",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    },
                    onError = { error = it },
                )
            },
            footnote = "When enabled, use your device’s fingerprint, face recognition or passcode to open Aivora after it leaves the foreground.",
        )
        if (error != null) {
            Text(
                error!!,
                color = AivoraColors.Danger,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
    }
}


@Composable
fun SettingsPrivacyChatLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var hide by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        runCatching { api.getMeChatLock() }
            .onSuccess {
                hide = it.hideFolder
                enabled = it.enabled
            }
    }
    AivoraScaffold(title = "Chat lock", onBack = onBack) {
        SettingsFootnote(
            "Locked chats are kept separately and opened with a secret code. This protects chosen chats, not the entire app.",
        )
        SettingsMutedCard {
            SettingsRow(
                SettingsItem(
                    "Hide locked chats folder",
                    toggle = hide,
                    onToggle = { next ->
                        hide = next
                        scope.launch {
                            runCatching {
                                api.patchMeChatLock(mapOf("hide_folder" to next))
                            }.onFailure {
                                hide = !next
                                android.widget.Toast.makeText(
                                    context,
                                    "Set a secret code first",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                    },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsGreenActionRow(
                title = if (enabled) "Change secret code" else "Set secret code",
                iconRes = AivoraIcons.Lock,
                onClick = { onNavigate(Routes.SettingsPrivacyChatLockSecretCode) },
            )
        }
        SettingsFootnote(
            "Set a secret code, then lock chats from the chat menu. Open Locked chats from the home list footer.",
        )
    }
}


@Composable
fun SettingsPrivacyChatLockSecretCodeScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        runCatching { api.getMeChatLock() }.onSuccess { enabled = it.enabled }
    }

    AivoraScaffold(title = "Secret code", onBack = onBack) {
        InfoBanner("Choose a 4–8 digit secret code to open locked chats.")
        if (enabled) {
            OutlinedTextField(
                value = current,
                onValueChange = {
                    if (it.length <= 8 && it.all(Char::isDigit)) current = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                label = { Text("Current code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= 8 && it.all(Char::isDigit)) pin = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            label = { Text("New secret code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = {
                if (it.length <= 8 && it.all(Char::isDigit)) confirm = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            label = { Text("Confirm secret code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            singleLine = true,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = if (busy) "…" else "Save",
            onClick = {
                if (!busy) {
                    if (pin.length < 4 || pin != confirm) {
                        android.widget.Toast.makeText(
                            context,
                            "Codes must match (4–8 digits)",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    } else if (enabled && current.length < 4) {
                        android.widget.Toast.makeText(
                            context,
                            "Enter current code",
                            android.widget.Toast.LENGTH_SHORT,
                        ).show()
                    } else {
                        busy = true
                        scope.launch {
                            runCatching {
                                val body = buildMap<String, Any?> {
                                    put("pin", pin)
                                    if (enabled) put("current", current)
                                }
                                api.patchMeChatLock(body)
                            }.onSuccess {
                                android.widget.Toast.makeText(
                                    context,
                                    "Secret code saved",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                                onNavigate(Routes.SettingsPrivacyChatLock)
                            }.onFailure {
                                android.widget.Toast.makeText(
                                    context,
                                    it.message ?: "Could not save",
                                    android.widget.Toast.LENGTH_SHORT,
                                ).show()
                            }
                            busy = false
                        }
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
fun SettingsPrivacyCheckupScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    val useRemote = !com.aivora.app.data.local.SessionStore.accessToken(context).isNullOrBlank()
    val cached = remember { com.aivora.app.data.local.InMemoryPrivacyStore.dto }
    var privacy by remember {
        mutableStateOf(cached ?: com.aivora.app.data.model.PrivacySettingsDto())
    }
    var appLockOn by remember {
        mutableStateOf(com.aivora.app.data.local.AppLockPrefs.enabled(context))
    }
    var resumeTick by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) resumeTick += 1
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    LaunchedEffect(useRemote, resumeTick) {
        appLockOn = com.aivora.app.data.local.AppLockPrefs.enabled(context)
        if (!useRemote) return@LaunchedEffect
        runCatching { api.getMePrivacy() }
            .onSuccess {
                com.aivora.app.data.local.InMemoryPrivacyStore.replace(it)
                privacy = it
            }
    }

    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Personal info",
                subtitle = "Last seen · ${privacyWhoLabel(privacy.lastSeen)} · Photo · ${privacyWhoLabel(privacy.profilePhoto)}",
                icon = AppIcons.Person,
                onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) },
            ),
            SettingsItem(
                "Status privacy",
                trailing = privacyWhoLabel(privacy.status),
                iconRes = AivoraIcons.Chat,
                onClick = { onNavigate(Routes.SettingsPrivacyStatus) },
            ),
            SettingsItem(
                "Groups and calls",
                subtitle = "Groups · ${privacyWhoLabel(privacy.groups)}" +
                    if (privacy.silenceUnknownCallers) " · Unknown callers silenced" else "",
                iconRes = AivoraIcons.Group,
                onClick = { onNavigate(Routes.SettingsPrivacyGroups) },
            ),
            SettingsItem(
                "App lock",
                trailing = if (appLockOn) "On" else "Off",
                iconRes = AivoraIcons.Lock,
                onClick = { onNavigate(Routes.SettingsPrivacyAppLock) },
            ),
            SettingsItem(
                "Read receipts",
                trailing = if (privacy.readReceipts) "On" else "Off",
                onClick = { onNavigate(Routes.SettingsPrivacy) },
            ),
            SettingsItem(
                "Who can find me via link / QR",
                trailing = privacyWhoLabel(privacy.links),
                onClick = { onNavigate(Routes.SettingsPrivacyLinks) },
            ),
        ),
    )

    AivoraScaffold(title = "Privacy checkup", onBack = onBack) {
        InfoBanner("Live summary of your privacy choices. Opening a row doesn’t change settings until you edit them.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyLiveLocationScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember {
        com.aivora.app.data.api.NetworkModule.api(
            com.aivora.app.data.api.NetworkModule.okHttp {
                com.aivora.app.data.local.SessionStore.accessToken(context)
            },
        )
    }
    var items by remember {
        mutableStateOf<List<com.aivora.app.data.model.LiveLocationShareDto>>(emptyList())
    }
    var loading by remember { mutableStateOf(true) }
    var stopping by remember { mutableStateOf(false) }

    fun reload() {
        scope.launch {
            loading = true
            runCatching { api.getMeLiveLocations() }
                .onSuccess { items = it.items }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    AivoraScaffold(title = "Live location", onBack = onBack) {
        InfoBanner(
            if (items.isEmpty()) "You’re not sharing live location right now."
            else "Active shares update while the app is open.",
        )
        when {
            loading -> Text(
                "Loading…",
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(24.dp),
            )
            else -> SettingsList(
                listOf(
                    null to buildList {
                        add(
                            SettingsItem(
                                "Stop all live locations",
                                subtitle = if (items.isEmpty()) "None active" else "${items.size} active",
                                icon = AppIcons.LocationOn,
                                onClick = {
                                    if (!stopping && items.isNotEmpty()) {
                                        stopping = true
                                        scope.launch {
                                            runCatching { api.postMeLiveLocationsStop() }
                                                .onSuccess {
                                                    com.aivora.app.data.LiveLocationSession.stop()
                                                    items = emptyList()
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        "Live location ended",
                                                        android.widget.Toast.LENGTH_SHORT,
                                                    ).show()
                                                }
                                                .onFailure {
                                                    android.widget.Toast.makeText(
                                                        context,
                                                        it.message ?: "Could not stop",
                                                        android.widget.Toast.LENGTH_SHORT,
                                                    ).show()
                                                }
                                            stopping = false
                                        }
                                    }
                                },
                            ),
                        )
                        items.forEach { share ->
                            val mid = share.messageId
                            if (mid != null) {
                                add(
                                    SettingsItem(
                                        "Live · ${share.conversationId?.takeLast(6) ?: "chat"}",
                                        subtitle = share.expiresAt?.let { "Until $it" } ?: "Active",
                                        icon = AppIcons.ShareLocation,
                                        onClick = {
                                            scope.launch {
                                                runCatching {
                                                    api.postMeLiveLocationsStop(mapOf("message_id" to mid))
                                                }.onSuccess {
                                                    com.aivora.app.data.LiveLocationSession.stop(mid)
                                                    reload()
                                                }
                                            }
                                        },
                                    ),
                                )
                            }
                        }
                    },
                ),
            )
        }
    }
}

@Composable
fun SettingsAppearanceScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeMode by com.aivora.app.data.local.AppearancePrefs.themeFlow.collectAsState()
    val defaultWp by com.aivora.app.data.local.AppearancePrefs.wallpaperFlow.collectAsState()
    val fontScale by com.aivora.app.data.local.FontScaleStore.scaleFlow.collectAsState()
    var pick by remember { mutableStateOf<String?>(null) }

    if (pick == "theme") {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pick = null },
            title = { Text("Theme") },
            text = {
                Column {
                    listOf(
                        com.aivora.app.data.local.AppearancePrefs.THEME_SYSTEM to "System default",
                        com.aivora.app.data.local.AppearancePrefs.THEME_LIGHT to "Light",
                        com.aivora.app.data.local.AppearancePrefs.THEME_DARK to "Dark",
                    ).forEach { (key, label) ->
                        Text(
                            label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.aivora.app.data.local.AppearancePrefs.setTheme(context, key)
                                    pick = null
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (key == themeMode) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { pick = null }) { Text("Cancel") }
            },
        )
    }

    if (pick == "wallpaper") {
        com.aivora.app.ui.feature.chat.ChatWallpaperDialog(
            selected = defaultWp,
            onPick = {
                com.aivora.app.data.local.AppearancePrefs.setWallpaper(context, it)
                pick = null
            },
            onDismiss = { pick = null },
        )
    }

    AivoraScaffold(title = "Appearance", onBack = onBack) {
        SettingsList(
            listOf(
        null to listOf(
                    SettingsItem(
                        "Theme",
                        trailing = com.aivora.app.data.local.AppearancePrefs.themeLabel(themeMode),
                        onClick = { pick = "theme" },
                    ),
                ),
                "Chat display" to listOf(
                    SettingsItem(
                        "Default chat theme",
                        subtitle = com.aivora.app.data.local.AppearancePrefs.wallpaperLabel(defaultWp),
                        onClick = { pick = "wallpaper" },
                    ),
                    SettingsItem(
                        "Chat font size",
                        trailing = com.aivora.app.data.local.FontScaleStore.label(fontScale),
                        onClick = { onNavigate(Routes.SettingsChatsFontSize) },
                    ),
                ),
            ),
            modifier = Modifier.weight(1f),
        )
        Text(
            "Default wallpaper applies to chats without their own theme.",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AivoraColors.TextSecondary,
        )
        LinkButton(
            text = "Reset appearance",
            onClick = {
                com.aivora.app.data.local.AppearancePrefs.reset(context)
                android.widget.Toast.makeText(
                    context,
                    "Appearance reset",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsNotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs by com.aivora.app.data.local.NotificationPrefs.flow.collectAsState()
    var pickVibrate by remember { mutableStateOf<String?>(null) }

    var toneKind by remember { mutableStateOf("msg") }
    val pickTone = rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        @Suppress("DEPRECATION")
        val uri = result.data?.getParcelableExtra<android.net.Uri>(
            android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI,
        )
        if (toneKind == "call") {
            com.aivora.app.data.local.NotificationPrefs.setCallSoundUri(context, uri?.toString())
            com.aivora.app.data.push.MessageNotifier.recreateCallsChannel(context)
        } else {
            com.aivora.app.data.local.NotificationPrefs.setMsgSoundUri(context, uri?.toString())
            com.aivora.app.data.push.MessageNotifier.recreateMessagesChannel(context)
        }
    }

    if (pickVibrate != null) {
        val options = listOf(
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_DEFAULT,
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_LONG,
            com.aivora.app.data.local.NotificationPrefs.VIBRATE_OFF,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pickVibrate = null },
            title = { Text("Vibrate") },
            text = {
                Column {
                    options.forEach { key ->
                        Text(
                            com.aivora.app.data.local.NotificationPrefs.vibrateLabel(key),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (pickVibrate) {
                                        "msg" -> com.aivora.app.data.local.NotificationPrefs.setMsgVibrate(context, key)
                                        "group" -> com.aivora.app.data.local.NotificationPrefs.setGroupVibrate(context, key)
                                        "call" -> com.aivora.app.data.local.NotificationPrefs.setCallVibrate(context, key)
                                    }
                                    pickVibrate = null
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { pickVibrate = null }) { Text("Cancel") }
            },
        )
    }

    AivoraScaffold(title = "Notifications", onBack = onBack) {
        SettingsList(
            listOf(
                "Conversation tones" to listOf(
                    SettingsItem(
                        "Conversation tones",
                        subtitle = "Play sound for incoming and outgoing messages",
                        toggle = prefs.conversationTones,
                        onToggle = {
                            com.aivora.app.data.local.NotificationPrefs.setConversationTones(context, it)
                        },
                    ),
                ),
                "Messages" to listOf(
                    SettingsItem(
                        "Notification tone",
                        trailing = com.aivora.app.data.local.NotificationPrefs.soundLabel(
                            context,
                            prefs.msgSoundUri,
                        ),
                        onClick = {
                            toneKind = "msg"
                            val intent = android.content.Intent(
                                android.media.RingtoneManager.ACTION_RINGTONE_PICKER,
                            ).apply {
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    android.media.RingtoneManager.TYPE_NOTIFICATION,
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_TITLE,
                                    "Notification tone",
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                                    true,
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                                    false,
                                )
                                val existing = prefs.msgSoundUri
                                    ?.let { runCatching { android.net.Uri.parse(it) }.getOrNull() }
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    existing,
                                )
                            }
                            pickTone.launch(intent)
                        },
                    ),
                    SettingsItem(
                        "Vibrate",
                        trailing = com.aivora.app.data.local.NotificationPrefs.vibrateLabel(prefs.msgVibrate),
                        onClick = { pickVibrate = "msg" },
                    ),
                    SettingsItem(
                        "High priority notifications",
                        subtitle = "Show previews at the top of the screen",
                        toggle = prefs.highPriority,
                        onToggle = {
                            com.aivora.app.data.local.NotificationPrefs.setHighPriority(context, it)
                        },
                    ),
                    SettingsItem(
                        "Reaction notifications",
                        subtitle = "Show notifications when someone reacts",
                        toggle = prefs.reactions,
                        onToggle = {
                            com.aivora.app.data.local.NotificationPrefs.setReactions(context, it)
                        },
                    ),
                    SettingsItem(
                        "Show message preview",
                        subtitle = "Display message text in the notification",
                        toggle = prefs.showPreview,
                        onToggle = {
                            com.aivora.app.data.local.NotificationPrefs.setShowPreview(context, it)
                        },
                    ),
                ),
                "Groups" to listOf(
                    SettingsItem(
                        "Vibrate",
                        trailing = com.aivora.app.data.local.NotificationPrefs.vibrateLabel(prefs.groupVibrate),
                        onClick = { pickVibrate = "group" },
                    ),
                ),
                "Calls" to listOf(
                    SettingsItem(
                        "Ringtone",
                        trailing = com.aivora.app.data.local.NotificationPrefs.soundLabel(
                            context,
                            prefs.callSoundUri,
                        ),
                        onClick = {
                            toneKind = "call"
                            val intent = android.content.Intent(
                                android.media.RingtoneManager.ACTION_RINGTONE_PICKER,
                            ).apply {
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_TYPE,
                                    android.media.RingtoneManager.TYPE_RINGTONE,
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_TITLE,
                                    "Call ringtone",
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                                    true,
                                )
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
                                    false,
                                )
                                val existing = prefs.callSoundUri
                                    ?.let { runCatching { android.net.Uri.parse(it) }.getOrNull() }
                                putExtra(
                                    android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                    existing,
                                )
                            }
                            pickTone.launch(intent)
                        },
                    ),
                    SettingsItem(
                        "Vibrate",
                        trailing = com.aivora.app.data.local.NotificationPrefs.vibrateLabel(prefs.callVibrate),
                        onClick = { pickVibrate = "call" },
                    ),
                ),
                null to listOf(
                    SettingsItem("Permissions", onClick = { onNavigate(Routes.SettingsPermissions) }),
                ),
            ),
            modifier = Modifier.weight(1f),
        )
        LinkButton(
            text = "Reset notification settings",
            onClick = {
                com.aivora.app.data.local.NotificationPrefs.reset(context)
                com.aivora.app.data.push.MessageNotifier.recreateMessagesChannel(context)
                com.aivora.app.data.push.MessageNotifier.recreateCallsChannel(context)
                android.widget.Toast.makeText(
                    context,
                    "Notification settings reset",
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
