package com.aivora.app.ui.feature.chat

import com.aivora.app.ui.icons.AppIcons

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.data.local.LocalContact
import com.aivora.app.data.local.LocalEvent
import com.aivora.app.data.local.LocalLocation
import com.aivora.app.data.local.LocalPoll
import com.aivora.app.ui.components.Avatar
import com.aivora.app.ui.theme.AivoraColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TimeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val EventFmt = DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a", Locale.getDefault())
private val DayFmt = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

/** Short event time like "Sat, Oct 24 · 7:30 PM"; falls back to the raw string. */
internal fun formatEventStart(iso: String): String = runCatching {
    EventFmt.format(Instant.parse(iso).atZone(ZoneId.systemDefault()))
}.getOrDefault(iso)

/** Presence subtitle from an ISO last-seen timestamp (Today / Yesterday / date). */
internal fun formatLastSeen(iso: String): String = runCatching {
    val zoned = Instant.parse(iso).atZone(ZoneId.systemDefault())
    val today = LocalDate.now(zoned.zone)
    // Compact so chat header stays one line next to call/video (Figma Contact controls).
    when (zoned.toLocalDate()) {
        today -> "last seen today at ${TimeFmt.format(zoned)}"
        today.minusDays(1) -> "last seen yesterday at ${TimeFmt.format(zoned)}"
        else -> "last seen ${DayFmt.format(zoned)}"
    }
}.getOrDefault("")

@Composable
internal fun PollBubble(
    poll: LocalPoll,
    onVote: (optionId: String) -> Unit,
) {
    Column(modifier = Modifier.width(220.dp)) {
        Text(
            poll.question,
            color = AivoraColors.Ink,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            when {
                poll.closed -> "Poll closed"
                poll.multiple -> "Select one or more"
                else -> "Select one"
            },
            color = AivoraColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp),
        )
        poll.options.forEach { opt ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (opt.mine) AivoraColors.Primary.copy(alpha = 0.18f)
                        else AivoraColors.SurfaceMuted,
                    )
                    .clickable(enabled = !poll.closed) { onVote(opt.id) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    (if (opt.mine) "✓ " else "") + opt.text,
                    color = AivoraColors.Ink,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (opt.mine) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    opt.votes.toString(),
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Text(
            "${poll.totalVoters} ${if (poll.totalVoters == 1) "vote" else "votes"}",
            color = AivoraColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
internal fun LocationBubble(location: LocalLocation) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(220.dp)
            .clickable {
                val label = Uri.encode(location.name ?: "Location")
                val geo = Uri.parse("geo:${location.lat},${location.lng}?q=${location.lat},${location.lng}($label)")
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, geo)) }
                    .onFailure {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://www.google.com/maps/search/?api=1&query=${location.lat},${location.lng}"),
                                ),
                            )
                        }
                    }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFE8F0E9), Color(0xFFD6E4F0), Color(0xFFF0F0F0)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.LocationOn,
                contentDescription = "Location",
                tint = AivoraColors.Primary,
                modifier = Modifier.size(36.dp),
            )
        }
        Text(
            location.name?.takeIf { it.isNotBlank() } ?: "Location",
            color = AivoraColors.Ink,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        location.address?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                color = AivoraColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "%.4f, %.4f".format(Locale.US, location.lat, location.lng),
            color = AivoraColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
internal fun ContactBubble(contact: LocalContact) {
    Row(
        modifier = Modifier.width(220.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(contact.name.ifBlank { "?" }, size = 40)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                contact.name,
                color = AivoraColors.Ink,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            contact.phones.forEach {
                Text(
                    it,
                    color = AivoraColors.TextSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
internal fun EventBubble(
    event: LocalEvent,
    onRsvp: (answer: String) -> Unit,
) {
    Column(modifier = Modifier.width(220.dp)) {
        Text(
            event.title,
            color = AivoraColors.Ink,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (event.cancelled) {
            Text(
                "Cancelled",
                color = AivoraColors.Danger,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Text(
            formatEventStart(event.startsAt),
            color = AivoraColors.TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(top = 2.dp),
        )
        event.location?.takeIf { it.isNotBlank() }?.let {
            Text(
                it,
                color = AivoraColors.TextSecondary,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "${event.going} going · ${event.maybe} maybe · ${event.notGoing} can't",
            color = AivoraColors.TextSecondary,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
        )
        if (!event.cancelled) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                RsvpChip("Going", event.myAnswer == "going", Modifier.weight(1f)) { onRsvp("going") }
                RsvpChip("Maybe", event.myAnswer == "maybe", Modifier.weight(1f)) { onRsvp("maybe") }
                RsvpChip("Can't", event.myAnswer == "not_going", Modifier.weight(1f)) { onRsvp("not_going") }
            }
        }
    }
}

@Composable
private fun RsvpChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) AivoraColors.PrimaryDark else AivoraColors.Ink,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        fontSize = 12.sp,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) AivoraColors.Primary.copy(alpha = 0.22f)
                else AivoraColors.SurfaceMuted,
            )
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        textAlign = TextAlign.Center,
    )
}
