package com.aivora.app.data.local

import com.aivora.app.data.model.ChatListDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory custom chat lists (Wave 42). */
object ChatListsStore {
    private val _lists = MutableStateFlow<List<ChatListDto>>(emptyList())
    val lists: StateFlow<List<ChatListDto>> = _lists.asStateFlow()

    fun replace(items: List<ChatListDto>) {
        _lists.value = items.sortedBy { it.sortOrder }
    }

    fun upsert(list: ChatListDto) {
        val without = _lists.value.filterNot { it.id == list.id }
        _lists.value = (without + list).sortedBy { it.sortOrder }
    }

    fun remove(id: String) {
        _lists.value = _lists.value.filterNot { it.id == id }
    }

    fun get(id: String): ChatListDto? = _lists.value.find { it.id == id }
}
