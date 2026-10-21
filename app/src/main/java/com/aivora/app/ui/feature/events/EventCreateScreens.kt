package com.aivora.app.ui.feature.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventCreateChatScreen(
    chatId: String = "",
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val peer = remember(chatId) {
        if (chatId.isBlank()) "chat"
        else InMemoryChatStore.chat(chatId).title.substringBefore(" ").ifBlank { "chat" }
    }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableIntStateOf(0) } // 0 in person, 1 call
    val canPreview = title.isNotBlank() && date.isNotBlank() && startTime.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("Create event", fontWeight = FontWeight.SemiBold)
                    Text(
                        "EVENT · $peer",
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = AivoraColors.LightBackground),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                "Put the plan on the calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(16.dp))
            FieldLabel("Event title *")
            EventField(value = title, onValueChange = { title = it }, placeholder = "Give your event a name")
            Spacer(Modifier.height(12.dp))
            FieldLabel("Description")
            EventField(
                value = description,
                onValueChange = { description = it },
                placeholder = "What’s the plan? (optional)",
                singleLine = false,
                minHeight = 88,
            )
            Spacer(Modifier.height(12.dp))
            FieldLabel("Date *")
            EventField(
                value = date,
                onValueChange = { date = it },
                placeholder = "Choose a date",
                trailing = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AivoraColors.TextSecondary) },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Start time *")
                    EventField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        placeholder = "Start",
                        trailing = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = AivoraColors.TextSecondary) },
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("End time")
                    EventField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        placeholder = "End",
                        trailing = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = AivoraColors.TextSecondary) },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel("Timezone")
            EventField(
                value = "Europe/London · BST (UTC+01:00)",
                onValueChange = { },
                placeholder = "",
                readOnly = true,
                trailing = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AivoraColors.TextSecondary) },
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
                    .background(AivoraColors.SurfaceMuted)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TypeChip("In person", selected = type == 0, modifier = Modifier.weight(1f)) { type = 0 }
                TypeChip("Call", selected = type == 1, modifier = Modifier.weight(1f)) { type = 1 }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel("Location")
            EventField(
                value = location,
                onValueChange = { location = it },
                placeholder = "Add a place or address",
                trailing = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = AivoraColors.TextSecondary) },
            )
            Spacer(Modifier.height(16.dp))
        }

        PrimaryButton(
            text = "Preview event",
            onClick = {
                if (chatId.isNotBlank()) {
                    InMemoryChatStore.sendText(
                        chatId,
                        "📅 Event: ${title.trim()} · $date $startTime" +
                            if (location.isNotBlank()) " · $location" else "",
                    )
                }
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
private fun EventField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    readOnly: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        singleLine = singleLine,
        readOnly = readOnly,
        trailingIcon = trailing,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (minHeight > 0) Modifier.height(minHeight.dp) else Modifier),
        shape = RoundedCornerShape(AivoraDimens.RadiusMd),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AivoraColors.LightSurface,
            focusedContainerColor = AivoraColors.LightSurface,
            unfocusedBorderColor = AivoraColors.Divider,
            focusedBorderColor = AivoraColors.Primary,
        ),
    )
}

@Composable
private fun TypeChip(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Text(
        label,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) AivoraColors.PrimaryDark else AivoraColors.TextSecondary,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .then(
                if (selected) {
                    Modifier
                        .background(AivoraColors.BubbleMine)
                        .border(1.dp, AivoraColors.Primary, RoundedCornerShape(AivoraDimens.RadiusMd))
                } else {
                    Modifier.background(AivoraColors.LightSurface)
                },
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    )
}
