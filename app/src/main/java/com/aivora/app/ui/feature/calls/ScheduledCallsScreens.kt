package com.aivora.app.ui.feature.calls

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.components.AivoraScaffold
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens

@Composable
fun ScheduledCallsScreen(
    onBack: (() -> Unit)? = null,
    onNavigate: (String) -> Unit = {},
) {
    var tab by remember { mutableIntStateOf(0) }
    var scheduling by remember { mutableStateOf(false) }
    var pastItems by remember {
        mutableStateOf(
            listOf(
                Triple("Weekend", "Babar CH", "3:00 AM · Audio · 30 min"),
            ),
        )
    }

    if (scheduling) {
        ScheduleCallForm(
            onBack = { scheduling = false },
            onDone = {
                pastItems = listOf(Triple("Catch-up", "You", "Soon · Audio · 30 min")) + pastItems
                tab = 0
                scheduling = false
            },
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (tab == 0) {
                EmptyScheduleState(onSchedule = { scheduling = true })
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Text(
                        "Saturday, 10 October",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 10.dp),
                    )
                    pastItems.forEach { (title, host, detail) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(host, size = 44)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, fontWeight = FontWeight.SemiBold)
                                Text(host, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Call, null, tint = AivoraColors.Primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
        PrimaryButton(
            text = if (tab == 0) "Schedule your first call" else "Schedule a call",
            onClick = { scheduling = true },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun EmptyScheduleState(onSchedule: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onSchedule)
            .padding(horizontal = AivoraDimens.ScreenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF9F1)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.aivora.app.ui.icons.AivoraIcons.CalendarAdd),
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
    var title by remember { mutableStateOf("") }
    var audio by remember { mutableStateOf(true) }
    val canReview = title.isNotBlank()

    AivoraScaffold(
        title = "Schedule a call",
        onBack = onBack,
        titleContent = {
            Column {
                Text("Schedule a call", fontWeight = FontWeight.Bold)
                Text(
                    "Plan an audio or video call with your contacts.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            FieldLabel("Call title")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Weekend catch-up") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(14.dp))
            FieldLabel("Participants")
            FormRow(Icons.Default.PersonAdd, "Add participants") { }
            Text("Choose one or more contacts", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text("Call type", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CallTypeCard("Audio", Icons.Default.Call, audio, Modifier.weight(1f)) { audio = true }
                CallTypeCard("Video", Icons.Default.Videocam, !audio, Modifier.weight(1f)) { audio = false }
            }
            Spacer(modifier = Modifier.height(14.dp))
            FieldLabel("Date & time")
            FormRow(Icons.Default.Event, "Choose date & time") { }
            Text("PKT · UTC+05:00 · 30-minute call", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            FieldLabel("Reminder")
            FormRow(Icons.Default.Notifications, "10 minutes before") { }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AivoraColors.Primary.copy(alpha = 0.12f))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(Icons.Default.Info, null, tint = AivoraColors.PrimaryDark)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add participants and choose a date and time to continue. Invitations are sent after you confirm.",
                    color = AivoraColors.PrimaryDark,
                    fontSize = 13.sp,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
        PrimaryButton(
            text = "Review call",
            onClick = { if (canReview) onDone() },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            enabled = canReview,
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

@Composable
private fun FormRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(0.5f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CallTypeCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AivoraColors.Primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(
                1.5.dp,
                if (selected) AivoraColors.Primary else Color.Transparent,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (selected) AivoraColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.CheckCircle, null, tint = AivoraColors.Primary, modifier = Modifier.size(20.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (on) Color.White else Color.Transparent)
                    .clickable { onSelect(index) },
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
