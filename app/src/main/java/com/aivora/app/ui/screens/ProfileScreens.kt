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

@Composable
fun ProfileIdScreen(
    profileId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val chat = com.aivora.app.ui.mock.MockData.chat(profileId.ifBlank { "1" })
    AivoraScaffold(title = "Contact info", onBack = onBack) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(chat.title, size = 96)
            Spacer(Modifier.height(12.dp))
            Text(chat.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
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
