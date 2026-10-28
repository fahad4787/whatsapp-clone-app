package com.aivora.app.ui.feature.calls

import com.aivora.app.ui.icons.AppIcons

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallLog
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.ScheduledCallCreateBody
import com.aivora.app.data.model.ScheduledCallDto
import com.aivora.app.data.model.ScheduledCallListDto
import com.aivora.app.data.model.ScheduledCallRespondBody
import com.aivora.app.data.model.UserDto
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraDatePickerDialog
import com.aivora.app.ui.components.AivoraNoOverscroll
import com.aivora.app.ui.components.AivoraOptionPickerDialog
import com.aivora.app.ui.components.AivoraPickerOption
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.AivoraTimePickerDialog
import com.aivora.app.ui.components.PeopleSelectList
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.SearchField
import com.aivora.app.ui.components.displayLabel
import com.aivora.app.ui.components.rememberPeopleSearch
import com.aivora.app.ui.icons.AivoraIcons
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FieldShape = RoundedCornerShape(AivoraDimens.RadiusSm)
private val DateLabelFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
private val TimeLabelFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val WhenFmt = DateTimeFormatter.ofPattern("EEE, d MMM · h:mm a", Locale.getDefault())

private val ReminderOptions = listOf(
    AivoraPickerOption("0", "None"),
    AivoraPickerOption("5", "5 minutes before"),
    AivoraPickerOption("10", "10 minutes before"),
    AivoraPickerOption("15", "15 minutes before"),
    AivoraPickerOption("30", "30 minutes before"),
    AivoraPickerOption("60", "1 hour before"),
)

private val DurationOptions = listOf(
    AivoraPickerOption("15", "15 minutes"),
    AivoraPickerOption("30", "30 minutes"),
    AivoraPickerOption("45", "45 minutes"),
    AivoraPickerOption("60", "1 hour"),
)

@Composable
fun ScheduledCallsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { NetworkModule.api() }
    val myId = remember { SessionStore.userId(context) }

    var tab by remember { mutableIntStateOf(0) }
    var scheduling by remember { mutableStateOf(false) }
    var upcoming by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryScheduledCallsStore.upcoming.value)
    }
    var past by remember {
        mutableStateOf(com.aivora.app.data.local.InMemoryScheduledCallsStore.past.value)
    }
    var loading by remember {
        mutableStateOf(
            com.aivora.app.data.local.InMemoryScheduledCallsStore.upcoming.value.isEmpty() &&
                com.aivora.app.data.local.InMemoryScheduledCallsStore.past.value.isEmpty(),
        )
    }
    var selected by remember { mutableStateOf<ScheduledCallDto?>(null) }
    var reloadKey by remember { mutableIntStateOf(0) }

    fun reload() {
        reloadKey++
    }

    LaunchedEffect(reloadKey) {
        val warmUp = com.aivora.app.data.local.InMemoryScheduledCallsStore.upcoming.value
        val warmPast = com.aivora.app.data.local.InMemoryScheduledCallsStore.past.value
        if (warmUp.isNotEmpty() || warmPast.isNotEmpty()) {
            upcoming = warmUp
            past = warmPast
            loading = false
        } else {
            loading = true
        }
        val up = runCatching {
            val raw = api.getScheduledCalls("upcoming").string()
            NetworkModule.json.decodeFromString(ScheduledCallListDto.serializer(), raw).items
        }.onFailure { CallLog.w("list upcoming failed", it) }.getOrElse { warmUp }
        val pa = runCatching {
            val raw = api.getScheduledCalls("past").string()
            NetworkModule.json.decodeFromString(ScheduledCallListDto.serializer(), raw).items
        }.onFailure { CallLog.w("list past failed", it) }.getOrElse { warmPast }
        com.aivora.app.data.local.InMemoryScheduledCallsStore.replace(up, pa)
        upcoming = up
        past = pa
        loading = false
        CallLog.d("scheduled list upcoming=${up.size} past=${pa.size}")
    }

    if (scheduling) {
        ScheduleCallForm(
            onBack = { scheduling = false },
            onDone = {
                scheduling = false
                tab = 0
                reload()
            },
        )
        return
    }

    selected?.let { call ->
        ScheduledCallDetail(
            call = call,
            myId = myId,
            onBack = { selected = null },
            onChanged = {
                selected = null
                reload()
            },
            onNavigate = onNavigate,
        )
        return
    }

    AivoraScaffold(title = "Scheduled calls", onBack = onBack) {
        SegmentedTabs(
            options = listOf("Upcoming", "Past"),
            selected = tab,
            onSelect = { tab = it },
        )
        if (tab == 1) {
            Text(
                "All times in PKT · UTC+05:00",
                modifier = Modifier.padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 8.dp),
                color = AivoraColors.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val items = if (tab == 0) upcoming else past
            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading…", color = AivoraColors.TextSecondary)
                    }
                }
                items.isEmpty() -> EmptyScheduleState()
                else -> {
                    AivoraNoOverscroll {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    ) {
                        items(
                            items = items,
                            key = { item ->
                                item.id?.takeIf { it.isNotBlank() }
                                    ?: "sched-${item.startsAt}-${item.title}-${item.organizer?.id}"
                            },
                            contentType = { "sched" },
                        ) { item ->
                            val host = item.organizer?.displayLabel()
                                ?: item.organizer?.displayName
                                ?: "Host"
                            val detail = formatCallDetail(item)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, AivoraColors.BorderMuted, RoundedCornerShape(14.dp))
                                    .clickable { selected = item }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Avatar(host, size = 44)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        item.title ?: "Scheduled call",
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(host, color = AivoraColors.TextSecondary, fontSize = 13.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            AppIcons.Call,
                                            null,
                                            tint = AivoraColors.Primary,
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(detail, color = AivoraColors.TextSecondary, fontSize = 13.sp)
                                    }
                                }
                                Icon(
                                    AppIcons.KeyboardArrowRight,
                                    null,
                                    tint = AivoraColors.TextSecondary,
                                )
                            }
                        }
                        item { Spacer(Modifier.height(12.dp)) }
                    }
                    }
                }
            }
        }
        PrimaryButton(
            text = if (tab == 0 && upcoming.isEmpty()) "Schedule your first call" else "Schedule a call",
            onClick = { scheduling = true },
            iconRes = if (tab == 0 && upcoming.isEmpty()) AivoraIcons.CalendarAdd else null,
            modifier = Modifier.padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 12.dp),
        )
    }
}

@Composable
private fun ScheduledCallDetail(
    call: ScheduledCallDto,
    myId: String?,
    onBack: () -> Unit,
    onChanged: () -> Unit,
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { NetworkModule.api() }
    val isOrganizer = myId != null && (call.organizerId == myId || call.organizer?.id == myId)
    val myStatus = call.myStatus?.lowercase()
    val upcoming = call.status.equals("upcoming", ignoreCase = true)
    val host = call.organizer?.displayLabel() ?: "Host"

    AivoraScaffold(title = call.title ?: "Scheduled call", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AivoraDimens.CardHorizontal),
        ) {
            Text(formatCallDetail(call), color = AivoraColors.TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(8.dp))
            Text("Hosted by $host", color = AivoraColors.InkMuted, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text("Participants", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            call.participants.orEmpty().forEach { p ->
                val label = p.user?.displayLabel() ?: p.userId ?: "User"
                Text(
                    "$label · ${(p.status ?: "pending").lowercase()}",
                    color = AivoraColors.TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 12.dp)) {
            if (upcoming && !isOrganizer && myStatus == "pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "Decline",
                        onClick = {
                            scope.launch {
                                runCatching {
                                    api.postScheduledCallsIdRespond(
                                        call.id!!,
                                        ScheduledCallRespondBody(action = "decline"),
                                    )
                                }.onSuccess {
                                    Toast.makeText(context, "Declined", Toast.LENGTH_SHORT).show()
                                    onChanged()
                                }.onFailure {
                                    Toast.makeText(context, it.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                    PrimaryButton(
                        text = "Accept",
                        onClick = {
                            scope.launch {
                                runCatching {
                                    api.postScheduledCallsIdRespond(
                                        call.id!!,
                                        ScheduledCallRespondBody(action = "accept"),
                                    )
                                }.onSuccess {
                                    Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
                                    onChanged()
                                }.onFailure {
                                    Toast.makeText(context, it.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            if (upcoming) {
                PrimaryButton(
                    text = "Start call",
                    onClick = {
                        scope.launch {
                            val id = call.id ?: return@launch
                            Toast.makeText(context, "Starting…", Toast.LENGTH_SHORT).show()
                            CallLog.i("schedule start id=$id")
                            val result = runCatching { api.postScheduledCallsIdStart(id) }
                                .onFailure {
                                    CallLog.e("schedule start failed id=$id", it)
                                    Toast.makeText(
                                        context,
                                        it.message ?: "Start failed",
                                        Toast.LENGTH_LONG,
                                    ).show()
                                }
                                .getOrNull() ?: return@launch
                            val token = result.token
                            val url = result.livekitUrl
                            val callId = result.call?.id
                            if (token.isNullOrBlank() || url.isNullOrBlank() || callId.isNullOrBlank()) {
                                Toast.makeText(context, "Bad response", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                            val media = call.media ?: if (call.audio) "audio" else "video"
                            val peer = call.title ?: host
                            ActiveCallSession.setActive(
                                callId = callId,
                                token = token,
                                livekitUrl = url,
                                media = media,
                                peerName = peer,
                                asCaller = true,
                            )
                            onChanged()
                            onNavigate(
                                if (media.equals("video", true)) Routes.videoCall(peer)
                                else Routes.audioCall(peer),
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (upcoming && isOrganizer) {
                Spacer(Modifier.height(8.dp))
                PrimaryButton(
                    text = "Cancel",
                    onClick = {
                        scope.launch {
                            runCatching { api.deleteScheduledCallsId(call.id!!) }
                                .onSuccess {
                                    Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT).show()
                                    onChanged()
                                }
                                .onFailure {
                                    Toast.makeText(context, it.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EmptyScheduleState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AivoraDimens.ScreenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AivoraColors.SoftMint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(AivoraIcons.CalendarAdd),
                contentDescription = null,
                tint = AivoraColors.Ink,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "No calls scheduled yet",
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            color = AivoraColors.InkMuted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Make time for your people. Choose who to call and a time that works for you.",
            color = AivoraColors.TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        )
    }
}

@Composable
private fun ScheduleCallForm(onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { NetworkModule.api() }
    val zone = remember { ZoneId.of("Asia/Karachi") }
    val defaultStart = remember {
        LocalDateTime.now(zone).plusHours(1).withMinute(0).withSecond(0).withNano(0)
    }

    var title by remember { mutableStateOf("") }
    var video by remember { mutableStateOf(true) }
    var selectedPeople by remember { mutableStateOf<List<UserDto>>(emptyList()) }
    var date by remember { mutableStateOf(defaultStart.toLocalDate()) }
    var time by remember { mutableStateOf(defaultStart.toLocalTime()) }
    var reminderMinutes by remember { mutableIntStateOf(10) }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var picking by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showReminder by remember { mutableStateOf(false) }
    var showDuration by remember { mutableStateOf(false) }

    val startsAt = remember(date, time) {
        LocalDateTime.of(date, time).atZone(zone).toInstant()
    }
    val reminderLabel = ReminderOptions.find { it.id == reminderMinutes.toString() }?.label
        ?: "$reminderMinutes minutes before"
    val durationLabel = DurationOptions.find { it.id == durationMinutes.toString() }?.label
        ?: "$durationMinutes minutes"
    val canReview = title.isNotBlank() && selectedPeople.isNotEmpty() && !saving

    if (picking) {
        ParticipantPicker(
            selected = selectedPeople,
            onBack = { picking = false },
            onDone = { selectedPeople = it; picking = false },
        )
        return
    }

    if (showDate) {
        AivoraDatePickerDialog(
            initial = date,
            onDismiss = { showDate = false },
            onConfirm = { date = it },
        )
    }
    if (showTime) {
        AivoraTimePickerDialog(
            initial = time,
            onDismiss = { showTime = false },
            onConfirm = { time = it },
        )
    }
    if (showReminder) {
        AivoraOptionPickerDialog(
            title = "Reminder",
            options = ReminderOptions,
            selectedId = reminderMinutes.toString(),
            onDismiss = { showReminder = false },
            onSelect = { reminderMinutes = it.id.toIntOrNull() ?: 10 },
        )
    }
    if (showDuration) {
        AivoraOptionPickerDialog(
            title = "Duration",
            options = DurationOptions,
            selectedId = durationMinutes.toString(),
            onDismiss = { showDuration = false },
            onSelect = { durationMinutes = it.id.toIntOrNull() ?: 30 },
        )
    }

    AivoraScaffold(title = "Schedule a call", onBack = onBack) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AivoraDimens.CardHorizontal),
        ) {
            Text(
                "Plan an audio or video call with your contacts.",
                color = Color(0xFF72748D),
                fontSize = 16.sp,
                lineHeight = 22.sp,
                modifier = Modifier.padding(bottom = 20.dp),
            )

            FieldLabel("Call title")
            MutedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = "Weekend catch-up",
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Participants")
            FormRow(
                icon = AppIcons.PersonAdd,
                label = if (selectedPeople.isEmpty()) {
                    "Add participants"
                } else {
                    selectedPeople.joinToString { it.displayLabel() }
                },
                placeholder = selectedPeople.isEmpty(),
                onClick = { picking = true },
            )
            Text(
                "Choose one or more contacts",
                color = Color(0xFF72748D),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(16.dp))

            Text(
                "Call type",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = AivoraColors.Ink,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CallTypeCard(
                    label = "Audio",
                    icon = AppIcons.Call,
                    selected = !video,
                    modifier = Modifier.weight(1f),
                    onClick = { video = false },
                )
                CallTypeCard(
                    label = "Video",
                    icon = AppIcons.Videocam,
                    selected = video,
                    modifier = Modifier.weight(1f),
                    onClick = { video = true },
                )
            }
            Spacer(Modifier.height(16.dp))

            FieldLabel("Date")
            FormRow(
                icon = AppIcons.Event,
                label = date.format(DateLabelFmt),
                placeholder = false,
                onClick = { showDate = true },
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Time")
            FormRow(
                icon = AppIcons.AccessTime,
                label = time.format(TimeLabelFmt),
                placeholder = false,
                onClick = { showTime = true },
            )
            Text(
                "PKT · UTC+05:00",
                color = Color(0xFF72748D),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Duration")
            FormRow(
                icon = AppIcons.Timer,
                label = durationLabel,
                placeholder = false,
                onClick = { showDuration = true },
            )
            Spacer(Modifier.height(16.dp))

            FieldLabel("Reminder")
            FormRow(
                icon = AppIcons.Notifications,
                label = reminderLabel,
                placeholder = false,
                onClick = { showReminder = true },
            )
            Spacer(Modifier.height(8.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AivoraDimens.CardHorizontal)
                .clip(FieldShape)
                .background(AivoraColors.SoftMint)
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                AppIcons.Info,
                contentDescription = null,
                tint = AivoraColors.InfoGreen,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Add participants and choose a date and time to continue. Invitations are sent after you confirm.",
                color = AivoraColors.InfoGreen,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
        PrimaryButton(
            text = if (saving) "Scheduling…" else "Schedule call",
            onClick = {
                if (!canReview) return@PrimaryButton
                if (startsAt.isBefore(Instant.now().plusSeconds(60))) {
                    Toast.makeText(context, "Pick a time at least 1 minute from now", Toast.LENGTH_SHORT).show()
                    return@PrimaryButton
                }
                scope.launch {
                    saving = true
                    val inviteeIds = selectedPeople.mapNotNull { it.id }
                    CallLog.i(
                        "schedule create title=${title.trim()} media=${if (video) "video" else "audio"} " +
                            "startsAt=$startsAt duration=$durationMinutes reminder=$reminderMinutes " +
                            "invitees=${inviteeIds.size}",
                    )
                    val body = ScheduledCallCreateBody(
                        title = title.trim(),
                        media = if (video) "video" else "audio",
                        startsAt = startsAt.toString(),
                        durationMinutes = durationMinutes,
                        reminderMinutes = reminderMinutes,
                        userIds = inviteeIds,
                    )
                    runCatching { api.postScheduledCalls(body) }
                        .onSuccess { created ->
                            CallLog.i("schedule create ok id=${created.id}")
                            Toast.makeText(context, "Scheduled", Toast.LENGTH_SHORT).show()
                            onDone()
                        }
                        .onFailure {
                            CallLog.e("schedule create failed", it)
                            Toast.makeText(
                                context,
                                it.message ?: "Could not schedule",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    saving = false
                }
            },
            enabled = canReview,
            modifier = Modifier.padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = 12.dp),
        )
    }
}

@Composable
private fun ParticipantPicker(
    selected: List<UserDto>,
    onBack: () -> Unit,
    onDone: (List<UserDto>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf(selected.associateBy { it.id.orEmpty() }) }
    val search = rememberPeopleSearch(query)

    AivoraScaffold(
        title = "Add participants",
        onBack = onBack,
        actions = {
            Text(
                "Done",
                color = AivoraColors.Primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onDone(chosen.values.filter { it.id != null }) }
                    .padding(horizontal = 16.dp),
            )
        },
    ) {
        SearchField(query, { query = it }, "Search people")
        PeopleSelectList(
            people = search.people,
            selectedIds = chosen.keys.filter { it.isNotBlank() }.toSet(),
            onToggle = { id ->
                val person = search.people.find { it.id == id } ?: return@PeopleSelectList
                chosen = if (chosen.containsKey(id)) {
                    chosen - id
                } else {
                    chosen + (id to person)
                }
            },
            modifier = Modifier.weight(1f),
        )
    }
}

private fun formatCallDetail(item: ScheduledCallDto): String {
    val zone = ZoneId.of("Asia/Karachi")
    val whenLabel = item.startsAt?.let {
        runCatching { WhenFmt.withZone(zone).format(Instant.parse(it)) }.getOrNull()
    } ?: "Soon"
    val media = when {
        item.media.equals("video", true) -> "Video"
        item.audio -> "Audio"
        else -> "Audio"
    }
    val mins = item.durationMinutes.takeIf { it > 0 } ?: 30
    return "$whenLabel · $media · $mins min"
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        color = AivoraColors.TextSecondary,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun MutedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(AivoraColors.SurfaceMuted)
            .border(1.dp, AivoraColors.BorderMuted, FieldShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(placeholder, color = Color(0xFF72748D), fontSize = 16.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = AivoraColors.InkMuted, fontSize = 16.sp),
            cursorBrush = SolidColor(AivoraColors.Primary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FormRow(
    icon: ImageVector,
    label: String,
    placeholder: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(AivoraColors.SurfaceMuted)
            .border(1.dp, AivoraColors.BorderMuted, FieldShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = AivoraColors.InkMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = if (placeholder) Color(0xFF72748D) else AivoraColors.InkMuted,
        )
        Icon(
            AppIcons.KeyboardArrowRight,
            null,
            tint = AivoraColors.TextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun CallTypeCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(72.dp)
            .clip(FieldShape)
            .background(if (selected) AivoraColors.SoftMint else AivoraColors.SurfaceMuted)
            .border(
                1.dp,
                if (selected) AivoraColors.Primary else AivoraColors.BorderMuted,
                FieldShape,
            )
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = AivoraColors.InkMuted, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AivoraColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcons.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(1.5.dp, AivoraColors.Divider, CircleShape),
            )
        }
    }
}

@Composable
private fun SegmentedTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AivoraDimens.ScreenHorizontal, vertical = AivoraDimens.Space2)
            .height(44.dp)
            .clip(RoundedCornerShape(AivoraDimens.RadiusSm))
            .background(AivoraColors.SurfaceMuted)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            val on = selected == index
            val interaction = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(
                        if (on) {
                            Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(4.dp),
                                    ambientColor = Color(0xFF292341).copy(alpha = 0.06f),
                                    spotColor = Color(0xFF292341).copy(alpha = 0.06f),
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                        } else {
                            Modifier.clip(RoundedCornerShape(4.dp))
                        },
                    )
                    .clickable(interactionSource = interaction, indication = null) { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontWeight = if (on) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (on) AivoraColors.InkMuted else AivoraColors.TextSecondary,
                )
            }
        }
    }
}
