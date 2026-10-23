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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Disappearing messages", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                DisappearingOptions.forEach { (seconds, label) ->
                    val pick = {
                        onDismiss()
                        if (seconds != currentSeconds) {
                            if (!useRemote) {
                                Toast.makeText(context, "Sign in to change this setting", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    runCatching { api.putConversationsIdDisappearing(chatId, DisappearingBody(seconds)) }
                                        .onSuccess {
                                            InMemoryChatStore.upsertChat(it.toLocalChat())
                                            onUpdated()
                                        }
                                        .onFailure {
                                            Toast.makeText(context, "Could not update timer", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pick() }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = seconds == currentSeconds,
                            onClick = { pick() },
                            colors = RadioButtonDefaults.colors(selectedColor = AivoraColors.Primary),
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        },
    )
}
