package com.aivora.app.ui.components

import com.aivora.app.ui.icons.AppIcons

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.aivora.app.ui.theme.AivoraColors

data class OverflowMenuItem(
    val label: String,
    val icon: ImageVector,
    val danger: Boolean = false,
    val onClick: () -> Unit,
)

/** Screenshot-style overflow: white card, tinted icon circles, soft shadow. */
@Composable
fun AivoraOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    items: List<OverflowMenuItem>,
    offset: DpOffset = DpOffset((-8).dp, 0.dp),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = offset,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
        properties = PopupProperties(focusable = true),
        modifier = Modifier.widthIn(min = 220.dp),
    ) {
        items.forEachIndexed { index, item ->
            OverflowRow(item = item, onDismiss = onDismiss)
            if (index < items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun OverflowRow(item: OverflowMenuItem, onDismiss: () -> Unit) {
    val accent = if (item.danger) AivoraColors.Danger else AivoraColors.Primary
    val labelColor = if (item.danger) AivoraColors.Danger else MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onDismiss()
                item.onClick()
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(item.icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            item.label,
            color = labelColor,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun CreateCallLinkDialog(
    onDismiss: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val api = remember { com.aivora.app.data.ChatRepository.api() }
    var video by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var createdUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    fun createThen(action: (String) -> Unit) {
        val existing = createdUrl
        if (existing != null) {
            action(existing)
            onDismiss()
            return
        }
        if (busy) return
        busy = true
        error = null
        scope.launch {
            runCatching {
                api.postCallLinks(
                    com.aivora.app.data.model.CallLinkCreateBody(
                        media = if (video) "video" else "audio",
                    ),
                )
            }.onSuccess { dto ->
                val url = dto.url?.takeIf { it.isNotBlank() }
                    ?: dto.code?.let {
                        "https://whatsapp-clone-backend-hep1.onrender.com/call/$it"
                    }
                if (url.isNullOrBlank()) {
                    error = "Bad response"
                } else {
                    createdUrl = url
                    action(url)
                    onDismiss()
                }
            }.onFailure {
                error = it.message ?: "Could not create link"
            }
            busy = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Create call link", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Anyone with the link can join this call — no chat needed.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CallTypePill(
                        label = "Voice",
                        icon = AppIcons.Call,
                        selected = !video,
                        onClick = { if (!busy && createdUrl == null) video = false },
                        modifier = Modifier.weight(1f),
                    )
                    CallTypePill(
                        label = "Video",
                        icon = AppIcons.Videocam,
                        selected = video,
                        onClick = { if (!busy && createdUrl == null) video = true },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (error != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    createThen { url ->
                        val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, url)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(send, "Share call link"),
                        )
                    }
                },
                enabled = !busy,
            ) {
                Text(
                    if (busy) "Creating…" else "Share link",
                    color = AivoraColors.Primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    createThen { url ->
                        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                        cm.setPrimaryClip(android.content.ClipData.newPlainText("Call link", url))
                        android.widget.Toast.makeText(context, "Link copied", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !busy,
            ) {
                Text("Copy link", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        },
    )
}

@Composable
fun ClearCallLogDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Clear call log?", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "This removes call history from this device. It can’t be undone.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(); onDismiss() }) {
                Text("Clear", color = AivoraColors.Danger, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
    )
}

@Composable
private fun CallTypePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) AivoraColors.Primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            )
            .then(
                if (selected) Modifier.border(1.dp, AivoraColors.Primary.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(22.dp)),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, null, tint = AivoraColors.Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
    }
}
