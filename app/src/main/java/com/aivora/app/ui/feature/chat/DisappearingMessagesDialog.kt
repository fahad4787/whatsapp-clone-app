package com.aivora.app.ui.feature.chat

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.ChatRepository
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.DisappearingBody
import com.aivora.app.data.toLocalChat
import com.aivora.app.ui.theme.AivoraColors
import kotlinx.coroutines.launch
import retrofit2.HttpException

private val DisappearingOptions = listOf(
    0 to "Off",
    86_400 to "24 hours",
    604_800 to "7 days",
    7_776_000 to "90 days",
)

fun formatDisappearing(seconds: Int): String =
    DisappearingOptions.firstOrNull { it.first == seconds }?.second
        ?: when {
            seconds <= 0 -> "Off"
            seconds % 86_400 == 0 -> "${seconds / 86_400} days"
            seconds % 3_600 == 0 -> "${seconds / 3_600} hours"
            else -> "${seconds / 60} minutes"
        }

/** Radio dialog for per-chat disappearing timer; PUTs to the API and refreshes [InMemoryChatStore]. */
@Composable
fun DisappearingMessagesDialog(
    chatId: String,
    currentSeconds: Int,
    onDismiss: () -> Unit,
    onUpdated: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { ChatRepository.api() }
    val useRemote = !SessionStore.accessToken(context).isNullOrBlank()
    var saving by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Disappearing messages", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "New messages in this chat disappear after the timer. Already-sent messages stay.",
                    color = AivoraColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                DisappearingOptions.forEach { (seconds, label) ->
                    val pick = {
                        when {
                            saving -> Unit
                            seconds == currentSeconds -> onDismiss()
                            !useRemote -> {
                                Toast.makeText(context, "Sign in to change this setting", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                saving = true
                                scope.launch {
                                    runCatching {
                                        api.putConversationsIdDisappearing(chatId, DisappearingBody(seconds = seconds))
                                    }.onSuccess { dto ->
                                        InMemoryChatStore.upsertChat(dto.toLocalChat())
                                        onUpdated()
                                        onDismiss()
                                    }.onFailure { err ->
                                        saving = false
                                        val code = (err as? HttpException)?.code()
                                        val msg = when (code) {
                                            404 -> "Chat not found"
                                            401, 403 -> "Please sign in again"
                                            else -> "Could not update timer"
                                        }
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !saving) { pick() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = seconds == currentSeconds,
                            onClick = { pick() },
                            enabled = !saving,
                            colors = RadioButtonDefaults.colors(selectedColor = AivoraColors.Primary),
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !saving) {
                Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        },
    )
}
