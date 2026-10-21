package com.aivora.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Holds a just-captured image URI until a screen consumes it (status composer, etc.). */
object PendingMedia {
    private val _uri = MutableStateFlow<String?>(null)
    val uri: StateFlow<String?> = _uri.asStateFlow()

    fun set(value: String?) {
        _uri.value = value
    }

    fun consume(): String? {
        val value = _uri.value
        _uri.value = null
        return value
    }
}
