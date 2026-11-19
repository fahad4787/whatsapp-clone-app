package com.aivora.app.data

import java.util.concurrent.ConcurrentHashMap

/** userId -> display name, learned from conversation members; used to label group senders. */
object SenderNames {
    private val names = ConcurrentHashMap<String, String>()
    fun put(id: String?, name: String?) {
        if (!id.isNullOrBlank() && !name.isNullOrBlank()) names[id] = name
    }
    fun get(id: String?): String? = id?.let { names[it] }
}
