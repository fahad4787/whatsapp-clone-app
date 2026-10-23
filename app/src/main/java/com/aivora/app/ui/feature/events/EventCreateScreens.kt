package com.aivora.app.ui.feature.events

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.EventCreateBody
import com.aivora.app.data.model.SendMessageBody
import com.aivora.app.data.toLocalMessage
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import com.aivora.app.ui.theme.AivoraTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

private val DateLabelFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
private val TimeLabelFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

private val CommonZones = listOf(
    "Asia/Karachi",
    "Asia/Dubai",
    "Asia/Kolkata",
    "Asia/Shanghai",
    "Asia/Tokyo",
    "Europe/London",
    "Europe/Paris",
    "Europe/Berlin",
    "America/New_York",
    "America/Chicago",
    "America/Denver",
    "America/Los_Angeles",
    "Australia/Sydney",
    "UTC",
)

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
    val deviceZone = remember { ZoneId.systemDefault() }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var zoneId by remember { mutableStateOf(deviceZone) }
    var location by remember { mutableStateOf("") }
    var type by remember { mutableIntStateOf(0) }
    var sending by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showStartTime by remember { mutableStateOf(false) }
    var showEndTime by remember { mutableStateOf(false) }
    var showZone by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canSend = title.isNotBlank() && date != null && startTime != null

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
            PickerField(
                value = date?.format(DateLabelFmt).orEmpty(),
                placeholder = "Choose a date",
                trailing = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AivoraColors.TextSecondary) },
                onClick = { showDate = true },
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Start time *")
                    PickerField(
                        value = startTime?.format(TimeLabelFmt).orEmpty(),
                        placeholder = "Start",
                        trailing = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = AivoraColors.TextSecondary) },
                        onClick = { showStartTime = true },
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("End time")
                    PickerField(
                        value = endTime?.format(TimeLabelFmt).orEmpty(),
                        placeholder = "End",
                        trailing = { Icon(Icons.Default.AccessTime, contentDescription = null, tint = AivoraColors.TextSecondary) },
                        onClick = { showEndTime = true },
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            FieldLabel("Timezone")
            PickerField(
                value = formatZoneLabel(zoneId),
                placeholder = "Timezone",
                trailing = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AivoraColors.TextSecondary) },
                onClick = { showZone = true },
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
            text = "Send event",
            onClick = {
                val d = date ?: return@PrimaryButton
                val start = startTime ?: return@PrimaryButton
                val uid = SessionStore.userId(context)
                val startsAt = LocalDateTime.of(d, start).atZone(zoneId).toInstant()
                val endsAt = endTime?.let { end ->
                    LocalDateTime.of(d, end).atZone(zoneId).toInstant().takeIf { it.isAfter(startsAt) }
                }
                if (chatId.isBlank() || SessionStore.accessToken(context).isNullOrBlank() || uid == null) {
                    if (chatId.isNotBlank()) {
                        InMemoryChatStore.sendText(
                            chatId,
                            "📅 Event: ${title.trim()} · ${d.format(DateLabelFmt)} ${start.format(TimeLabelFmt)}" +
                                if (location.isNotBlank()) " · $location" else "",
                        )
                    }
                    onBack?.invoke()
                } else if (!sending) {
                    sending = true
                    scope.launch {
                        runCatching {
                            NetworkModule.api().postConversationsIdMessages(
                                chatId,
                                SendMessageBody(
                                    clientId = UUID.randomUUID().toString(),
                                    event = EventCreateBody(
                                        title = title.trim(),
                                        description = description.trim().ifBlank { null },
                                        location = location.trim().ifBlank { null },
                                        startsAt = startsAt.toString(),
                                        endsAt = endsAt?.toString(),
                                        withCall = type == 1,
                                        timezone = zoneId.id,
                                    ),
                                ),
                            )
                        }.onSuccess {
                            InMemoryChatStore.upsertMessage(chatId, it.toLocalMessage(uid))
                            onBack?.invoke()
                        }.onFailure {
                            sending = false
                            Toast.makeText(context, "Could not send event", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            enabled = canSend && !sending,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
    }

    // Force light Aivora chrome so pickers don't follow system dark / purple defaults.
    if (showDate || showStartTime || showEndTime || showZone) {
        AivoraTheme(darkTheme = false) {
            if (showDate) {
                val state = rememberDatePickerState(
                    initialSelectedDateMillis = (date ?: LocalDate.now())
                        .atStartOfDay(ZoneOffset.UTC)
                        .toInstant()
                        .toEpochMilli(),
                )
                DatePickerDialog(
                    onDismissRequest = { showDate = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                state.selectedDateMillis?.let { millis ->
                                    date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                                }
                                showDate = false
                            },
                        ) { Text("OK", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDate = false }) {
                            Text("Cancel", color = AivoraColors.TextSecondary)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = AivoraColors.LightSurface,
                    ),
                ) {
                    DatePicker(
                        state = state,
                        colors = DatePickerDefaults.colors(
                            containerColor = AivoraColors.LightSurface,
                            selectedDayContainerColor = AivoraColors.Primary,
                            selectedDayContentColor = Color.White,
                            todayDateBorderColor = AivoraColors.Primary,
                            selectedYearContainerColor = AivoraColors.Primary,
                            selectedYearContentColor = Color.White,
                        ),
                    )
                }
            }
            if (showStartTime) {
                EventTimePickerDialog(
                    initial = startTime ?: LocalTime.now().withSecond(0).withNano(0),
                    onDismiss = { showStartTime = false },
                    onConfirm = {
                        startTime = it
                        showStartTime = false
                    },
                )
            }
            if (showEndTime) {
                EventTimePickerDialog(
                    initial = endTime
                        ?: (startTime?.plusHours(1) ?: LocalTime.now().plusHours(1).withSecond(0).withNano(0)),
                    onDismiss = { showEndTime = false },
                    onConfirm = {
                        endTime = it
                        showEndTime = false
                    },
                )
            }
            if (showZone) {
                TimezonePickerDialog(
                    selected = zoneId,
                    onDismiss = { showZone = false },
                    onSelect = {
                        zoneId = it
                        showZone = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false,
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AivoraColors.LightSurface,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Select time",
                    fontWeight = FontWeight.SemiBold,
                    color = AivoraColors.Ink,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                )
                TimePicker(
                    state = state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = AivoraColors.SurfaceMuted,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = AivoraColors.Ink,
                        selectorColor = AivoraColors.Primary,
                        containerColor = AivoraColors.LightSurface,
                        periodSelectorBorderColor = AivoraColors.BorderMuted,
                        periodSelectorSelectedContainerColor = AivoraColors.PrimarySoft,
                        periodSelectorUnselectedContainerColor = AivoraColors.SurfaceMuted,
                        periodSelectorSelectedContentColor = AivoraColors.PrimaryDark,
                        periodSelectorUnselectedContentColor = AivoraColors.TextSecondary,
                        timeSelectorSelectedContainerColor = AivoraColors.PrimarySoft,
                        timeSelectorUnselectedContainerColor = AivoraColors.SurfaceMuted,
                        timeSelectorSelectedContentColor = AivoraColors.PrimaryDark,
                        timeSelectorUnselectedContentColor = AivoraColors.Ink,
                    ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AivoraColors.TextSecondary)
                    }
                    TextButton(
                        onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                    ) {
                        Text("OK", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimezonePickerDialog(
    selected: ZoneId,
    onDismiss: () -> Unit,
    onSelect: (ZoneId) -> Unit,
) {
    val zones = remember {
        val ids = linkedSetOf(ZoneId.systemDefault().id)
        ids.addAll(CommonZones)
        ids.map { ZoneId.of(it) }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AivoraColors.LightSurface,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .heightIn(max = 480.dp),
        ) {
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                Text(
                    "Timezone",
                    fontWeight = FontWeight.SemiBold,
                    color = AivoraColors.Ink,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 360.dp)
                        .padding(horizontal = 8.dp),
                ) {
                    items(zones, key = { it.id }) { z ->
                        val selectedRow = z.id == selected.id
                        Text(
                            formatZoneLabel(z),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedRow) AivoraColors.PrimarySoft else Color.Transparent)
                                .clickable { onSelect(z) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            fontWeight = if (selectedRow) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedRow) AivoraColors.PrimaryDark else AivoraColors.Ink,
                        )
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 8.dp),
                ) {
                    Text("Cancel", color = AivoraColors.TextSecondary)
                }
            }
        }
    }
}

private fun formatZoneLabel(zone: ZoneId): String {
    val now = Instant.now()
    val offset = zone.rules.getOffset(now)
    val offsetLabel = offset.id.replace("Z", "+00:00")
    val short = zone.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    return "${zone.id} · $short ($offsetLabel)"
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
private fun PickerField(
    value: String,
    placeholder: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AivoraDimens.RadiusMd))
            .clickable(onClick = onClick),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            placeholder = { Text(placeholder) },
            singleLine = true,
            readOnly = true,
            enabled = false,
            trailingIcon = trailing,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AivoraDimens.RadiusMd),
            colors = OutlinedTextFieldDefaults.colors(
                disabledContainerColor = AivoraColors.LightSurface,
                disabledTextColor = AivoraColors.Ink,
                disabledBorderColor = AivoraColors.Divider,
                disabledPlaceholderColor = AivoraColors.TextTertiary,
                disabledTrailingIconColor = AivoraColors.TextSecondary,
            ),
        )
    }
}

@Composable
private fun EventField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    trailing: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = if (placeholder.isNotEmpty()) ({ Text(placeholder) }) else null,
        singleLine = singleLine,
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
