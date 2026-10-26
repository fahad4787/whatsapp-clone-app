package com.aivora.app.data.local

import com.aivora.app.data.model.StatusRowDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LocalStatus(
    val id: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val mediaUri: String? = null,
    val background: String? = null,
    val timeLabel: String = "Just now",
    val mine: Boolean = false,
    val viewed: Boolean = false,
    val viewCount: Int = 0,
    /** Epoch millis for chronological viewing (oldest → newest like WhatsApp). */
    val createdAtMs: Long = 0L,
)

/**
 * Process-lifetime status feed; hydrated from API + [StatusCache].
 */
object InMemoryStatusStore {
    private val _statuses = MutableStateFlow<List<LocalStatus>>(emptyList())
    val statuses: StateFlow<List<LocalStatus>> = _statuses.asStateFlow()

    @Volatile var usingRemote: Boolean = false
        private set

    fun myStatuses(): List<LocalStatus> =
        _statuses.value.filter { it.mine }

    fun recent(): List<LocalStatus> =
        _statuses.value.filter { !it.mine && !it.viewed }

    fun viewed(): List<LocalStatus> =
        _statuses.value.filter { !it.mine && it.viewed }

    fun get(id: String): LocalStatus? =
        _statuses.value.find { it.id == id }

    /** All statuses for one author, oldest first (WhatsApp playback order). */
    fun statusesForAuthor(authorId: String, mine: Boolean): List<LocalStatus> =
        _statuses.value
            .filter { it.authorId == authorId && it.mine == mine }
            .sortedBy { it.createdAtMs }

    fun replaceAll(list: List<LocalStatus>) {
        usingRemote = true
        _statuses.value = list
    }

    fun hydrateFromCache(list: List<LocalStatus>) {
        if (list.isEmpty() || _statuses.value.isNotEmpty()) return
        usingRemote = true
        _statuses.value = list
    }

    fun markViewed(id: String) {
        _statuses.update { list ->
            list.map { if (it.id == id) it.copy(viewed = true) else it }
        }
    }

    fun prependMine(status: LocalStatus) {
        _statuses.update { listOf(status) + it.filter { s -> s.id != status.id } }
    }

    fun remove(id: String) {
        _statuses.update { it.filter { s -> s.id != id } }
    }

    fun mergeRow(row: StatusRowDto, myUserId: String?) {
        val authorId = row.user?.id ?: return
        val authorName = row.user.displayName?.takeIf { it.isNotBlank() }
            ?: row.user.phone
            ?: "Contact"
        val mine = myUserId != null && authorId == myUserId
        val incoming = (row.statuses.orEmpty()).mapNotNull { item ->
            val id = item.id ?: return@mapNotNull null
            LocalStatus(
                id = id,
                authorId = authorId,
                authorName = if (mine) "My status" else authorName,
                text = item.text.orEmpty().ifBlank {
                    when (item.kind) {
                        "image" -> "Photo"
                        "video" -> "Video"
                        else -> ""
                    }
                },
                mediaUri = item.attachment?.url,
                background = item.background,
                timeLabel = formatStatusTime(item.createdAt),
                mine = mine,
                viewed = item.seen || mine,
                viewCount = item.viewCount,
                createdAtMs = parseCreatedAtMs(item.createdAt),
            )
        }
        if (incoming.isEmpty()) return
        _statuses.update { cur ->
            val withoutAuthor = cur.filter { it.authorId != authorId }
            (incoming + withoutAuthor).distinctBy { it.id }
        }
    }

    fun fromRows(rows: List<StatusRowDto>, myUserId: String?): List<LocalStatus> {
        val out = mutableListOf<LocalStatus>()
        for (row in rows) {
            val authorId = row.user?.id ?: continue
            val authorName = row.user.displayName?.takeIf { it.isNotBlank() }
                ?: row.user.phone
                ?: "Contact"
            val mine = myUserId != null && authorId == myUserId
            for (item in row.statuses.orEmpty()) {
                val id = item.id ?: continue
                out.add(
                    LocalStatus(
                        id = id,
                        authorId = authorId,
                        authorName = if (mine) "My status" else authorName,
                        text = item.text.orEmpty().ifBlank {
                            when (item.kind) {
                                "image" -> "Photo"
                                "video" -> "Video"
                                else -> ""
                            }
                        },
                        mediaUri = item.attachment?.url,
                        background = item.background,
                        timeLabel = formatStatusTime(item.createdAt),
                        mine = mine,
                        viewed = item.seen || mine,
                        viewCount = item.viewCount,
                        createdAtMs = parseCreatedAtMs(item.createdAt),
                    ),
                )
            }
        }
        return out
    }

    private fun parseCreatedAtMs(iso: String?): Long =
        runCatching { Instant.parse(iso).toEpochMilli() }.getOrDefault(0L)

    private fun formatStatusTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "Just now"
        return runCatching {
            val instant = Instant.parse(iso)
            val local = instant.atZone(ZoneId.systemDefault())
            val now = Instant.now().atZone(ZoneId.systemDefault())
            when {
                local.toLocalDate() == now.toLocalDate() ->
                    "Today, " + local.format(DateTimeFormatter.ofPattern("h:mm a"))
                local.toLocalDate() == now.toLocalDate().minusDays(1) -> "Yesterday"
                else -> local.format(DateTimeFormatter.ofPattern("MMM d"))
            }
        }.getOrDefault("Just now")
    }
}
