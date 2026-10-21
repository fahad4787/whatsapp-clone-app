package com.aivora.app.ui.feature.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.SeedData
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.CreateCallLinkDialog
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.theme.AivoraColors

@Composable
fun NewCallScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<String?>(null) }
    var showLink by remember { mutableStateOf(false) }
    val people = remember(query) {
        SeedData.contacts.filter {
            query.isBlank() || it.first.contains(query, true) || it.second.contains(query, true)
        }
    }

    if (showLink) {
        CreateCallLinkDialog(onDismiss = { showLink = false })
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
                    onClick = { },
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
                onClick = { onBack?.invoke() },
            )
            BottomCallPill(
                icon = Icons.Default.Videocam,
                label = "Video call",
                modifier = Modifier.weight(1f),
                onClick = { onBack?.invoke() },
            )
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
