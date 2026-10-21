package com.aivora.app.ui.feature.settings

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
import androidx.compose.material.icons.filled.Menu
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


@Composable
fun SettingsProfileScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    var notify by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Edit profile", "Name, about, avatar, username", Icons.Default.Person, onClick = { onNavigate(Routes.YourDetails) }),
            SettingsItem("Account", "Security, email, passkeys, number", Icons.Default.AccountCircle, onClick = { onNavigate(Routes.SettingsAccount) }),
            SettingsItem("Privacy", "Last seen, profile photo, calls, locks", Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacy) }),
            SettingsItem("Chats", "Theme, backup, history, transfer", Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChats) }),
            SettingsItem("Lists", "Filter chats into custom lists", Icons.Default.Menu, onClick = { onNavigate(Routes.SettingsListsNew) }),
            SettingsItem("Reorder lists", "Change list order", Icons.Default.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
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
        null to listOf(
            SettingsItem("Security notifications", null, Icons.Default.Security, onClick = { onNavigate(Routes.SettingsAccountSecurityNotifications) }),
            SettingsItem("Passkeys", null, Icons.Default.Key, onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email address", "Not added", Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
            SettingsItem("Two-step verification", "Off", Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsTwoStep) }),
        ),
        null to listOf(
            SettingsItem("Change number", null, Icons.Default.Phone, onClick = { onNavigate(Routes.SettingsAccountChangeNumber) }),
            SettingsItem("Request account info", null, Icons.Default.Folder, onClick = { onNavigate(Routes.SettingsAccountRequestInfo) }),
            SettingsItem("Add account", null, Icons.Default.PersonAdd, onClick = { onNavigate(Routes.SettingsAccountAdd) }),
        ),
        null to listOf(
            SettingsItem("Delete account", null, Icons.Default.Delete, danger = true, onClick = { onNavigate(Routes.SettingsDeleteAccount) }),
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
    val sections = listOf(
        null to listOf(
            SettingsItem("Archived chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Chat backup", "Google Drive / local", Icons.Default.Storage, onClick = { onNavigate(Routes.SettingsChatsBackup) }),
            SettingsItem("Chat history", "Clear, export, or transfer", Icons.Default.Folder, onClick = { onNavigate(Routes.SettingsChatsHistory) }),
            SettingsItem("Export chat", null, Icons.Default.Folder, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Transfer chats", "Move to a new phone", Icons.Default.Devices, onClick = { onNavigate(Routes.SettingsChatsTransfer) }),
            SettingsItem("Font size", null, Icons.Default.Edit, onClick = { onNavigate(Routes.SettingsChatsFontSize) }),
            SettingsItem("Chat transcripts", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsTranscripts) }),
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
    var findable by remember { mutableStateOf(true) }

    val sections = listOf(
        null to listOf(
            SettingsItem("Last seen and online", null, onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Profile photo", null, onClick = { onNavigate(Routes.SettingsPrivacyProfilePhoto) }),
            SettingsItem("About", null, onClick = { onNavigate(Routes.SettingsPrivacyAbout) }),
            SettingsItem("Status", null, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
            SettingsItem("Groups", null, onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
            SettingsItem(
                "Read receipts",
                trailing = if (readReceipts) "On" else "Off",
                toggle = readReceipts,
                onToggle = { readReceipts = it },
            ),
        ),
        null to listOf(
            SettingsItem(
                "Let people find me",
                toggle = findable,
                onToggle = { findable = it },
            ),
        ),
        null to listOf(
            SettingsItem("Default message timer", trailing = "Off", onClick = { onNavigate(Routes.SettingsPrivacyDefaultTimer) }),
            SettingsItem("Calls", trailing = "Silence Off", onClick = { onNavigate(Routes.SettingsPrivacyCalls) }),
            SettingsItem("Blocked contacts", trailing = "None", onClick = { onNavigate(Routes.SettingsBlocked) }),
            SettingsItem("Live location", trailing = "None", onClick = { onNavigate(Routes.SettingsPrivacyLiveLocation) }),
            SettingsItem("App lock", trailing = "Off", onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
            SettingsItem("Chat lock", trailing = "Off", onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
            SettingsItem("Advanced", null, onClick = { onNavigate(Routes.SettingsPrivacyAdvanced) }),
            SettingsItem("Privacy checkup", null, onClick = { onNavigate(Routes.SettingsPrivacyCheckup) }),
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
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Last seen and online", onBack = onBack) {
        InfoBanner("If you don't share your last seen, you won't see other people's last seen either.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyProfilePhotoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Profile photo", onBack = onBack) {
        InfoBanner("Who can see your profile photo.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyAboutScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "About", onBack = onBack) {
        InfoBanner("Who can see your about info.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyStatusScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Status privacy", onBack = onBack) {
        InfoBanner("Choose who can see your status updates.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyGroupsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Groups", onBack = onBack) {
        InfoBanner("Choose who can add you to groups.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyCallsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Calls", onBack = onBack) {
        InfoBanner("Blocked contacts can't call you or send you messages.")
        RadioGroup(
            options = listOf(
            "Everyone" to null,
            "My contacts" to null,
            "My contacts except…" to "Hide from selected contacts",
            "Nobody" to null
            ),
            selected = selected,
            onSelect = { selected = it },
        )
    }
}


@Composable
fun SettingsPrivacyDefaultTimerScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var selected by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Default message timer", onBack = onBack) {
        InfoBanner("Start new chats with a disappearing message timer.")
        RadioGroup(
            options = listOf(
            "Off" to null,
            "24 hours" to null,
            "7 days" to null,
            "90 days" to null
            ),
            selected = selected,
            onSelect = { selected = it },
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
            SettingsItem("My QR code", "Show my name and photo on my QR page", Icons.Default.QrCode, onClick = { }),
            SettingsItem("Scan QR code", null, Icons.Default.CameraAlt, onClick = { }),
            SettingsItem("Show name and photo on QR page", null, Icons.Default.Visibility, toggle = showPhoto, onToggle = { showPhoto = it }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("No starred messages", "Search starred messages", Icons.Default.Star, onClick = { }),
            SettingsItem("Delete starred messages", "Remove stars from all your starred messages. The messages will remain.", Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Starred messages", onBack = onBack) {
        InfoBanner("Messages you star will show up here.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsStorageScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

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

    AivoraScaffold(title = "Storage and data", onBack = onBack) {
        InfoBanner("Free up storage")
        SettingsList(sections)
    }
}


@Composable
fun SettingsTwoStepScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Enable", "Two-step verification is enabled", Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) }),
            SettingsItem("Change PIN", "Two-step verification PIN", Icons.Default.Key, onClick = { onNavigate(Routes.SettingsAccountTwoStepNewPin) }),
            SettingsItem("Recovery email", "Add a recovery email", Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
        ),
    )

    AivoraScaffold(title = "Two-step verification", onBack = onBack) {
        InfoBanner("Set up two-step verification")
        SettingsList(sections)
    }
}


@Composable
fun SettingsBlockedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Add blocked contact", "Choose a contact to block", Icons.Default.Block, onClick = { }),
            SettingsItem("Blocked · Tap to unblock", "Sample blocked contact", Icons.Default.Person, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Blocked contacts", onBack = onBack) {
        InfoBanner("Blocked contacts can't call you or send you messages.")
        SettingsList(sections)
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
                        Icons.Default.QrCode,
                        onClick = { onNavigate(Routes.SettingsDevicesLink) },
                    ),
                ),
            ),
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("V2440", "android · last active now · this device", Icons.Default.Phone, onClick = { }),
                    SettingsItem("Chrome on macOS", "web · last active Yesterday", Icons.Default.Devices, trailing = "Sign out", onClick = { }),
                    SettingsItem("V2440", "android · last active Mon", Icons.Default.Phone, trailing = "Sign out", onClick = { }),
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
            SettingsItem("Scan QR code", "Point camera at the code", Icons.Default.QrCode, onClick = { }),
            SettingsItem("Link with phone number", null, Icons.Default.Phone, onClick = { }),
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
            SettingsItem("Camera", "Allow camera", Icons.Default.Photo, toggle = cam, onToggle = { cam = it }),
            SettingsItem("Microphone", "Allow the microphone", Icons.Default.Mic, toggle = mic, onToggle = { mic = it }),
            SettingsItem("Contacts", "Allow contacts", Icons.Default.Person, toggle = contacts, onToggle = { contacts = it }),
            SettingsItem("Location", "Allow location access?", Icons.Default.LocationOn, toggle = loc, onToggle = { loc = it }),
            SettingsItem("Notifications", "Message and call alerts", Icons.Default.Notifications, toggle = notif, onToggle = { notif = it }),
            SettingsItem("Photos and videos", "Allow access to Photos", Icons.Default.Photo, toggle = photos, onToggle = { photos = it }),
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
            SettingsItem("Create community", null, Icons.Default.Groups, onClick = { onNavigate(Routes.NewGroup) }),
            SettingsItem("Your communities", "Everyone in the community", Icons.Default.Groups, onClick = { onNavigate(Routes.CommunityId.replace("{id}", "1")) }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Change number instead", null, Icons.Default.Phone, onClick = { onNavigate(Routes.SettingsAccountChangeNumber) }),
            SettingsItem("Delete account", "Delete your account and wipe app data on this phone.", Icons.Default.Delete, danger = true, onClick = { onNavigate(Routes.SettingsAccountDeleted) }),
        ),
    )

    AivoraScaffold(title = "Delete account", onBack = onBack) {
        InfoBanner("Deleting your account is permanent. Your profile and message history will be wiped from this phone.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountAddScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Continue with phone number", null, Icons.Default.Phone, onClick = { onNavigate(Routes.Login) }),
            SettingsItem("Sign in with a passkey", null, Icons.Default.Fingerprint, onClick = { onNavigate(Routes.SettingsAccountPasskeys) }),
            SettingsItem("Email sign-in", null, Icons.Default.Email, onClick = { onNavigate(Routes.SettingsAccountEmail) }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Current number", "Verified on this device", Icons.Default.Phone, onClick = { }),
            SettingsItem("Enter new number", "Add a phone number", Icons.Default.Phone, onClick = { onNavigate(Routes.Login) }),
        ),
    )

    AivoraScaffold(title = "Change number", onBack = onBack) {
        InfoBanner("Migrate your account to a new number.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountDeletedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Create new account", null, Icons.Default.Person, onClick = { onNavigate(Routes.Login) }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Add a recovery email", "Check the email and try again.", Icons.Default.Email, onClick = { }),
            SettingsItem("Verify email", null, Icons.Default.Check, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Email", onBack = onBack) {
        InfoBanner("You can use this address to reset your two-step verification PIN if you forget it.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountPasskeysScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Create passkey", "Your passkey is ready", Icons.Default.Fingerprint, onClick = { }),
            SettingsItem("Delete passkey", "Other sign-in verification remains available, even if you delete this passkey.", Icons.Default.Delete, danger = true, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Passkeys", onBack = onBack) {
        InfoBanner("Sign in with a passkey")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountRequestInfoScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Request report", "Account information report", Icons.Default.Info, onClick = { }),
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

    AivoraScaffold(title = "Security notifications", onBack = onBack) {
        InfoBanner("Get notified when your security code changes.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsAccountTwoStepNewPinScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Save PIN", "This account has two-step verification on.", Icons.Default.VpnKey, onClick = { onNavigate(Routes.SettingsTwoStep) }),
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
            SettingsItem("Keep chats archived", "New messages stay archived.", Icons.Default.Chat, toggle = keep, onToggle = { keep = it }),
            SettingsItem("Archive all chats", null, Icons.Default.Chat, onClick = { }),
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

    AivoraScaffold(title = "Chat backup", onBack = onBack) {
        InfoBanner("Back up your chats and media.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsChatsBackupEncryptedScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Create password", "Backup password", Icons.Default.Lock, onClick = { }),
            SettingsItem("Turn on encrypted backup", null, Icons.Default.Security, onClick = { }),
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
            SettingsItem("Daily", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Weekly", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Monthly", null, Icons.Default.Timer, onClick = { }),
            SettingsItem("Off", null, Icons.Default.Timer, onClick = { }),
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
            SettingsItem("Internal storage", null, Icons.Default.Storage, onClick = { }),
            SettingsItem("Memory card or USB storage", null, Icons.Default.Storage, onClick = { }),
            SettingsItem("Another app's storage", null, Icons.Default.Folder, onClick = { }),
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
            SettingsItem("Restore from backup", "Aivora backup.zip", Icons.Default.Folder, onClick = { }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Without media", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Include media", null, Icons.Default.Photo, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Export chat", onBack = onBack) {
        InfoBanner("Export chat without media or with media.")
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
            SettingsItem("Small", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Medium", null, Icons.Default.Chat, selected = true, onClick = { }),
            SettingsItem("Large", null, Icons.Default.Chat, onClick = { }),
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
            SettingsItem("Export chat", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsExport) }),
            SettingsItem("Archive all chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsChatsArchived) }),
            SettingsItem("Clear all chats", null, Icons.Default.Delete, onClick = { }),
            SettingsItem("Delete all chats", null, Icons.Default.Delete, danger = true, onClick = { }),
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
            SettingsItem("Generate transcripts", null, Icons.Default.Chat, onClick = { }),
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

    val sections = listOf(
        null to listOf(
            SettingsItem("Transfer to new phone", null, Icons.Default.Devices, onClick = { onNavigate(Routes.TransferNewPhone) }),
            SettingsItem("Receive chats", null, Icons.Default.Devices, onClick = { }),
        ),
    )

    AivoraScaffold(title = "Chat transfer", onBack = onBack) {
        InfoBanner("Allow access for chat transfer?")
        SettingsList(sections)
    }
}


@Composable
fun SettingsListsNewScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("List name", "Favourites, Work, Family…", Icons.Default.Menu, onClick = { }),
            SettingsItem("Add people or groups", null, Icons.Default.Groups, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Reorder lists", null, Icons.Default.Menu, onClick = { onNavigate(Routes.SettingsListsReorder) }),
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
            SettingsItem("Rename list", null, Icons.Default.Edit, onClick = { }),
            SettingsItem("Add chats", null, Icons.Default.Chat, onClick = { onNavigate(Routes.NewChat) }),
            SettingsItem("Delete list", "Deleting this list does not delete any chats.", Icons.Default.Delete, danger = true, onClick = { }),
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
            SettingsItem("Favourites", null, Icons.Default.Star, onClick = { }),
            SettingsItem("Unread", null, Icons.Default.Chat, onClick = { }),
            SettingsItem("Groups", null, Icons.Default.Groups, onClick = { }),
            SettingsItem("Custom lists aren't available yet.", null, Icons.Default.Menu, onClick = { }),
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

    var adv by remember { mutableStateOf(false) }
    var def by remember { mutableStateOf(false) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Advanced chat privacy", "Advanced chat privacy · Can't be exported", Icons.Default.Security, toggle = adv, onToggle = { adv = it }),
            SettingsItem("Default for new chats", null, Icons.Default.Chat, toggle = def, onToggle = { def = it }),
        ),
    )

    AivoraScaffold(title = "Advanced chat privacy", onBack = onBack) {
        InfoBanner("Add more privacy to your chats. Advanced privacy is on: this chat can't be exported or forwarded out of.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyAppLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    var require by remember { mutableStateOf(false) }
    var bio by remember { mutableStateOf(true) }
    val sections = listOf(
        null to listOf(
            SettingsItem("Require unlock", "App lock", Icons.Default.Lock, toggle = require, onToggle = { require = it }),
            SettingsItem("Automatically lock", "Immediately", Icons.Default.Timer, onClick = { }),
            SettingsItem("Unlock with biometrics", null, Icons.Default.Fingerprint, toggle = bio, onToggle = { bio = it }),
        ),
    )

    AivoraScaffold(title = "App lock", onBack = onBack) {
        InfoBanner("Authenticate to unlock")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyChatLockScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

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

    AivoraScaffold(title = "Chat lock", onBack = onBack) {
        InfoBanner("Unlock locked chats with your secret code.")
        SettingsList(sections)
    }
}


@Composable
fun SettingsPrivacyChatLockSecretCodeScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {

    val sections = listOf(
        null to listOf(
            SettingsItem("Enter secret code", null, Icons.Default.VpnKey, onClick = { }),
            SettingsItem("Confirm secret code", null, Icons.Default.VpnKey, onClick = { }),
            SettingsItem("Save", null, Icons.Default.Check, onClick = { onNavigate(Routes.SettingsPrivacyChatLock) }),
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
            SettingsItem("Personal info", "Last seen, photo, about", Icons.Default.Person, onClick = { onNavigate(Routes.SettingsPrivacyLastSeen) }),
            SettingsItem("Status privacy", null, Icons.Default.Chat, onClick = { onNavigate(Routes.SettingsPrivacyStatus) }),
            SettingsItem("Groups and calls", null, Icons.Default.Groups, onClick = { onNavigate(Routes.SettingsPrivacyGroups) }),
            SettingsItem("App lock", null, Icons.Default.Lock, onClick = { onNavigate(Routes.SettingsPrivacyAppLock) }),
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
            SettingsItem("Stop all live locations", "Live location ended", Icons.Default.LocationOn, onClick = { }),
            SettingsItem("Live location · from another device", null, Icons.Default.Devices, onClick = { }),
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
    var theme by remember { mutableIntStateOf(0) }
    AivoraScaffold(title = "Appearance", onBack = onBack) {
        Spacer(modifier = Modifier.height(8.dp))
        RadioGroup(
            options = listOf(
                "System default" to null,
                "Light" to null,
                "Dark" to null,
            ),
            selected = theme,
            onSelect = { theme = it },
        )
        Text(
            "System default follows your phone's dark theme setting.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SettingsNotificationsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var reactions by remember { mutableStateOf(true) }
    AivoraScaffold(title = "Notifications", onBack = onBack) {
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Notifications", trailing = "On", icon = Icons.Default.Notifications, onClick = { }),
                    SettingsItem("Messages", trailing = "On", icon = Icons.Default.Chat, onClick = { }),
                    SettingsItem("Calls", trailing = "On", icon = Icons.Default.Call, onClick = { }),
                    SettingsItem("Missed calls", trailing = "On", icon = Icons.Default.Phone, onClick = { }),
                ),
            ),
        )
        Text(
            "Sound, vibration and pop-ups are set for each of these in Android's own settings.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem(
                        "Reaction notifications",
                        toggle = reactions,
                        onToggle = { reactions = it },
                        icon = Icons.Default.Notifications,
                    ),
                ),
            ),
        )
        Text(
            "Tell me when someone reacts to my message.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SettingsList(
            listOf(
                null to listOf(
                    SettingsItem("Permissions", null, Icons.Default.Security, onClick = { onNavigate(Routes.SettingsPermissions) }),
                ),
            ),
        )
        Text(
            "If calls or notifications don't arrive, look here first.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
