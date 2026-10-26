package com.aivora.app.data.call

import com.aivora.app.data.model.CallDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** In-memory call session — tokens are too large for nav args. */
object ActiveCallSession {
    data class Session(
        val callId: String,
        val token: String,
        val livekitUrl: String,
        val media: String, // audio | video
        val peerName: String,
        val asCaller: Boolean,
    )

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session.asStateFlow()

    private val _incoming = MutableStateFlow<CallDto?>(null)
    val incoming: StateFlow<CallDto?> = _incoming.asStateFlow()

    fun setActive(
        callId: String,
        token: String,
        livekitUrl: String,
        media: String,
        peerName: String,
        asCaller: Boolean,
    ) {
        _incoming.value = null
        _session.value = Session(
            callId = callId,
            token = token,
            livekitUrl = livekitUrl,
            media = media.lowercase(),
            peerName = peerName,
            asCaller = asCaller,
        )
    }

    fun clear() {
        _session.value = null
    }

    fun setIncoming(call: CallDto?) {
        _incoming.value = call
    }

    fun clearIncoming() {
        _incoming.value = null
    }
}
