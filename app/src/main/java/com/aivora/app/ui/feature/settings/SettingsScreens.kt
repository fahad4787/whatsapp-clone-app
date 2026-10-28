package com.aivora.app.ui.feature.settings

import com.aivora.app.ui.icons.AppIcons

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var notify by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Edit profile", "Name, about, avatar, username", AppIcons.Person, onClick = { onNavigate(Routes.YourDetails) }),
            SettingsItem("Account", "Security, email, passkeys, number", AppIcons.AccountCircle, onClick = { onNavigate(Routes.SettingsAccount) }),
            SettingsItem("Privacy", "Last seen, profile photo, calls, locks", iconRes = AivoraIcons.Lock, onClick = { onNavigate(Routes.SettingsPrivacy) }),
            SettingsItem("Chats", "Theme, backup, history, transfer", iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChats) }),
            SettingsItem("Lists", "Filter chats into custom lists", AppIcons.Menu, onClick = { onNavigate(Routes.SettingsListsNew) }),
            SettingsItem("Reorder lists", "Change list order", AppIcons.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
            SettingsItem("Notifications", "Message and call alerts", iconRes = AivoraIcons.Notifications, toggle = notify, onToggle = { notify = it }),
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
    val sections = listOf(
        "Security" to listOf(
            SettingsItem("Security notifications", onClick = { onNavigate(Routes.SettingsAccountSecurityNotifications) }),
            SettingsItem("Passkeys", onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email address", trailing = "Not added", onClick = { onNavigate(Routes.SettingsAccountEmail) }),
            SettingsItem("Two-step verification", trailing = "Off", onClick = { onNavigate(Routes.SettingsTwoStep) }),
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
    var enterIsSend by remember { mutableStateOf(false) }
    var mediaVisibility by remember { mutableStateOf(true) }
    val sections = listOf(
        "Display" to listOf(
            SettingsItem(
                "Theme",
                subtitle = "Light",
                onClick = { onNavigate(Routes.SettingsAppearance) },
            ),
            SettingsItem(
                "Default chat theme",
                subtitle = "Default · Green",
                onClick = { onNavigate(Routes.SettingsAppearance) },
            ),
            SettingsItem(
                "Chat font size",
                trailing = "Medium",
                onClick = { onNavigate(Routes.SettingsChatsFontSize) },
            ),
        ),
        "Chat settings" to listOf(
            SettingsItem(
                "Enter is send",
                subtitle = "Enter key sends your message",
                toggle = enterIsSend,
                onToggle = { enterIsSend = it },
            ),
            SettingsItem(
                "Media visibility",
                subtitle = "Show newly downloaded media in your phone’s gallery",
                toggle = mediaVisibility,
                onToggle = { mediaVisibility = it },
            ),
            SettingsItem("Keep chats archived", subtitle = "Archived chats stay archived when new messages arrive", onClick = { onNavigate(Routes.SettingsChatsArchived) }),
        ),
        "Chat history" to listOf(
            SettingsItem("Chat backup", onClick = { onNavigate(Routes.SettingsChatsBackup) }),
            SettingsItem("Transfer chats", onClick = { onNavigate(Routes.SettingsChatsTransfer) }),
            SettingsItem("Chat history", onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Export chat", onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Clear all chats", danger = true, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Delete all chats", danger = true, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
        ),
    )
    AivoraScaffold(title = "Chats", onBack = onBack) {
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var readReceipts by remember { mutableStateOf(true) }

    val sections = listOf(
        "Who can…" to listOf(
            SettingsItem("Last seen and online", trailing = "Everyone", onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Profile photo", trailing = "Everyone", onClick = { onNavigate(Routes.SettingsPrivacyProfilePhoto) }),
            SettingsItem("About", trailing = "Everyone", onClick = { onNavigate(Routes.SettingsPrivacyAbout) }),
            SettingsItem("Links", trailing = "My contacts", onClick = { }),
            SettingsItem("Status", trailing = "My contacts", onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
            SettingsItem(
                "Read receipts",
                subtitle = "If turned off, you won’t send or receive read receipts",
                toggle = readReceipts,
                onToggle = { readReceipts = it },
            ),
        ),
        null to listOf(
            SettingsItem("Disappearing messages", trailing = "Off", onClick = { onNavigate(Routes.SettingsPrivacyDefaultTimer) }),
            SettingsItem("Groups", trailing = "Everyone", onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
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
    var selected by remember { mutableIntStateOf(2) }
    AivoraScaffold(title = "Profile photo", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my profile photo")
        RadioGroup(
            options = listOf(
                "Everyone" to null,
                "My contacts" to null,
                "My contacts except…" to "1 excluded",
                "Nobody" to null,
            ),
            selected = selected,
            onSelect = { selected = it },
            navigableIndices = setOf(2),
        )
        SettingsFootnote("Your profile photo is visible to your contacts, except the people you exclude.")
    }
}


@Composable
fun SettingsPrivacyAboutScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "About", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsSectionLabel("Who can see my about info")
        RadioGroup(
            options = listOf(
                "Everyone" to null,
                "My contacts" to null,
                "My contacts except…" to null,
                "Nobody" to null,
            ),
            selected = selected,
            onSelect = { selected = it },
            navigableIndices = setOf(2),
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
    var selected by remember { mutableIntStateOf(0) }
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
            onSelect = { selected = it },
            navigableIndices = setOf(2),
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
    var silence by remember { mutableStateOf(true) }
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
                    onToggle = { silence = it },
                ),
            )
        }
        SettingsFootnote(
            "Calls from unknown numbers won’t ring, but will still appear in your Calls list and notifications.\n\nSilencing a caller doesn’t block them from calling or messaging you.",
        )
    }
}


@Composable
fun SettingsPrivacyDefaultTimerScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(2) }
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
            onSelect = { selected = it },
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

    var showPhoto by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("My QR code", "Show my name and photo on my QR page", iconRes = AivoraIcons.Qr, onClick = { }),
            SettingsItem("Scan QR code", null, iconRes = AivoraIcons.Camera, onClick = { }),
            SettingsItem("Show name and photo on QR page", null, AppIcons.Visibility, toggle = showPhoto, onToggle = { showPhoto = it }),
        ),
    )

    AivoraScaffold(title = "QR code", onBack = onBack) {
        InfoBanner("Anyone who opens your QR link can see them, even without the app.")
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


@Composable
fun SettingsStorageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var lessData by remember { mutableStateOf(false) }
    val sections = listOf(
        null to listOf(
            SettingsItem(
                "Manage storage",
                subtitle = "1.2 GB",
                onClick = { onNavigate(Routes.SettingsManageStorage) },
            ),
        ),
        "Network" to listOf(
            SettingsItem("Network usage", subtitle = "0 MB sent · 0 MB received", onClick = { }),
            SettingsItem(
                "Use less data for calls",
                toggle = lessData,
                onToggle = { lessData = it },
            ),
            SettingsItem("Proxy", trailing = "Off", onClick = { }),
        ),
        "Media auto-download" to listOf(
            SettingsItem("When using mobile data", trailing = "Photos", onClick = { }),
            SettingsItem("When connected on Wi-Fi", trailing = "Photos", onClick = { }),
            SettingsItem("When roaming", trailing = "No media", onClick = { }),
        ),
        "Media upload quality" to listOf(
            SettingsItem("Photo upload quality", trailing = "Auto (recommended)", onClick = { }),
        ),
    )

    AivoraScaffold(title = "Storage and data", onBack = onBack) {
        SettingsList(sections)
    }
}


@Composable
fun SettingsManageStorageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val chats = listOf(
        "George Alan" to "400 MB",
        "Weekend walkers" to "280 MB",
        "Maya Chen" to "190 MB",
        "Family" to "120 MB",
    )
    AivoraScaffold(title = "Manage storage", onBack = onBack) {
        AivoraNoOverscroll {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            "1.2 GB",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "10.8 GB free of 64 GB",
                            style = MaterialTheme.typography.bodySmall,
                            color = AivoraColors.TextSecondary,
                        )
                    }
                    Text(
                        "Used by Aivora",
                        style = MaterialTheme.typography.bodySmall,
                        color = AivoraColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                    ) {
                        Box(Modifier.weight(1.2f).fillMaxSize().background(AivoraColors.Primary))
                        Box(Modifier.weight(5.2f).fillMaxSize().background(AivoraColors.TextTertiary))
                        Box(Modifier.weight(1.08f).fillMaxSize().background(AivoraColors.Divider))
                    }
                    Spacer(Modifier.height(12.dp))
                    StorageLegendRow(AivoraColors.Primary, "Aivora", "1.2 GB")
                    StorageLegendRow(AivoraColors.TextTertiary, "Other apps and files", "52 GB")
                    StorageLegendRow(AivoraColors.Divider, "Free", "10.8 GB")
                }
            }
            item {
                Text(
                    "Review and delete",
                    style = MaterialTheme.typography.labelLarge,
                    color = AivoraColors.TextSecondary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                SettingsMutedCard {
                    SettingsRow(
                        SettingsItem(
                            "Larger than 5 MB",
                            subtitle = "200 MB · 6 items",
                            icon = AppIcons.Folder,
                            onClick = { },
                        ),
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
                    SettingsRow(
                        SettingsItem(
                            "Forwarded many times",
                            subtitle = "72 MB · 3 items",
                            icon = AppIcons.Reply,
                            onClick = { },
                        ),
                    )
                }
            }
            item {
                SearchField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Search chats or media",
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
            items(
                items = chats.filter { it.first.contains(query, ignoreCase = true) },
                key = { it.first },
            ) { (name, size) ->
                PersonRow(
                    name = name,
                    onClick = { },
                    trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(size, color = AivoraColors.TextSecondary, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(AivoraIcons.ChevronRight),
                                contentDescription = null,
                                tint = AivoraColors.IconSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
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
    AivoraScaffold(title = "Two-step verification", onBack = onBack) {
        SettingsHeroIntro(
            iconRes = AivoraIcons.Lock,
            title = "Two-step verification is enabled",
            body = "Your six-digit account PIN adds a verification step when you register your phone number again.",
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem(
                        "Recovery email",
                        subtitle = "leslie@example.com",
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
                        "Change email",
                        icon = AppIcons.Email,
                        onClick = { onNavigate(Routes.SettingsAccountEmail) },
                    ),
                    SettingsItem(
                        "Disable",
                        iconRes = AivoraIcons.Lock,
                        danger = true,
                        onClick = { },
                    ),
                ),
            ),
        )
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
    AivoraScaffold(title = "Linked devices", onBack = onBack) {
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem(
                        "Link a device",
                        "Scan the code on Aivora Web",
                        iconRes = AivoraIcons.Qr,
                        onClick = { onNavigate(Routes.SettingsDevicesLink) },
                    ),
                ),
            ),
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("V2440", "android · last active now · this device", iconRes = AivoraIcons.Call, onClick = { }),
                    SettingsItem("Chrome on macOS", "web · last active Yesterday", AppIcons.Devices, trailing = "Sign out", onClick = { }),
                    SettingsItem("V2440", "android · last active Mon", iconRes = AivoraIcons.Call, trailing = "Sign out", onClick = { }),
                ),
            ),
        )
    }
}


@Composable
fun SettingsDevicesLinkScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Scan QR code", "Point camera at the code", iconRes = AivoraIcons.Qr, onClick = { }),
            SettingsItem("Link with phone number", null, iconRes = AivoraIcons.Call, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Link a device", onBack = onBack) {
        InfoBanner("Camera access lets this phone scan the QR code your new phone shows.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPermissionsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    var cam by remember { mutableStateOf(true) }
    var mic by remember { mutableStateOf(true) }
    var contacts by remember { mutableStateOf(true) }
    var loc by remember { mutableStateOf(false) }
    var notif by remember { mutableStateOf(true) }
    var photos by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Camera", "Allow camera", AppIcons.Photo, toggle = cam, onToggle = { cam = it }),
            SettingsItem("Microphone", "Allow the microphone", iconRes = AivoraIcons.Mic, toggle = mic, onToggle = { mic = it }),
            SettingsItem("Contacts", "Allow contacts", AppIcons.Person, toggle = contacts, onToggle = { contacts = it }),
            SettingsItem("Location", "Allow location access?", AppIcons.LocationOn, toggle = loc, onToggle = { loc = it }),
            SettingsItem("Notifications", "Message and call alerts", iconRes = AivoraIcons.Notifications, toggle = notif, onToggle = { notif = it }),
            SettingsItem("Photos and videos", "Allow access to Photos", AppIcons.Photo, toggle = photos, onToggle = { photos = it }),
        ),
    )

    AivoraScaffold(title = "Permissions", onBack = onBack) {
        InfoBanner("Control access for Aivora.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsCommunitiesScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Create community", null, iconRes = AivoraIcons.Group, onClick = { onNavigate(Routes.NewGroup) }),
            SettingsItem("Your communities", "Everyone in the community", iconRes = AivoraIcons.Group, onClick = { onNavigate(Routes.CommunityId.replace("{id}", "1")) }),
        ),
    )

    AivoraScaffold(title = "Communities", onBack = onBack) {
        InfoBanner("A community keeps related groups together with one announcement group.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsDeleteAccountScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var phone by remember { mutableStateOf("") }
    val canDelete = phone.length >= 8
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
                SettingsRow(
                    SettingsItem(
                        "India",
                        subtitle = "Country / region",
                        onClick = { },
                    ),
                )
                HorizontalDivider(color = AivoraColors.Divider)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("+91", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter { c -> c.isDigit() } },
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
            text = "Delete my account",
            onClick = { onNavigate(Routes.SettingsAccountDeleted) },
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Continue with phone number", null, iconRes = AivoraIcons.Call, onClick = { onNavigate(Routes.Login) }),
            SettingsItem("Sign in with a passkey", null, AppIcons.Fingerprint, onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email sign-in", null, AppIcons.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
        ),
    )

    AivoraScaffold(title = "Add account", onBack = onBack) {
        InfoBanner("Add a second account")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountChangeNumberScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Change number", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.Sync,
            title = "Move to a new phone number",
            body = "Changing your number moves your account information, profile, groups and settings to your new number.\n\nBefore you continue, make sure your new number can receive SMS messages or calls.\n\nYour old number must be the one currently registered with Aivora.",
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "Next",
            onClick = { onNavigate(Routes.Login) },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
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
    var email by remember { mutableStateOf("leslie@example.com") }
    AivoraScaffold(title = "Email address", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Add your email address",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "An email address can help you verify access to your account and recover it. It won’t be shown to your contacts.",
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
                onValueChange = { email = it },
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
            Spacer(Modifier.height(8.dp))
            Text(
                "We’ll ask you to verify this address.",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
            )
        }
        PrimaryButton(
            text = "Send verification",
            onClick = { },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
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
            title = "Sign in with a passkey",
            body = "Use your face, fingerprint or device passcode to verify it’s you when signing in to Aivora.\n\nCreate a passkey using your device’s authentication. Other sign-in verification methods remain available.",
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "Create passkey",
            onClick = { },
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}


@Composable
fun SettingsAccountRequestInfoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Request report", "Account information report", AppIcons.Info, onClick = { }),
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
    var show by remember { mutableStateOf(true) }
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
                    onToggle = { show = it },
                ),
            )
        }
        SettingsFootnote(
            "Get notified when your security code changes for a contact’s phone. If you have multiple devices, turn this on for each one.",
        )
    }
}


@Composable
fun SettingsAccountTwoStepNewPinScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Save PIN", "This account has two-step verification on.", iconRes = AivoraIcons.Key, onClick = { onNavigate(Routes.SettingsTwoStep) }),
        ),
    )

    AivoraScaffold(title = "New PIN", onBack = onBack) {
        InfoBanner("Enter a 6-digit PIN for two-step verification.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsArchivedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    var keep by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Keep chats archived", "New messages stay archived.", iconRes = AivoraIcons.Chat, toggle = keep, onToggle = { keep = it }),
            SettingsItem("Archive all chats", null, iconRes = AivoraIcons.Chat, onClick = { }),
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
    var videos by remember { mutableStateOf(false) }
    var wifiOnly by remember { mutableStateOf(true) }
    AivoraScaffold(title = "Chat backup", onBack = onBack) {
        SettingsHeroIntro(
            icon = AppIcons.CloudUpload,
            title = "Chat backup",
            body = "Back up your chats and media to your cloud account so you can restore them on a new phone.",
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
                    subtitle = "9 October 2026 at 8:00 AM",
                ),
            )
            HorizontalDivider(
                modifier = Modifier.padding(start = 16.dp),
                color = AivoraColors.Divider,
            )
            SettingsRow(SettingsItem("Total size", trailing = "1.2 GB"))
        }
        PrimaryButton(
            text = "Back up",
            onClick = { },
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
                    "Backup account",
                    subtitle = "leslie@example.com",
                    onClick = { onNavigate(Routes.SettingsChatsBackupLocation) },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Automatic backups",
                    trailing = "Daily",
                    onClick = { onNavigate(Routes.SettingsChatsBackupFrequency) },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Include videos",
                    toggle = videos,
                    onToggle = { videos = it },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Wi-Fi only",
                    toggle = wifiOnly,
                    onToggle = { wifiOnly = it },
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
                    "End-to-end encrypted backup",
                    subtitle = "Off",
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Create password", "Backup password", iconRes = AivoraIcons.Lock, onClick = { }),
            SettingsItem("Turn on encrypted backup", null, AppIcons.Security, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Encrypted backup", onBack = onBack) {
        InfoBanner("This protects only your backup file. It is not your two-step verification PIN.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupFrequencyScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Daily", null, AppIcons.Timer, onClick = { }),
            SettingsItem("Weekly", null, AppIcons.Timer, onClick = { }),
            SettingsItem("Monthly", null, AppIcons.Timer, onClick = { }),
            SettingsItem("Off", null, AppIcons.Timer, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Backup frequency", onBack = onBack) {
        InfoBanner("Automatic backups")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupLocationScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Internal storage", null, iconRes = AivoraIcons.Storage, onClick = { }),
            SettingsItem("Memory card or USB storage", null, iconRes = AivoraIcons.Storage, onClick = { }),
            SettingsItem("Another app's storage", null, AppIcons.Folder, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Backup location", onBack = onBack) {
        InfoBanner("Choose where to save backups.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupOpenScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Restore from backup", "Aivora backup.zip", AppIcons.Folder, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Open backup", onBack = onBack) {
        InfoBanner("Restore chats from a backup file.")
        SettingsList(sections)
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Small", null, iconRes = AivoraIcons.Chat, onClick = { }),
            SettingsItem("Medium", null, iconRes = AivoraIcons.Chat, selected = true, onClick = { }),
            SettingsItem("Large", null, iconRes = AivoraIcons.Chat, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Font size", onBack = onBack) {
        InfoBanner("Message text size")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsHistoryScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Export chat", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Archive all chats", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Clear all chats", null, AppIcons.Delete, onClick = { }),
            SettingsItem("Delete all chats", null, AppIcons.Delete, danger = true, onClick = { }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Generate transcripts", null, iconRes = AivoraIcons.Chat, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Chat transcripts", onBack = onBack) {
        InfoBanner("Export transcripts for selected chats.")
        SettingsList(sections)
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
            body = "Transfer your chats and media locally to a new phone using the same phone number.",
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
                    "Keep both phones nearby",
                    subtitle = "Leave both phones unlocked during the transfer",
                    icon = AppIcons.PhoneForwarded,
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 54.dp), color = AivoraColors.Divider)
            SettingsRow(
                SettingsItem(
                    "Make sure both have enough power",
                    subtitle = "Connect them to power if needed",
                    icon = AppIcons.Info,
                ),
            )
        }
        SettingsFootnote("This is a local transfer, not a cloud backup or an account merge.")
        Spacer(Modifier.weight(1f))
        PrimaryButton(
            text = "Start",
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

    val sections = listOf(
        null to listOf(
            SettingsItem("List name", "Favourites, Work, Family…", AppIcons.Menu, onClick = { }),
            SettingsItem("Add people or groups", null, iconRes = AivoraIcons.Group, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Reorder lists", null, AppIcons.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
        ),
    )

    AivoraScaffold(title = "New list", onBack = onBack) {
        InfoBanner("A list can hold 500 chats at most. Add at least one chat to save your list.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsListsEditIdScreen(
    listId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Rename list", null, iconRes = AivoraIcons.Edit, onClick = { }),
            SettingsItem("Add chats", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Delete list", "Deleting this list does not delete any chats.", AppIcons.Delete, danger = true, onClick = { }),
        ),
    )

    AivoraScaffold(title = if (listId.isBlank()) "Edit list" else "Edit list · $listId", onBack = onBack) {
        InfoBanner("Customize which chats belong to this list.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsListsReorderScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Favourites", null, iconRes = AivoraIcons.Star, onClick = { }),
            SettingsItem("Unread", null, iconRes = AivoraIcons.Chat, onClick = { }),
            SettingsItem("Groups", null, iconRes = AivoraIcons.Group, onClick = { }),
            SettingsItem("Custom lists aren't available yet.", null, AppIcons.Menu, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Reorder lists", onBack = onBack) {
        InfoBanner("Default lists")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyAdvancedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var protectIp by remember { mutableStateOf(true) }
    var disablePreviews by remember { mutableStateOf(true) }
    var blockUnknown by remember { mutableStateOf(true) }
    AivoraScaffold(title = "Advanced", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsToggleBlock(
            title = "Protect IP address in calls",
            checked = protectIp,
            onCheckedChange = { protectIp = it },
            footnote = "Calls are routed through Aivora to hide your IP address from the other person. This may reduce call quality.",
        )
        SettingsToggleBlock(
            title = "Disable link previews",
            checked = disablePreviews,
            onCheckedChange = { disablePreviews = it },
            footnote = "Link previews aren’t generated for links you share when this is enabled.",
        )
        SettingsToggleBlock(
            title = "Block messages from unknown accounts",
            checked = blockUnknown,
            onCheckedChange = { blockUnknown = it },
            footnote = "Limits messages from unknown accounts sending unusually high volumes. It doesn’t block all messages from non-contacts.",
        )
    }
}


@Composable
fun SettingsPrivacyAppLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var unlock by remember { mutableStateOf(false) }
    AivoraScaffold(title = "App lock", onBack = onBack) {
        Spacer(Modifier.height(8.dp))
        SettingsToggleBlock(
            title = "Unlock with device authentication",
            checked = unlock,
            onCheckedChange = { unlock = it },
            footnote = "When enabled, use your device’s fingerprint, face recognition or passcode to open Aivora.",
        )
    }
}


@Composable
fun SettingsPrivacyChatLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var hide by remember { mutableStateOf(false) }
    AivoraScaffold(title = "Chat lock", onBack = onBack) {
        SettingsFootnote(
            "Locked chats are kept separately and opened with your device’s fingerprint, face recognition or passcode. This protects chosen chats, not the entire app.",
        )
        SettingsMutedCard {
            SettingsRow(
                SettingsItem(
                    "Hide locked chats",
                    toggle = hide,
                    onToggle = { hide = it },
                ),
            )
            HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = AivoraColors.Divider)
            SettingsGreenActionRow(
                title = "Set secret code",
                iconRes = AivoraIcons.Lock,
                onClick = { onNavigate(Routes.SettingsPrivacyChatLockSecretCode) },
            )
        }
        SettingsFootnote(
            "Set a secret code to hide the Locked chats folder and find it by entering your code in search.",
        )
    }
}


@Composable
fun SettingsPrivacyChatLockSecretCodeScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Enter secret code", null, iconRes = AivoraIcons.Key, onClick = { }),
            SettingsItem("Confirm secret code", null, iconRes = AivoraIcons.Key, onClick = { }),
            SettingsItem("Save", null, AppIcons.Check, onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
        ),
    )

    AivoraScaffold(title = "Secret code", onBack = onBack) {
        InfoBanner("Choose a secret code to open locked chats.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyCheckupScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Personal info", "Last seen, photo, about", AppIcons.Person, onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Status privacy", null, iconRes = AivoraIcons.Chat, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
            SettingsItem("Groups and calls", null, iconRes = AivoraIcons.Group, onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
            SettingsItem("App lock", null, iconRes = AivoraIcons.Lock, onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
        ),
    )

    AivoraScaffold(title = "Privacy checkup", onBack = onBack) {
        InfoBanner("Done returns to Privacy. Completing this review doesn’t change your settings.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyLiveLocationScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Stop all live locations", "Live location ended", AppIcons.LocationOn, onClick = { }),
            SettingsItem("Live location · from another device", null, AppIcons.Devices, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Live location", onBack = onBack) {
        InfoBanner("Live location sharing stopped")
        SettingsList(sections)
    }
}

@Composable
fun SettingsAppearanceScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Appearance", onBack = onBack) {
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Theme", trailing = "Light", onClick = { }),
                ),
                "Chat display" to listOf(
                    SettingsItem(
                        "Default chat theme",
                        subtitle = "Default · Green",
                        onClick = { },
                    ),
                    SettingsItem(
                        "Chat font size",
                        trailing = "Medium",
                        onClick = { onNavigate(Routes.SettingsChatsFontSize) },
                    ),
                ),
            ),
            modifier = Modifier.weight(1f),
        )
        Text(
            "Manage chat display in Chats settings.",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AivoraColors.TextSecondary,
        )
        LinkButton(text = "Reset appearance", onClick = { })
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun SettingsNotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var conversationTones by remember { mutableStateOf(true) }
    var highPriority by remember { mutableStateOf(true) }
    var reactions by remember { mutableStateOf(true) }
    var showPreview by remember { mutableStateOf(true) }

    AivoraScaffold(title = "Notifications", onBack = onBack) {
        SettingsList(
            listOf(
                "Conversation tones" to listOf(
                    SettingsItem(
                        "Conversation tones",
                        subtitle = "Play sound for incoming and outgoing messages",
                        toggle = conversationTones,
                        onToggle = { conversationTones = it },
                    ),
                ),
                "Messages" to listOf(
                    SettingsItem("Notification tone", trailing = "Default", onClick = { }),
                    SettingsItem("Vibrate", trailing = "Default", onClick = { }),
                    SettingsItem(
                        "High priority notifications",
                        subtitle = "Show previews at the top of the screen",
                        toggle = highPriority,
                        onToggle = { highPriority = it },
                    ),
                    SettingsItem(
                        "Reaction notifications",
                        subtitle = "Show notifications when someone reacts",
                        toggle = reactions,
                        onToggle = { reactions = it },
                    ),
                ),
                "Groups" to listOf(
                    SettingsItem("Notification tone", trailing = "Default", onClick = { }),
                    SettingsItem("Vibrate", trailing = "Default", onClick = { }),
                ),
                "Calls" to listOf(
                    SettingsItem("Ringtone", trailing = "Default", onClick = { }),
                    SettingsItem("Vibrate", trailing = "Default", onClick = { }),
                ),
                "Status" to listOf(
                    SettingsItem(
                        "Show previews",
                        toggle = showPreview,
                        onToggle = { showPreview = it },
                    ),
                    SettingsItem("Reactions", trailing = "Default", onClick = { }),
                ),
                null to listOf(
                    SettingsItem("Permissions", onClick = { onNavigate(Routes.SettingsPermissions) }),
                ),
            ),
            modifier = Modifier.weight(1f),
        )
        LinkButton(text = "Reset notification settings", onClick = { })
        Spacer(modifier = Modifier.height(8.dp))
    }
}
