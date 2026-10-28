package com.aivora.app.data.local

import com.aivora.app.data.model.BlockItemDto
import com.aivora.app.data.model.ScheduledCallDto
import com.aivora.app.data.model.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/** Clear process-lifetime list caches on logout. */
fun clearInMemoryListCaches() {
    InMemoryPeopleStore.clear()
    InMemoryScheduledCallsStore.clear()
    InMemoryMediaGalleryStore.clear()
    InMemoryStarredStore.clear()
    InMemoryBlockedStore.clear()
    InMemoryMeStore.clear()
    InMemoryPrivacyStore.clear()
    InMemoryConversationDetailStore.clear()
    InMemoryMessageInfoStore.clear()
}

/**
 * People directory for New chat / New call / New group / privacy pickers.
 * Blank-query directory + per-query cache so reopen never blanks.
 */
object InMemoryPeopleStore {
    private val _directory = MutableStateFlow<List<UserDto>>(emptyList())
    val directory: StateFlow<List<UserDto>> = _directory.asStateFlow()
    private val byQuery = ConcurrentHashMap<String, List<UserDto>>()

    fun cached(query: String): List<UserDto>? = byQuery[query.trim().lowercase()]

    fun put(query: String, list: List<UserDto>) {
        val key = query.trim().lowercase()
        byQuery[key] = list
        if (key.isEmpty()) _directory.value = list
    }

    fun clear() {
        byQuery.clear()
        _directory.value = emptyList()
    }
}

object InMemoryScheduledCallsStore {
    private val _upcoming = MutableStateFlow<List<ScheduledCallDto>>(emptyList())
    private val _past = MutableStateFlow<List<ScheduledCallDto>>(emptyList())
    val upcoming: StateFlow<List<ScheduledCallDto>> = _upcoming.asStateFlow()
    val past: StateFlow<List<ScheduledCallDto>> = _past.asStateFlow()

    fun replace(upcoming: List<ScheduledCallDto>, past: List<ScheduledCallDto>) {
        _upcoming.value = upcoming
        _past.value = past
    }

    fun clear() {
        _upcoming.value = emptyList()
        _past.value = emptyList()
    }
}

/** Per chatId:tab media/docs/links gallery. */
object InMemoryMediaGalleryStore {
    private val cache = ConcurrentHashMap<String, List<LocalMessage>>()

    private fun key(chatId: String, tab: Int) = "$chatId:$tab"

    fun get(chatId: String, tab: Int): List<LocalMessage>? = cache[key(chatId, tab)]

    fun put(chatId: String, tab: Int, items: List<LocalMessage>) {
        cache[key(chatId, tab)] = items
    }

    fun clear() = cache.clear()
}

object InMemoryStarredStore {
    private val _items = MutableStateFlow<List<LocalMessage>>(emptyList())
    val items: StateFlow<List<LocalMessage>> = _items.asStateFlow()

    fun replace(list: List<LocalMessage>) {
        _items.value = list
    }

    fun clear() {
        _items.value = emptyList()
    }
}

object InMemoryBlockedStore {
    private val _items = MutableStateFlow<List<BlockItemDto>>(emptyList())
    val items: StateFlow<List<BlockItemDto>> = _items.asStateFlow()

    fun replace(list: List<BlockItemDto>) {
        _items.value = list
    }

    fun removeId(id: String) {
        _items.value = _items.value.filterNot { it.id == id }
    }

    fun clear() {
        _items.value = emptyList()
    }
}

/** Last GET /api/me/privacy so radios don't flash wrong default. */
object InMemoryPrivacyStore {
    @Volatile
    var dto: com.aivora.app.data.model.PrivacySettingsDto? = null
        private set

    fun replace(value: com.aivora.app.data.model.PrivacySettingsDto) {
        dto = value
    }

    fun clear() {
        dto = null
    }
}

/** Last ConversationDto per chatId for Group info instant paint. */
object InMemoryConversationDetailStore {
    private val byId = java.util.concurrent.ConcurrentHashMap<String, com.aivora.app.data.model.ConversationDto>()

    fun get(chatId: String): com.aivora.app.data.model.ConversationDto? = byId[chatId]

    fun put(chatId: String, dto: com.aivora.app.data.model.ConversationDto) {
        byId[chatId] = dto
    }

    fun clear() = byId.clear()
}

/** Message info receipts cache — reopen without spinner wipe. */
object InMemoryMessageInfoStore {
    private val byId = java.util.concurrent.ConcurrentHashMap<String, com.aivora.app.data.model.MessageInfoDto>()

    fun get(messageId: String): com.aivora.app.data.model.MessageInfoDto? = byId[messageId]

    fun put(messageId: String, dto: com.aivora.app.data.model.MessageInfoDto) {
        byId[messageId] = dto
    }

    fun clear() = byId.clear()
}

/** Last known profile for Your Details instant paint. */
object InMemoryMeStore {
    data class Me(
        val displayName: String = "",
        val about: String = "",
        val phone: String = "",
        val avatarUrl: String? = null,
    )

    @Volatile
    var me: Me? = null
        private set

    fun replace(displayName: String?, about: String?, phone: String?, avatarUrl: String?) {
        me = Me(
            displayName = displayName.orEmpty(),
            about = about.orEmpty(),
            phone = phone.orEmpty(),
            avatarUrl = avatarUrl,
        )
    }

    fun clear() {
        me = null
    }
}
