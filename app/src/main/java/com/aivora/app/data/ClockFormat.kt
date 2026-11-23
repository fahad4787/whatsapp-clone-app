package com.aivora.app.data

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Clock text that follows the phone's 12 / 24-hour setting, like WhatsApp. */
object ClockFormat {
    private val h24 = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    private val h12 = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    fun format(at: ZonedDateTime): String {
        val is24 = runCatching {
            android.text.format.DateFormat.is24HourFormat(com.aivora.app.AivoraApp.instance)
        }.getOrDefault(false)
        return (if (is24) h24 else h12).format(at)
    }
}
