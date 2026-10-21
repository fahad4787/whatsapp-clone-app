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
import com.aivora.app.ui.mock.MockData
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    val people = MockData.contacts.filter {
        query.isBlank() || it.first.contains(query, true) || it.second.contains(query, true)
    }
    AivoraScaffold(
        title = "New chat",
        onBack = onBack,
        fab = { 
        FloatingActionButton(
            onClick = { onNavigate(Routes.NewGroup) },
            containerColor = AivoraColors.Primary,
            contentColor = AivoraColors.OnPrimary,
        ) { Icon(Icons.Default.Add, contentDescription = null) }
 },
    ) {
        InfoBanner("Allow access to your contacts")
        SearchField(query, { query = it }, "Search")
        LazyColumn {
            item {
                PersonRow(
                    name = "New group",
                    subtitle = "Create a group with friends",
                    onClick = { onNavigate(Routes.NewGroup) },
                )
                PersonRow(
                    name = "New contact",
                    subtitle = "Add someone by number",
                    onClick = { onNavigate(Routes.SettingsAccountAdd) },
                )
            }
            items(people) { (n, s) ->
                val id = (MockData.chats.find { it.title == n }?.id)
                    ?: (kotlin.math.abs(n.hashCode()) % 1000).toString()
                PersonRow(
                    name = n,
                    subtitle = s,
                    onClick = { onNavigate(Routes.ChatId.replace("{id}", id)) },
                )
            }
        }
    }
}
