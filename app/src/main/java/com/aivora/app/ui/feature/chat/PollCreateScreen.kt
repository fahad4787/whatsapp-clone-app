package com.aivora.app.ui.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SettingsItem
import com.aivora.app.ui.components.SettingsRow
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

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
    val canPreview = question.isNotBlank() && options.count { it.isNotBlank() } >= 2

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Create poll", fontWeight = FontWeight.SemiBold)
                    Text(
                        "POLL · $peer",
                        style = MaterialTheme.typography.labelMedium,
                        color = AivoraColors.TextSecondary,
                    )
                }
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            },
            actions = {
                TextButton(onClick = { onBack?.invoke() }) {
                    Text("Cancel", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AivoraColors.BubbleMine.copy(alpha = 0.45f),
            ),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                "Make the plan together",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
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
                shape = RoundedCornerShape(AivoraDimens.RadiusMd),
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
                        if (options.size > 2) {
                            IconButton(onClick = { options.removeAt(index) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(AivoraDimens.RadiusMd),
                    colors = fieldColors(),
                )
            }
            if (options.size < 6) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { options.add("") },
                ) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = AivoraColors.Primary)
                    Spacer(Modifier.size(8.dp))
                    Text("Add option", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted),
            ) {
                SettingsRow(
                    SettingsItem(
                        "Allow multiple answers",
                        subtitle = if (multi) {
                            "Everyone can choose more than one option."
                        } else {
                            "Everyone can choose just one option."
                        },
                        toggle = multi,
                        onToggle = { multi = it },
                    ),
                )
            }
            Text(
                "Names and votes are visible to everyone in this conversation.",
                style = MaterialTheme.typography.bodySmall,
                color = AivoraColors.TextSecondary,
                modifier = Modifier.padding(top = 10.dp, start = 4.dp),
            )
        }

        PrimaryButton(
            text = "Preview poll",
            onClick = {
                val body = buildString {
                    append("📊 Poll: ").append(question.trim())
                    options.filter { it.isNotBlank() }.forEachIndexed { i, opt ->
                        append('\n').append("${i + 1}. ").append(opt.trim())
                    }
                }
                InMemoryChatStore.sendText(chatId, body)
                onBack?.invoke()
            },
            enabled = canPreview,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = AivoraColors.LightSurface,
    focusedContainerColor = AivoraColors.LightSurface,
    unfocusedBorderColor = AivoraColors.Divider,
    focusedBorderColor = AivoraColors.Primary,
)
