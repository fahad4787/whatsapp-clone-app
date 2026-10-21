package com.aivora.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class LocalStatus(
    val id: String,
    val authorId: String,
    val authorName: String,
    val text: String,
    val mediaUri: String? = null,
    val timeLabel: String = "Just now",
    val mine: Boolean = false,
    val viewed: Boolean = false,
)

/**
 * Process-lifetime status updates. Nothing is written to disk.
 */
object InMemoryStatusStore {
    const val MY_AUTHOR_ID = "me"

    private val _statuses = MutableStateFlow(
        listOf(
            LocalStatus("s1", "1", "Ahmad Khan", "At the café ☕", timeLabel = "Today, 7:20 PM"),
            LocalStatus("s2", "f1", "Fatima Ali", "New recipe night", timeLabel = "Today, 6:05 PM"),
            LocalStatus("s3", "4", "Usman Raza", "Photo", mediaUri = null, timeLabel = "Today, 4:10 PM"),
            LocalStatus("s4", "6", "Zainab Noor", "Busy week ahead", timeLabel = "Today, 2:00 PM"),
            LocalStatus("s5", "7", "Hassan Malik", "Gym done", timeLabel = "Yesterday", viewed = true),
            LocalStatus("s6", "5", "Work Group", "Standup moved to 10", timeLabel = "Yesterday", viewed = true),
        ),
    )
    val statuses: StateFlow<List<LocalStatus>> = _statuses.asStateFlow()

    fun myStatuses(): List<LocalStatus> =
        _statuses.value.filter { it.mine }

    fun recent(): List<LocalStatus> =
        _statuses.value.filter { !it.mine && !it.viewed }

    fun viewed(): List<LocalStatus> =
        _statuses.value.filter { !it.mine && it.viewed }

    fun get(id: String): LocalStatus? =
        _statuses.value.find { it.id == id }

    fun post(text: String, mediaUri: String? = null) {
        val body = text.trim().ifBlank { if (mediaUri != null) "Photo" else return }
        val status = LocalStatus(
            id = "mine-${UUID.randomUUID().toString().take(8)}",
            authorId = MY_AUTHOR_ID,
            authorName = "My status",
            text = body,
            mediaUri = mediaUri,
            timeLabel = "Just now",
            mine = true,
        )
        _statuses.update { listOf(status) + it }
    }

    fun markViewed(id: String) {
        _statuses.update { list ->
            list.map { if (it.id == id) it.copy(viewed = true) else it }
        }
    }
}
