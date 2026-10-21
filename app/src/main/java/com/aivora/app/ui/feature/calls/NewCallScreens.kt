package com.aivora.app.ui.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.SeedData
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.CreateCallLinkDialog
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@Composable
fun NewCallScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var showLink by remember { mutableStateOf(false) }
    var keypad by remember { mutableStateOf(false) }
    var dial by remember { mutableStateOf("") }
    val people = remember(query) {
        SeedData.contacts.filter {
            query.isBlank() || it.first.contains(query, true) || it.second.contains(query, true)
        }
    }
    val peer = selected ?: dial.ifBlank { "Contact" }

    if (showLink) {
        CreateCallLinkDialog(onDismiss = { showLink = false })
    }

    if (keypad) {
        KeypadScreen(
            dial = dial,
            onDialChange = { dial = it },
            onBack = { keypad = false },
            onVoice = { onNavigate(Routes.audioCall(dial.ifBlank { "Unknown" })) },
            onVideo = { onNavigate(Routes.videoCall(dial.ifBlank { "Unknown" })) },
        )
        return
    }

    AivoraScaffold(
        title = "New call",
        onBack = onBack,
        titleContent = {
            Column {
                Text("New call", fontWeight = FontWeight.Bold)
                Text(
                    "Pick someone to call",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        SearchField(query, { query = it }, "Search contacts")
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                CallActionRow(
                    icon = Icons.Default.Dialpad,
                    title = "Dial a number",
                    subtitle = "Call a number that isn't in your contacts",
                    onClick = { keypad = true },
                )
                CallActionRow(
                    icon = Icons.Default.Link,
                    title = "Create call link",
                    subtitle = "Share a link anyone can join, without a chat",
                    onClick = { showLink = true },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(people, key = { it.first }) { (name, about) ->
                val on = selected == name
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selected = if (on) null else name }
                        .background(if (on) AivoraColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(name, size = 44)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text(about, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            BottomCallPill(
                icon = Icons.Default.Call,
                label = "Voice call",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Routes.audioCall(peer)) },
            )
            BottomCallPill(
                icon = Icons.Default.Videocam,
                label = "Video call",
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(Routes.videoCall(peer)) },
            )
        }
    }
}

@Composable
private fun KeypadScreen(
    dial: String,
    onDialChange: (String) -> Unit,
    onBack: () -> Unit,
    onVoice: () -> Unit,
    onVideo: () -> Unit,
) {
    val keys = listOf(
        "1" to "", "2" to "ABC", "3" to "DEF",
        "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
        "*" to "", "0" to "+", "#" to "",
    )

    AivoraScaffold(title = "Keypad", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = AivoraDimens.ScreenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                dial.ifBlank { "Enter number" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (dial.isBlank()) AivoraColors.TextTertiary else AivoraColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(28.dp))
            keys.chunked(3).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    row.forEach { (digit, letters) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable { onDialChange(dial + digit) }
                                .padding(8.dp),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(digit, fontSize = 28.sp, fontWeight = FontWeight.Medium, color = AivoraColors.Ink)
                            if (letters.isNotEmpty()) {
                                Text(letters, fontSize = 10.sp, color = AivoraColors.TextSecondary)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVideo) {
                    Icon(Icons.Default.Videocam, contentDescription = "Video", tint = AivoraColors.Primary)
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AivoraColors.Primary)
                        .clickable(onClick = onVoice),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = AivoraColors.OnPrimary, modifier = Modifier.size(28.dp))
                }
                IconButton(
                    onClick = { if (dial.isNotEmpty()) onDialChange(dial.dropLast(1)) },
                    enabled = dial.isNotEmpty(),
                ) {
                    Icon(
                        Icons.Default.Backspace,
                        contentDescription = "Delete",
                        tint = if (dial.isNotEmpty()) AivoraColors.Ink else AivoraColors.TextTertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CallActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AivoraColors.Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = AivoraColors.Primary)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BottomCallPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(AivoraColors.Primary.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold)
    }
}
