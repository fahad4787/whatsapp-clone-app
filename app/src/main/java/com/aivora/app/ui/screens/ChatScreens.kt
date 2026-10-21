package com.aivora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.mock.MockData
import com.aivora.app.ui.mock.MockMessage
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatIdScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val chat = remember(chatId) { MockData.chat(chatId) }
    val messages = remember(chatId) {
        mutableStateListOf<MockMessage>().also { it.addAll(MockData.messagesFor(chatId)) }
    }
    var text by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    AivoraScaffold(
        title = chat.title,
        onBack = onBack,
        actions = {
            IconButton(onClick = { onNavigate(Routes.NewCall) }) { Icon(Icons.Default.Videocam, contentDescription = "Video call") }
            IconButton(onClick = { onNavigate(Routes.NewCall) }) { Icon(Icons.Default.Call, contentDescription = "Voice call") }
            IconButton(onClick = { onNavigate(Routes.ProfileId.replace("{id}", chatId)) }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
        },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
        ) {
            items(messages, key = { it.id }) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.mine) Arrangement.End else Arrangement.Start,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (msg.mine) AivoraColors.Primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            msg.body,
                            color = if (msg.mine) AivoraColors.OnPrimary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            msg.time,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (msg.mine) AivoraColors.OnPrimary.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End).padding(top = 2.dp),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { onNavigate(Routes.EventCreateChat.replace("{chat}", chatId)) }) {
                Icon(Icons.Default.Add, contentDescription = "Attach", tint = AivoraColors.Primary)
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { }) { Icon(Icons.Default.AttachFile, contentDescription = "File") }
                        IconButton(onClick = { onNavigate(Routes.NewStatus) }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        }
                    }
                },
            )
            IconButton(
                onClick = {
                    val body = text.trim()
                    if (body.isNotEmpty()) {
                        messages.add(
                            MockMessage(
                                id = "local-${messages.size + 1}",
                                mine = true,
                                body = body,
                                time = "Now",
                            ),
                        )
                        text = ""
                    }
                },
            ) {
                Icon(
                    if (text.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                    contentDescription = if (text.isBlank()) "Voice" else "Send",
                    tint = AivoraColors.Primary,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LinkButton("Poll") {
                onNavigate(
                    Routes.MessageInfoChatMessage
                        .replace("{chat}", chatId)
                        .replace("{message}", "1"),
                )
            }
            LinkButton("Event") { onNavigate(Routes.EventCreateChat.replace("{chat}", chatId)) }
            LinkButton("Info") {
                onNavigate(
                    Routes.MessageInfoChatMessage
                        .replace("{chat}", chatId)
                        .replace("{message}", messages.lastOrNull()?.id ?: "1"),
                )
            }
        }
    }
}
