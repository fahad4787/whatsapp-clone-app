package com.aivora.app.ui.feature.chat

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.model.MessageInfoDto
import com.aivora.app.data.model.MessageReceiptDto
import com.aivora.app.ui.components.SectionHeader
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
fun MessageInfoChatMessageScreen(
    chatId: String = "",
    messageId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var info by remember(messageId) { mutableStateOf<MessageInfoDto?>(null) }
    var failed by remember(messageId) { mutableStateOf(false) }
    LaunchedEffect(messageId) {
        info = null
        failed = false
        runCatching { ChatRepository.api().getMessagesIdInfo(messageId) }
            .onSuccess { info = it }
            .onFailure { failed = true }
    }
    AivoraScaffold(title = "Message info", onBack = onBack) {
        val loaded = info
        when {
            failed -> EmptyState(
                title = "Couldn't load info",
                body = "Check your connection and try again.",
                icon = Icons.Default.Info,
            )
            loaded == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AivoraColors.Primary)
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                receiptSection("Read by", "No one has read this yet", loaded.read)
                receiptSection("Delivered to", "Not delivered to anyone yet", loaded.delivered)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.receiptSection(
    title: String,
    emptyText: String,
    receipts: List<MessageReceiptDto>,
) {
    item { SectionHeader(title) }
    if (receipts.isEmpty()) {
        item {
            Text(
                emptyText,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    } else {
        items(receipts) { r ->
            val name = r.displayName?.takeIf { it.isNotBlank() } ?: "Unknown"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(name, size = 40)
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(name, fontWeight = FontWeight.SemiBold, color = AivoraColors.Ink)
                    formatReceiptTime(r.at)?.let {
                        Text(it, color = AivoraColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun formatReceiptTime(iso: String?): String? {
    val at = runCatching { java.time.Instant.parse(iso) }.getOrNull() ?: return null
    val zone = java.time.ZoneId.systemDefault()
    val time = at.atZone(zone)
    val clock = java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(time)
    return if (time.toLocalDate() == java.time.LocalDate.now(zone)) {
        "Today, $clock"
    } else {
        java.time.format.DateTimeFormatter.ofPattern("MMM d").format(time) + ", " + clock
    }
}
