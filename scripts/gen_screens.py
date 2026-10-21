#!/usr/bin/env python3
"""Generate full frontend screens for every route — no stubs."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCREENS = ROOT / "app/src/main/java/com/aivora/app/ui/screens"


def w(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content if content.endswith("\n") else content + "\n")


COMMON_IMPORTS = """
package com.aivora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.EmptyState
import com.aivora.app.ui.components.InfoBanner
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.components.PersonRow
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.RadioGroup
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsList
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VpnKey
""".strip()


def settings_fn(name: str, title: str, banner: str | None, body: str) -> str:
    banner_line = f'        InfoBanner("{banner}")\n' if banner else ""
    return f"""
@Composable
fun {name}(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {{}},
) {{
{body}
    AivoraScaffold(title = "{title}", onBack = onBack) {{
{banner_line}        SettingsList(sections)
    }}
}}
"""


def radio_fn(name: str, title: str, banner: str, options: list[tuple[str, str | None]]) -> str:
    opts = []
    for t, s in options:
        if s is None:
            opts.append(f'"{t}" to null')
        else:
            opts.append(f'"{t}" to "{s}"')
    opts_joined = ",\n            ".join(opts)
    return f"""
@Composable
fun {name}(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {{}},
) {{
    var selected by remember {{ mutableIntStateOf(0) }}
    AivoraScaffold(title = "{title}", onBack = onBack) {{
        InfoBanner("{banner}")
        RadioGroup(
            options = listOf(
            {opts_joined}
            ),
            selected = selected,
            onSelect = {{ selected = it }},
        )
    }}
}}
"""


def form_fn(name: str, title: str, banner: str, fields: list[tuple[str, str]], button: str, nav: str) -> str:
    field_decls = "\n".join(
        f'    var f{i} by remember {{ mutableStateOf("") }}' for i, _ in enumerate(fields)
    )
    field_uis = "\n".join(
        f'''        OutlinedTextField(
            value = f{i},
            onValueChange = {{ f{i} = it }},
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            label = {{ Text("{label}") }},
            placeholder = {{ Text("{hint}") }},
            singleLine = true,
        )'''
        for i, (label, hint) in enumerate(fields)
    )
    return f"""
@Composable
fun {name}(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {{}},
) {{
{field_decls}
    AivoraScaffold(title = "{title}", onBack = onBack) {{
        InfoBanner("{banner}")
{field_uis}
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = "{button}",
            onClick = {{ onNavigate({nav}) }},
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }}
}}
"""


def list_people_fn(name: str, title: str, banner: str, people: list[tuple[str, str]], fab_nav: str | None = None) -> str:
    items = ",\n            ".join(f'"{n}" to "{s}"' for n, s in people)
    fab = ""
    if fab_nav:
        fab = f"""
        FloatingActionButton(
            onClick = {{ onNavigate({fab_nav}) }},
            containerColor = AivoraColors.Primary,
            contentColor = AivoraColors.OnPrimary,
        ) {{ Icon(Icons.Default.Add, contentDescription = null) }}
"""
    return f"""
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {name}(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {{}},
) {{
    var query by remember {{ mutableStateOf("") }}
    val people = listOf(
            {items}
    ).filter {{ query.isBlank() || it.first.contains(query, true) || it.second.contains(query, true) }}
    AivoraScaffold(
        title = "{title}",
        onBack = onBack,
        fab = {{ {fab} }},
    ) {{
        InfoBanner("{banner}")
        SearchField(query, {{ query = it }}, "Search")
        LazyColumn {{
            items(people) {{ (n, s) ->
                PersonRow(name = n, subtitle = s, onClick = {{ onNavigate(Routes.ChatId.replace("{{id}}", n.hashCode().toString())) }})
            }}
        }}
    }}
}}
"""


def build_settings_file() -> str:
    fns: list[str] = []

    # Settings hub (route settings/profile from Home menu)
    fns.append(
        settings_fn(
            "SettingsProfileScreen",
            "Settings",
            None,
            """
    var notify by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Edit profile", "Name, about, avatar, username", Icons.Default.Person, onClick = { onNavigate(Routes.YourDetails) }),
            SettingsItem("Account", "Security, email, passkeys, number", Icons.Default.AccountCircle, onClick = { onNavigate(Routes.SettingsAccountSecurityNotifications) }),
            SettingsItem("Privacy", "Last seen, profile photo, calls, locks", Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacy) }),
            SettingsItem("Chats", "Theme, backup, history, transfer", Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsBackup) }),
            SettingsItem("Lists", "Filter chats into custom lists", Icons.Default.Menu, onClick = { onNavigate(Routes.SettingsListsNew) }),
            SettingsItem("Notifications", "Message and call alerts", Icons.Default.Notifications, toggle = notify, onToggle = { notify = it }),
            SettingsItem("Storage and data", "Network usage, download, clear", Icons.Default.Storage, onClick = { onNavigate(Routes.SettingsStorage) }),
            SettingsItem("Linked devices", "Use Aivora on other devices", Icons.Default.Devices, onClick = { onNavigate(Routes.SettingsDevices) }),
            SettingsItem("Starred messages", null, Icons.Default.Star, onClick = { onNavigate(Routes.SettingsStarred) }),
            SettingsItem("Communities", null, Icons.Default.Groups, onClick = { onNavigate(Routes.SettingsCommunities) }),
            SettingsItem("QR code", "My QR code", Icons.Default.QrCode, onClick = { onNavigate(Routes.SettingsQr) }),
            SettingsItem("Permissions", "Camera, mic, contacts, location", Icons.Default.Security, onClick = { onNavigate(Routes.SettingsPermissions) }),
            SettingsItem("Two-step verification", null, Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsTwoStep) }),
            SettingsItem("Blocked contacts", null, Icons.Default.Block, onClick = { onNavigate(Routes.SettingsBlocked) }),
            SettingsItem("Add account", "Add a second account", Icons.Default.PersonAdd, onClick = { onNavigate(Routes.SettingsAccountAdd) }),
            SettingsItem("Delete account", null, Icons.Default.Delete, danger = true, onClick = { onNavigate(Routes.SettingsDeleteAccount) }),
        ),
    )
""",
        )
    )

    fns.append(
        settings_fn(
            "SettingsPrivacyScreen",
            "Privacy",
            "Control who can see your profile information and activity.",
            """
    val sections = listOf(
        "Who can see my personal info" to listOf(
            SettingsItem("Last seen and online", "Everyone", Icons.Default.Visibility, onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Profile photo", "Everyone", Icons.Default.Photo, onClick = { onNavigate(Routes.SettingsPrivacyProfilePhoto) }),
            SettingsItem("About", "Everyone", Icons.Default.Info, onClick = { onNavigate(Routes.SettingsPrivacyAbout) }),
            SettingsItem("Status", "My contacts", Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
        ),
        "Disappearing messages" to listOf(
            SettingsItem("Default message timer", "Off", Icons.Default.Timer, onClick = { onNavigate(Routes.SettingsPrivacyDefaultTimer) }),
        ),
        "Groups & calls" to listOf(
            SettingsItem("Groups", "Everyone", Icons.Default.Groups, onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
            SettingsItem("Calls", "Silence unknown callers", Icons.Default.Phone, onClick = { onNavigate(Routes.SettingsPrivacyCalls) }),
            SettingsItem("Live location", "None", Icons.Default.LocationOn, onClick = { onNavigate(Routes.SettingsPrivacyLiveLocation) }),
        ),
        "App security" to listOf(
            SettingsItem("App lock", "Off", Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
            SettingsItem("Chat lock", "Off", Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
            SettingsItem("Advanced chat privacy", null, Icons.Default.Security, onClick = { onNavigate(Routes.SettingsPrivacyAdvanced) }),
            SettingsItem("Privacy checkup", "Review your privacy", Icons.Default.Help, onClick = { onNavigate(Routes.SettingsPrivacyCheckup) }),
        ),
        null to listOf(
            SettingsItem("Blocked contacts", null, Icons.Default.Block, onClick = { onNavigate(Routes.SettingsBlocked) }),
        ),
    )
""",
        )
    )

    who = [("Everyone", None), ("My contacts", None), ("My contacts except…", "Hide from selected contacts"), ("Nobody", None)]
    for n, t, b in [
        ("SettingsPrivacyLastSeenScreen", "Last seen and online", "If you don't share your last seen, you won't see other people's last seen either."),
        ("SettingsPrivacyProfilePhotoScreen", "Profile photo", "Who can see your profile photo."),
        ("SettingsPrivacyAboutScreen", "About", "Who can see your about info."),
        ("SettingsPrivacyStatusScreen", "Status privacy", "Choose who can see your status updates."),
        ("SettingsPrivacyGroupsScreen", "Groups", "Choose who can add you to groups."),
        ("SettingsPrivacyCallsScreen", "Calls", "Blocked contacts can't call you or send you messages."),
    ]:
        fns.append(radio_fn(n, t, b, who))

    fns.append(
        radio_fn(
            "SettingsPrivacyDefaultTimerScreen",
            "Default message timer",
            "Start new chats with a disappearing message timer.",
            [("Off", None), ("24 hours", None), ("7 days", None), ("90 days", None)],
        )
    )

    # Bulk settings definitions: (fn, title, banner, kotlin sections body without `val sections =`)
    bulk = [
        (
            "SettingsQrScreen",
            "QR code",
            "Anyone who opens your QR link can see them, even without the app.",
            """
    var showPhoto by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("My QR code", "Show my name and photo on my QR page", Icons.Default.QrCode, onClick = { }),
            SettingsItem("Scan QR code", null, Icons.Default.CameraAlt, onClick = { }),
            SettingsItem("Show name and photo on QR page", null, Icons.Default.Visibility, toggle = showPhoto, onToggle = { showPhoto = it }),
        ),
    )
""",
        ),
        (
            "SettingsStarredScreen",
            "Starred messages",
            "Messages you star will show up here.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("No starred messages", "Search starred messages", Icons.Default.Star, onClick = { }),
            SettingsItem("Delete starred messages", "Remove stars from all your starred messages. The messages will remain.", Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsStorageScreen",
            "Storage and data",
            "Free up storage",
            """
    var lessData by remember { mutableStateOf(false) }
    val sections = listOf(
        "Storage" to listOf(
            SettingsItem("Manage storage", "Photos, videos, documents", Icons.Default.Storage, onClick = { }),
            SettingsItem("Network usage", null, Icons.Default.Storage, onClick = { }),
            SettingsItem("Use less data for calls", null, Icons.Default.Phone, toggle = lessData, onToggle = { lessData = it }),
        ),
        "Media auto-download" to listOf(
            SettingsItem("When using mobile data", "Photos", Icons.Default.Photo, onClick = { }),
            SettingsItem("When connected on Wi-Fi", "All", Icons.Default.Photo, onClick = { }),
            SettingsItem("When roaming", "No media", Icons.Default.Photo, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsTwoStepScreen",
            "Two-step verification",
            "Set up two-step verification",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Enable", "Two-step verification is enabled", Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) }),
            SettingsItem("Change PIN", "Two-step verification PIN", Icons.Default.Key, onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) }),
            SettingsItem("Recovery email", "Add a recovery email", Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
        ),
    )
""",
        ),
        (
            "SettingsBlockedScreen",
            "Blocked contacts",
            "Blocked contacts can't call you or send you messages.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Add blocked contact", "Choose a contact to block", Icons.Default.Block, onClick = { }),
            SettingsItem("Blocked · Tap to unblock", "Sample blocked contact", Icons.Default.Person, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsDevicesScreen",
            "Linked devices",
            "Link a device",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Link a device", "Scan QR on your other device", Icons.Default.Devices, onClick = { onNavigate(Routes.SettingsDevicesLink) }),
            SettingsItem("This phone", "Active now · Android", Icons.Default.Phone, onClick = { }),
            SettingsItem("Device authentication", null, Icons.Default.Fingerprint, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsDevicesLinkScreen",
            "Link a device",
            "Camera access lets this phone scan the QR code your new phone shows.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Scan QR code", "Point camera at the code", Icons.Default.QrCode, onClick = { }),
            SettingsItem("Link with phone number", null, Icons.Default.Phone, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsPermissionsScreen",
            "Permissions",
            "Control access for Aivora.",
            """
    var cam by remember { mutableStateOf(true) }
    var mic by remember { mutableStateOf(true) }
    var contacts by remember { mutableStateOf(true) }
    var loc by remember { mutableStateOf(false) }
    var notif by remember { mutableStateOf(true) }
    var photos by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Camera", "Allow camera", Icons.Default.Photo, toggle = cam, onToggle = { cam = it }),
            SettingsItem("Microphone", "Allow the microphone", Icons.Default.Mic, toggle = mic, onToggle = { mic = it }),
            SettingsItem("Contacts", "Allow contacts", Icons.Default.Person, toggle = contacts, onToggle = { contacts = it }),
            SettingsItem("Location", "Allow location access?", Icons.Default.LocationOn, toggle = loc, onToggle = { loc = it }),
            SettingsItem("Notifications", "Message and call alerts", Icons.Default.Notifications, toggle = notif, onToggle = { notif = it }),
            SettingsItem("Photos and videos", "Allow access to Photos", Icons.Default.Photo, toggle = photos, onToggle = { photos = it }),
        ),
    )
""",
        ),
        (
            "SettingsCommunitiesScreen",
            "Communities",
            "A community keeps related groups together with one announcement group.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Create community", null, Icons.Default.Groups, onClick = { onNavigate(Routes.NewGroup) }),
            SettingsItem("Your communities", "Everyone in the community", Icons.Default.Groups, onClick = { onNavigate(Routes.CommunityId.replace("{id}", "1")) }),
        ),
    )
""",
        ),
        (
            "SettingsDeleteAccountScreen",
            "Delete account",
            "Deleting your account is permanent. Your profile and message history will be wiped from this phone.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Change number instead", null, Icons.Default.Phone, onClick = { onNavigate(Routes.SettingsAccountChangeNumber) }),
            SettingsItem("Delete account", "Delete your account and wipe app data on this phone.", Icons.Default.Delete, danger = true, onClick = { onNavigate(Routes.SettingsAccountDeleted) }),
        ),
    )
""",
        ),
        (
            "SettingsAccountAddScreen",
            "Add account",
            "Add a second account",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Continue with phone number", null, Icons.Default.Phone, onClick = { onNavigate(Routes.Login) }),
            SettingsItem("Sign in with a passkey", null, Icons.Default.Fingerprint, onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email sign-in", null, Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
        ),
    )
""",
        ),
        (
            "SettingsAccountChangeNumberScreen",
            "Change number",
            "Migrate your account to a new number.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Current number", "Verified on this device", Icons.Default.Phone, onClick = { }),
            SettingsItem("Enter new number", "Add a phone number", Icons.Default.Phone, onClick = { onNavigate(Routes.Login) }),
        ),
    )
""",
        ),
        (
            "SettingsAccountDeletedScreen",
            "Account deleted",
            "Account deleted",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Create new account", null, Icons.Default.Person, onClick = { onNavigate(Routes.Login) }),
        ),
    )
""",
        ),
        (
            "SettingsAccountEmailScreen",
            "Email",
            "You can use this address to reset your two-step verification PIN if you forget it.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Add a recovery email", "Check the email and try again.", Icons.Default.Email, onClick = { }),
            SettingsItem("Verify email", null, Icons.Default.Check, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsAccountPasskeysScreen",
            "Passkeys",
            "Sign in with a passkey",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Create passkey", "Your passkey is ready", Icons.Default.Fingerprint, onClick = { }),
            SettingsItem("Delete passkey", "Other sign-in verification remains available, even if you delete this passkey.", Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsAccountRequestInfoScreen",
            "Request account info",
            "Download a copy of your account information and settings. Chat messages are not included.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Request report", "Account information report", Icons.Default.Info, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsAccountSecurityNotificationsScreen",
            "Security notifications",
            "Get notified when your security code changes.",
            """
    var show by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Show security notifications", null, Icons.Default.Security, toggle = show, onToggle = { show = it }),
            SettingsItem("Request account info", "Download a copy of your account information", Icons.Default.Info, onClick = { onNavigate(Routes.SettingsAccountRequestInfo) }),
            SettingsItem("Passkeys", null, Icons.Default.Fingerprint, onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email", null, Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
            SettingsItem("Change number", null, Icons.Default.Phone, onClick = { onNavigate(Routes.SettingsAccountChangeNumber) }),
            SettingsItem("Two-step verification", null, Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsTwoStep) }),
            SettingsItem("Delete account", null, Icons.Default.Delete, danger = true, onClick = { onNavigate(Routes.SettingsDeleteAccount) }),
        ),
    )
""",
        ),
        (
            "SettingsAccountTwoStepNewPinScreen",
            "New PIN",
            "Enter a 6-digit PIN for two-step verification.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Save PIN", "This account has two-step verification on.", Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsTwoStep) }),
        ),
    )
""",
        ),
        (
            "SettingsChatsArchivedScreen",
            "Archived chats",
            "Archive hides chats from your main list without deleting messages.",
            """
    var keep by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Keep chats archived", "New messages stay archived.", Icons.Default.Chat, toggle = keep, onToggle = { keep = it }),
            SettingsItem("Archive all chats", null, Icons.Default.Chat, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsBackupScreen",
            "Chat backup",
            "Back up your chats and media.",
            """
    var videos by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Back up now", "Backup complete", Icons.Default.Folder, onClick = { }),
            SettingsItem("Automatic backups", "Automatic backups start once you choose where to save them.", Icons.Default.Timer, onClick = { onNavigate(Routes.SettingsChatsBackupFrequency) }),
            SettingsItem("Backup location", "Internal storage", Icons.Default.Folder, onClick = { onNavigate(Routes.SettingsChatsBackupLocation) }),
            SettingsItem("End-to-end encrypted backup", null, Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsChatsBackupEncrypted) }),
            SettingsItem("Include videos", null, Icons.Default.Photo, toggle = videos, onToggle = { videos = it }),
            SettingsItem("Open backup", null, Icons.Default.Folder, onClick = { onNavigate(Routes.SettingsChatsBackupOpen) }),
            SettingsItem("Chat history", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Font size", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsFontSize) }),
            SettingsItem("Export chat", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Chat transfer", null, Icons.Default.Devices, onClick = { onNavigate(Routes.SettingsChatsTransfer) }),
            SettingsItem("Archived chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Transcripts", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsTranscripts) }),
        ),
    )
""",
        ),
        (
            "SettingsChatsBackupEncryptedScreen",
            "Encrypted backup",
            "This protects only your backup file. It is not your two-step verification PIN.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Create password", "Backup password", Icons.Default.Lock, onClick = { }),
            SettingsItem("Turn on encrypted backup", null, Icons.Default.Security, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsBackupFrequencyScreen",
            "Backup frequency",
            "Automatic backups",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Daily", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Weekly", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Monthly", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Off", null, Icons.Default.Timer, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsBackupLocationScreen",
            "Backup location",
            "Choose where to save backups.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Internal storage", null, Icons.Default.Storage, onClick = { }),
            SettingsItem("Memory card or USB storage", null, Icons.Default.Storage, onClick = { }),
            SettingsItem("Another app's storage", null, Icons.Default.Folder, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsBackupOpenScreen",
            "Open backup",
            "Restore chats from a backup file.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Restore from backup", "Aivora backup.zip", Icons.Default.Folder, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsExportScreen",
            "Export chat",
            "Export chat without media or with media.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Without media", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Include media", null, Icons.Default.Photo, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsFontSizeScreen",
            "Font size",
            "Message text size",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Small", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Medium", null, Icons.Default.Chat, selected = true, onClick = { }),
            SettingsItem("Large", null, Icons.Default.Chat, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsHistoryScreen",
            "Chat history",
            "Manage chat history on this device.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Export chat", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Archive all chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Clear all chats", null, Icons.Default.Delete, onClick = { }),
            SettingsItem("Delete all chats", null, Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsTranscriptsScreen",
            "Chat transcripts",
            "Export transcripts for selected chats.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Generate transcripts", null, Icons.Default.Chat, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsChatsTransferScreen",
            "Chat transfer",
            "Allow access for chat transfer?",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Transfer to new phone", null, Icons.Default.Devices, onClick = { onNavigate(Routes.TransferNewPhone) }),
            SettingsItem("Receive chats", null, Icons.Default.Devices, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsListsNewScreen",
            "New list",
            "A list can hold 500 chats at most. Add at least one chat to save your list.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("List name", "Favourites, Work, Family…", Icons.Default.Menu, onClick = { }),
            SettingsItem("Add people or groups", null, Icons.Default.Groups, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Reorder lists", null, Icons.Default.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
        ),
    )
""",
        ),
        (
            "SettingsListsEditIdScreen",
            "Edit list",
            "Edit list",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Rename list", null, Icons.Default.Edit, onClick = { }),
            SettingsItem("Add chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Delete list", "Deleting this list does not delete any chats.", Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsListsReorderScreen",
            "Reorder lists",
            "Default lists",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Favourites", null, Icons.Default.Star, onClick = { }),
            SettingsItem("Unread", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Groups", null, Icons.Default.Groups, onClick = { }),
            SettingsItem("Custom lists aren't available yet.", null, Icons.Default.Menu, onClick = { }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyAdvancedScreen",
            "Advanced chat privacy",
            "Add more privacy to your chats. Advanced privacy is on: this chat can't be exported or forwarded out of.",
            """
    var adv by remember { mutableStateOf(false) }
    var def by remember { mutableStateOf(false) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Advanced chat privacy", "Advanced chat privacy · Can't be exported", Icons.Default.Security, toggle = adv, onToggle = { adv = it }),
            SettingsItem("Default for new chats", null, Icons.Default.Chat, toggle = def, onToggle = { def = it }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyAppLockScreen",
            "App lock",
            "Authenticate to unlock",
            """
    var require by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Require unlock", "App lock", Icons.Default.Lock, toggle = require, onToggle = { require = it }),
            SettingsItem("Automatically lock", "Immediately", Icons.Default.Timer, onClick = { }),
            SettingsItem("Unlock with biometrics", null, Icons.Default.Fingerprint, toggle = bio, onToggle = { bio = it }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyChatLockScreen",
            "Chat lock",
            "Unlock locked chats with your secret code.",
            """
    var on by remember { mutableStateOf(false) }
    var clear by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Turn on chat lock", null, Icons.Default.Lock, toggle = on, onToggle = { on = it }),
            SettingsItem("Secret code", null, Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsPrivacyChatLockSecretCode) }),
            SettingsItem("Unlock and clear from recent", null, Icons.Default.Visibility, toggle = clear, onToggle = { clear = it }),
            SettingsItem("Locked chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.LockedChats) }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyChatLockSecretCodeScreen",
            "Secret code",
            "Choose a secret code to open locked chats.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Enter secret code", null, Icons.Default.VpnKey, onClick = { }),
            SettingsItem("Confirm secret code", null, Icons.Default.VpnKey, onClick = { }),
            SettingsItem("Save", null, Icons.Default.Check, onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyCheckupScreen",
            "Privacy checkup",
            "Done returns to Privacy. Completing this review doesn’t change your settings.",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Personal info", "Last seen, photo, about", Icons.Default.Person, onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Status privacy", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
            SettingsItem("Groups and calls", null, Icons.Default.Groups, onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
            SettingsItem("App lock", null, Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
        ),
    )
""",
        ),
        (
            "SettingsPrivacyLiveLocationScreen",
            "Live location",
            "Live location sharing stopped",
            """
    val sections = listOf(
        null to listOf(
            SettingsItem("Stop all live locations", "Live location ended", Icons.Default.LocationOn, onClick = { }),
            SettingsItem("Live location · from another device", null, Icons.Default.Devices, onClick = { }),
        ),
    )
""",
        ),
    ]

    for name, title, banner, body in bulk:
        fns.append(settings_fn(name, title, banner, body))

    return COMMON_IMPORTS + "\n\n" + "\n".join(fns)


def build_main_screens() -> dict[str, str]:
    files: dict[str, str] = {}

    files["ChatScreens.kt"] = COMMON_IMPORTS + """

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatIdScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }
    val messages = listOf(
        false to "Assalam o Alaikum!",
        true to "Walikum Assalam — how's it going?",
        false to "All good. Are we still on for the call?",
        true to "Yes, joining in 5.",
    )
    AivoraScaffold(
        title = "Chat",
        onBack = onBack,
        actions = {
            IconButton(onClick = { onNavigate(Routes.NewCall) }) { Icon(Icons.Default.Videocam, null) }
            IconButton(onClick = { onNavigate(Routes.NewCall) }) { Icon(Icons.Default.Call, null) }
            IconButton(onClick = { onNavigate(Routes.ProfileId.replace("{id}", "1")) }) { Icon(Icons.Default.MoreVert, null) }
        },
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(messages) { (mine, body) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
                ) {
                    Text(
                        body,
                        modifier = Modifier
                            .padding(4.dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        color = if (mine) AivoraColors.OnPrimary else MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onNavigate(Routes.EventCreateChat.replace("{chat}", "1")) }) {
                Icon(Icons.Default.Add, null, tint = AivoraColors.Primary)
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { }) { Icon(Icons.Default.AttachFile, null) }
                        IconButton(onClick = { onNavigate(Routes.NewStatus) }) { Icon(Icons.Default.CameraAlt, null) }
                    }
                },
            )
            IconButton(onClick = { text = "" }) {
                Icon(if (text.isBlank()) Icons.Default.Mic else Icons.Default.Send, null, tint = AivoraColors.Primary)
            }
        }
        Row(Modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            LinkButton("Poll") { onNavigate(Routes.MessageInfoChatMessage.replace("{chat}", "1").replace("{message}", "1")) }
            LinkButton("Event") { onNavigate(Routes.EventCreateChat.replace("{chat}", "1")) }
            LinkButton("Info") { onNavigate(Routes.MessageInfoChatMessage.replace("{chat}", "1").replace("{message}", "1")) }
        }
    }
}
"""

    files["ChannelsScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun ChannelsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val channels = listOf(
        "Aivora News" to "Verified · 12.4k followers",
        "Aivora Updates" to "Listed channel",
        "Tech Daily" to "1.2k followers",
    ).filter { query.isBlank() || it.first.contains(query, true) }
    AivoraScaffold(
        title = "Channels",
        onBack = onBack,
        fab = {
            FloatingActionButton(onClick = { onNavigate(Routes.ChannelSlug.replace("{slug}", "new")) }, containerColor = AivoraColors.Primary, contentColor = AivoraColors.OnPrimary) {
                Icon(Icons.Default.Add, null)
            }
        },
    ) {
        InfoBanner("Find channels")
        SearchField(query, { query = it }, "Search channels")
        if (channels.isEmpty()) EmptyState("No channels match.", "Try a different search.")
        LazyColumn {
            items(channels) { (n, s) ->
                PersonRow(n, s, onClick = { onNavigate(Routes.ChannelSlug.replace("{slug}", n.lowercase().replace(" ", "-"))) })
            }
        }
    }
}
"""

    files["ChannelScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun ChannelSlugScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(
        title = "Channel",
        onBack = onBack,
        actions = {
            IconButton(onClick = { }) { Icon(Icons.Default.Search, null) }
            IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null) }
        },
        fab = {
            FloatingActionButton(onClick = { }, containerColor = AivoraColors.Primary, contentColor = AivoraColors.OnPrimary) {
                Icon(Icons.Default.Edit, contentDescription = "New channel post")
            }
        },
    ) {
        InfoBanner("New channel post")
        val posts = listOf(
            "Welcome to the channel — follow for updates.",
            "Today's briefing is live.",
            "Tap follow to get notified.",
        )
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(posts) { p ->
                Column(Modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text(p, style = MaterialTheme.typography.bodyLarge)
                    Text("View count · Reactions", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                PrimaryButton("Follow channel", onClick = { })
                Spacer(Modifier.height(8.dp))
                LinkButton("Report this channel") { }
            }
        }
    }
}
"""

    files["CommunityScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun CommunityIdScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Community", onBack = onBack) {
        InfoBanner("A community keeps related groups together with one announcement group.")
        SettingsList(
            listOf(
                "Announcement" to listOf(
                    SettingsItem("Community announcements", "Everyone in the community", Icons.Default.Groups, onClick = { onNavigate(Routes.ChatId.replace("{id}", "ann")) }),
                ),
                "Groups" to listOf(
                    SettingsItem("General", "128 members", Icons.Default.Groups, onClick = { onNavigate(Routes.ChatId.replace("{id}", "g1")) }),
                    SettingsItem("Events", "42 members", Icons.Default.Event, onClick = { onNavigate(Routes.ChatId.replace("{id}", "g2")) }),
                ),
            )
        )
    }
}
"""

    people = [
        ("Ahmad Khan", "Hey there! I am using Aivora."),
        ("Fatima Ali", "Available"),
        ("Usman Raza", "At work"),
        ("Zainab Noor", "Busy"),
        ("Hassan Malik", "Battery about to die"),
    ]

    files["NewChatScreens.kt"] = COMMON_IMPORTS + list_people_fn(
        "NewChatScreen", "New chat", "Allow access to your contacts", people, "Routes.NewGroup"
    )
    files["NewCallScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun NewCallScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Calls", onBack = onBack, fab = {
        FloatingActionButton(onClick = { }, containerColor = AivoraColors.Primary, contentColor = AivoraColors.OnPrimary) {
            Icon(Icons.Default.Add, null)
        }
    }) {
        InfoBanner("Audio call · A call can have up to many participants.")
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Create call link", "Anyone with the link can join this call — no chat needed.", Icons.Default.Call, onClick = { }),
                    SettingsItem("Scheduled calls", null, Icons.Default.Event, onClick = { onNavigate(Routes.ScheduledCalls) }),
                    SettingsItem("New call", "Add to call", Icons.Default.Call, onClick = { onNavigate(Routes.NewChat) }),
                ),
                "Recent" to listOf(
                    SettingsItem("Ahmad Khan", "Outgoing · Yesterday", Icons.Default.Call, onClick = { }),
                    SettingsItem("Fatima Ali", "Missed · 2 days ago", Icons.Default.Videocam, onClick = { }),
                ),
            )
        )
    }
}
"""

    files["NewGroupScreens.kt"] = COMMON_IMPORTS + form_fn(
        "NewGroupScreen",
        "New group",
        "Choose the people you want in this group.",
        [("Group name", "Add a group photo · Optional"), ("Members", "Add people or groups")],
        "Create group",
        "Routes.Home",
    )

    files["NewStatusScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun NewStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }
    AivoraScaffold(title = "New status", onBack = onBack) {
        InfoBanner("My status · Photo status · Text status")
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(160.dp),
            placeholder = { Text("Type a status") },
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Photo status", null, Icons.Default.Photo, onClick = { }),
                    SettingsItem("Camera", null, Icons.Default.CameraAlt, onClick = { }),
                    SettingsItem("Status privacy", "Who can see your status", Icons.Default.Visibility, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
                    SettingsItem("Post status", null, Icons.Default.Send, onClick = { onNavigate(Routes.Home) }),
                )
            )
        )
    }
}
"""

    files["LockedChatsScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun LockedChatsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Locked chats", onBack = onBack) {
        InfoBanner("Authenticate to unlock")
        EmptyState("No locked chats", "Turn on chat lock from Privacy settings.")
        PrimaryButton("Open chat lock settings", onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }, modifier = Modifier.padding(16.dp))
    }
}
"""

    files["ProfileScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun ProfileIdScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Contact info", onBack = onBack) {
        Column(Modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar("A", size = 96)
            Spacer(Modifier.height(12.dp))
            Text("Ahmad Khan", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("+92 300 1234567", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Audio call", null, Icons.Default.Call, onClick = { onNavigate(Routes.NewCall) }),
                    SettingsItem("Video call", null, Icons.Default.Videocam, onClick = { onNavigate(Routes.NewCall) }),
                    SettingsItem("Search in chat", null, Icons.Default.Search, onClick = { }),
                    SettingsItem("Media, links, and docs", null, Icons.Default.Photo, onClick = { }),
                    SettingsItem("Starred messages", null, Icons.Default.Star, onClick = { onNavigate(Routes.SettingsStarred) }),
                    SettingsItem("Notifications", null, Icons.Default.Notifications, onClick = { }),
                    SettingsItem("Disappearing messages", "Off", Icons.Default.Timer, onClick = { onNavigate(Routes.SettingsPrivacyDefaultTimer) }),
                    SettingsItem("Advanced chat privacy", null, Icons.Default.Security, onClick = { onNavigate(Routes.SettingsPrivacyAdvanced) }),
                    SettingsItem("Encryption", "Messages and calls are end-to-end encrypted.", Icons.Default.Lock, onClick = { }),
                    SettingsItem("Block", null, Icons.Default.Block, danger = true, onClick = { onNavigate(Routes.SettingsBlocked) }),
                    SettingsItem("Report", null, Icons.Default.Info, danger = true, onClick = { }),
                )
            )
        )
    }
}
"""

    files["YourDetailsScreens.kt"] = COMMON_IMPORTS + form_fn(
        "YourDetailsScreen",
        "Edit profile",
        "Add your name",
        [("Name", "Add your name"), ("About", "Hey there! I am using Aivora."), ("Username", "Choose a username")],
        "Save",
        "Routes.SettingsProfile",
    )

    files["ProfileSetupScreens.kt"] = COMMON_IMPORTS + form_fn(
        "ProfileSetupScreen",
        "Profile setup",
        "Add profile picture",
        [("Your name", "Add your name"), ("About", "Hey there! I am using Aivora.")],
        "Finish",
        "Routes.Home",
    )

    files["TransferScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun TransferNewPhoneScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Transfer chats", onBack = onBack) {
        InfoBanner("Aivora needs the camera to scan your new phone’s QR code.")
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Scan QR code", "Camera access lets this phone scan the QR code your new phone shows.", Icons.Default.QrCode, onClick = { }),
                    SettingsItem("Continue", null, Icons.Default.Devices, onClick = { onNavigate(Routes.Home) }),
                )
            )
        )
    }
}
"""

    files["ScheduledCallsScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun ScheduledCallsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Scheduled calls", onBack = onBack, fab = {
        FloatingActionButton(onClick = { }, containerColor = AivoraColors.Primary, contentColor = AivoraColors.OnPrimary) {
            Icon(Icons.Default.Add, null)
        }
    }) {
        InfoBanner("A scheduled call is starting soon")
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Team standup", "Tomorrow · 10:00 · Audio", Icons.Default.Call, onClick = { }),
                    SettingsItem("Family catch-up", "Friday · Video", Icons.Default.Videocam, onClick = { }),
                    SettingsItem("Back to scheduled calls", null, Icons.Default.Event, onClick = { }),
                )
            )
        )
    }
}
"""

    files["EventCreateScreens.kt"] = COMMON_IMPORTS + form_fn(
        "EventCreateChatScreen",
        "Create event",
        "Event title *",
        [("Title", "Event title *"), ("Description", "Optional"), ("Location", "Add a place or address"), ("Starts at", "Date and time")],
        "Create event",
        "Routes.Home",
    )
    files["EventDetailsScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun EventDetailsChatMessageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Event details", onBack = onBack) {
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Going", null, Icons.Default.Check, onClick = { }),
                    SettingsItem("Maybe", null, Icons.Default.Info, onClick = { }),
                    SettingsItem("Can't go", null, Icons.Default.Close, onClick = { }),
                    SettingsItem("Edit event", null, Icons.Default.Edit, onClick = { onNavigate(Routes.EventEditChatMessage.replace("{chat}", "1").replace("{message}", "1")) }),
                    SettingsItem("Cancel event", "Cancel this event?", Icons.Default.Delete, danger = true, onClick = { }),
                )
            )
        )
    }
}
"""
    files["EventEditScreens.kt"] = COMMON_IMPORTS + form_fn(
        "EventEditChatMessageScreen",
        "Edit event",
        "Discard this event?",
        [("Title", "Event title *"), ("Description", "Optional"), ("Location", "Add a place or address")],
        "Save",
        "Routes.Home",
    )
    files["MessageInfoScreens.kt"] = COMMON_IMPORTS + """

@Composable
fun MessageInfoChatMessageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    AivoraScaffold(title = "Message info", onBack = onBack) {
        InfoBanner("Read receipts and delivery details for this message.")
        SettingsList(
            listOf(
                "Read by" to listOf(
                    SettingsItem("Ahmad Khan", "Today, 9:41 PM", Icons.Default.Person, onClick = { }),
                ),
                "Delivered to" to listOf(
                    SettingsItem("Fatima Ali", "Today, 9:40 PM", Icons.Default.Person, onClick = { }),
                ),
                null to listOf(
                    SettingsItem("Star", null, Icons.Default.Star, onClick = { }),
                    SettingsItem("Delete for everyone", null, Icons.Default.Delete, danger = true, onClick = { }),
                ),
            )
        )
    }
}
"""

    return files


def main() -> None:
    w(SCREENS / "SettingsScreens.kt", build_settings_file())
    for name, content in build_main_screens().items():
        w(SCREENS / name, content)

    # Verify every route screen function exists
    routes = {
        "ChannelSlugScreen",
        "ChannelsScreen",
        "ChatIdScreen",
        "CommunityIdScreen",
        "EventCreateChatScreen",
        "EventDetailsChatMessageScreen",
        "EventEditChatMessageScreen",
        "HomeScreen",
        "LockedChatsScreen",
        "LoginScreen",
        "MessageInfoChatMessageScreen",
        "NewCallScreen",
        "NewChatScreen",
        "NewGroupScreen",
        "NewStatusScreen",
        "ProfileSetupScreen",
        "ProfileIdScreen",
        "ScheduledCallsScreen",
        "SettingsAccountAddScreen",
        "SettingsAccountChangeNumberScreen",
        "SettingsAccountDeletedScreen",
        "SettingsAccountEmailScreen",
        "SettingsAccountPasskeysScreen",
        "SettingsAccountRequestInfoScreen",
        "SettingsAccountSecurityNotificationsScreen",
        "SettingsAccountTwoStepNewPinScreen",
        "SettingsBlockedScreen",
        "SettingsChatsArchivedScreen",
        "SettingsChatsBackupScreen",
        "SettingsChatsBackupEncryptedScreen",
        "SettingsChatsBackupFrequencyScreen",
        "SettingsChatsBackupLocationScreen",
        "SettingsChatsBackupOpenScreen",
        "SettingsChatsExportScreen",
        "SettingsChatsFontSizeScreen",
        "SettingsChatsHistoryScreen",
        "SettingsChatsTranscriptsScreen",
        "SettingsChatsTransferScreen",
        "SettingsCommunitiesScreen",
        "SettingsDeleteAccountScreen",
        "SettingsDevicesScreen",
        "SettingsDevicesLinkScreen",
        "SettingsListsEditIdScreen",
        "SettingsListsNewScreen",
        "SettingsListsReorderScreen",
        "SettingsPermissionsScreen",
        "SettingsPrivacyScreen",
        "SettingsPrivacyAboutScreen",
        "SettingsPrivacyAdvancedScreen",
        "SettingsPrivacyAppLockScreen",
        "SettingsPrivacyCallsScreen",
        "SettingsPrivacyChatLockScreen",
        "SettingsPrivacyChatLockSecretCodeScreen",
        "SettingsPrivacyCheckupScreen",
        "SettingsPrivacyDefaultTimerScreen",
        "SettingsPrivacyGroupsScreen",
        "SettingsPrivacyLastSeenScreen",
        "SettingsPrivacyLiveLocationScreen",
        "SettingsPrivacyProfilePhotoScreen",
        "SettingsPrivacyStatusScreen",
        "SettingsProfileScreen",
        "SettingsQrScreen",
        "SettingsStarredScreen",
        "SettingsStorageScreen",
        "SettingsTwoStepScreen",
        "TransferNewPhoneScreen",
        "YourDetailsScreen",
    }
    found = set()
    for p in SCREENS.glob("*.kt"):
        text = p.read_text()
        for r in routes:
            if f"fun {r}(" in text:
                found.add(r)
    missing = sorted(routes - found)
    print("found", len(found), "missing", missing)
    # Login + Home kept as hand-written earlier — ensure still present
    for required in ("LoginScreen", "HomeScreen"):
        assert any(f"fun {required}(" in p.read_text() for p in SCREENS.glob("*.kt")), required


if __name__ == "__main__":
    main()
