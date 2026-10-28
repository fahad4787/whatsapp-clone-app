package com.aivora.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

/** Shared Aivora-styled date picker (same chrome as Create event). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AivoraDatePickerDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    AivoraTheme(darkTheme = false) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            onConfirm(
                                Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate(),
                            )
                        }
                        onDismiss()
                    },
                ) {
                    Text("OK", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = AivoraColors.TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = AivoraColors.LightSurface),
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
}

/** Shared Aivora-styled time picker (same chrome as Create event). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AivoraTimePickerDialog(
    initial: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    title: String = "Select time",
) {
    AivoraTheme(darkTheme = false) {
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
                        title,
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
                            onClick = {
                                onConfirm(LocalTime.of(state.hour, state.minute))
                                onDismiss()
                            },
                        ) {
                            Text("OK", color = AivoraColors.Primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

data class AivoraPickerOption(
    val id: String,
    val label: String,
)

/** Simple single-select list dialog (reminders, duration, etc.). */
@Composable
fun AivoraOptionPickerDialog(
    title: String,
    options: List<AivoraPickerOption>,
    selectedId: String?,
    onDismiss: () -> Unit,
    onSelect: (AivoraPickerOption) -> Unit,
) {
    AivoraTheme(darkTheme = false) {
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
                        title,
                        fontWeight = FontWeight.SemiBold,
                        color = AivoraColors.Ink,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 360.dp)
                            .padding(horizontal = 8.dp),
                    ) {
                        items(options, key = { it.id }) { opt ->
                            val selected = opt.id == selectedId
                            Text(
                                opt.label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) AivoraColors.PrimarySoft else Color.Transparent,
                                    )
                                    .clickable {
                                        onSelect(opt)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 14.dp),
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) AivoraColors.PrimaryDark else AivoraColors.Ink,
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
}
