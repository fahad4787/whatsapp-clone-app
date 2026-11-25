package com.aivora.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.Locale

/** Clock text that follows the phone's 12 / 24-hour setting, like WhatsApp. */
object ClockFormat {
    private val h24 = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val h12 = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    private val day = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

    fun format(at: TemporalAccessor): String {
        val is24 = runCatching {
            android.text.format.DateFormat.is24HourFormat(com.aivora.app.AivoraApp.instance)
        }.getOrDefault(false)
        return (if (is24) h24 else h12).format(at)
    }

    /** A server timestamp in the phone's time zone: "Today, 8:00 PM" or "Nov 23, 8:00 PM". */
    fun dayAndTime(iso: String?): String? {
        val instant = runCatching { Instant.parse(iso) }.getOrNull() ?: return null
        val local = instant.atZone(ZoneId.systemDefault())
        val today = LocalDate.now(local.zone)
        val label = when (local.toLocalDate()) {
            today -> "Today"
            today.plusDays(1) -> "Tomorrow"
            today.minusDays(1) -> "Yesterday"
            else -> day.format(local)
        }
        return "$label, ${format(local)}"
    }
}
