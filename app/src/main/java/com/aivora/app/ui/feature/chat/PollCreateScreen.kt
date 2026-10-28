package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.PollCreateBody
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.aivoraSystemNavClearance
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollCreateScreen(
    chatId: String = "1",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val chat = remember(chatId) { InMemoryChatStore.chat(chatId) }
    val peer = chat.title.substringBefore(" ").ifBlank { "contact" }
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }
    var multi by remember { mutableStateOf(false) }
    var sending by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canPreview = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
    val navClearance = aivoraSystemNavClearance()
    val fieldShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.SurfaceMuted)
            .imePadding(),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Create poll", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    Text(
                        "POLL · ${chat.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AivoraColors.TextSecondary,
                        fontSize = 12.sp,
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(AppIcons.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                TextButton(onClick = { onBack?.invoke() }) {
                    Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
            ),
        )
        HorizontalDivider(color = AivoraColors.BorderMuted, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
        ) {
            Text(
                "Make the plan together",
                fontWeight = FontWeight.SemiBold,
                fontSize = 23.sp,
                color = AivoraColors.Ink,
                lineHeight = 29.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ask $peer a question and add at least two answers.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
            )
            Spacer(Modifier.height(20.dp))
            FieldLabel("Question")
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                placeholder = { Text("What would you like to ask?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = fieldShape,
                colors = fieldColors(),
            )
            options.forEachIndexed { index, value ->
                Spacer(Modifier.height(14.dp))
                FieldLabel("Option ${index + 1}")
                OutlinedTextField(
                    value = value,
                    onValueChange = { options[index] = it },
                    placeholder = { Text("Add an answer") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (options.size > 2) options.removeAt(index)
                            },
                            enabled = options.size > 2,
                        ) {
                            Icon(
                                AppIcons.Close,
                                contentDescription = "Remove",
                                tint = if (options.size > 2) {
                                    AivoraColors.Ink
                                } else {
                                    AivoraColors.TextTertiary
                                },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = fieldColors(),
                )
            }
            if (options.size < 6) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { options.add("") },
                ) {
                    Icon(
                        AppIcons.AddCircleOutline,
                        contentDescription = null,
                        tint = AivoraColors.Primary,
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        "Add option",
                        color = AivoraColors.Primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, AivoraColors.BorderMuted, RoundedCornerShape(16.dp))
                    .padding(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Allow multiple answers",
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        color = AivoraColors.Ink,
                    )
                    Switch(
                        checked = multi,
                        onCheckedChange = { multi = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = AivoraColors.Primary,
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = AivoraColors.BorderMuted,
                            uncheckedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (multi) {
                        "Everyone can choose more than one option."
                    } else {
                        "Everyone can choose just one option."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = AivoraColors.TextSecondary,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Names and votes are visible to everyone in this conversation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = AivoraColors.TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }

        // Figma “Screen actions” + home-indicator clearance (same as bottom nav).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
        ) {
            HorizontalDivider(color = AivoraColors.BorderMuted, thickness = 1.dp)
            PrimaryButton(
                text = "Send poll",
                onClick = {
                    val q = question.trim()
                    val opts = options.filter { it.isNotBlank() }.map { it.trim() }
                    val uid = SessionStore.userId(context)
                    if (SessionStore.accessToken(context).isNullOrBlank() || uid == null) {
                        // Offline demo fallback
                        val body = buildString {
                            append("📊 Poll: ").append(q)
                            opts.forEachIndexed { i, opt -> append('\n').append("${i + 1}. ").append(opt) }
                        }
                        InMemoryChatStore.sendText(chatId, body)
                        onBack?.invoke()
                    } else {
                        sending = true
                        scope.launch {
                            runCatching {
                                NetworkModule.api().postConversationsIdMessages(
                                    chatId,
                                    SendMessageBody(
                                        clientId = UUID.randomUUID().toString(),
                                        poll = PollCreateBody(q, opts, multi),
                                    ),
                                )
                            }.onSuccess {
                                InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
                                onBack?.invoke()
                            }.onFailure {
                                sending = false
                                Toast.makeText(context, "Could not send poll", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = canPreview && !sending,
                modifier = Modifier.padding(start = 24.dp, top = 12.dp, end = 24.dp, bottom = 16.dp),
            )
            Spacer(Modifier.height(navClearance))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = AivoraColors.Ink,
        modifier = Modifier.padding(bottom = 7.dp),
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.White,
    focusedContainerColor = Color.White,
    unfocusedBorderColor = AivoraColors.BorderMuted,
    focusedBorderColor = AivoraColors.Primary,
)
