package com.aivora.app.data.local

import com.aivora.app.data.model.CallHistoryItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-lifetime call history so the Calls tab paints instantly on revisit
 * instead of flashing "Loading…" on every select.
 */
object InMemoryCallHistoryStore {
    private val _items = MutableStateFlow<List<CallHistoryItemDto>>(emptyList())
    val items: StateFlow<List<CallHistoryItemDto>> = _items.asStateFlow()

    fun replace(list: List<CallHistoryItemDto>) {
        _items.value = list
    }

    fun clear() {
        _items.value = emptyList()
    }
}
