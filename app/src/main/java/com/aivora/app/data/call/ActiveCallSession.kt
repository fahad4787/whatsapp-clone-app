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

    /** True once callee answered (or we joined as callee). */
    private val _answered = MutableStateFlow(false)
    val answered: StateFlow<Boolean> = _answered.asStateFlow()

    /** Terminal event for in-call UI (rejected / ended / missed). */
    private val _endedReason = MutableStateFlow<String?>(null)
    val endedReason: StateFlow<String?> = _endedReason.asStateFlow()

    fun setActive(
        callId: String,
        token: String,
        livekitUrl: String,
        media: String,
        peerName: String,
        asCaller: Boolean,
    ) {
        _incoming.value = null
        _endedReason.value = null
        // Callee only reaches here after Accept → already answered.
        _answered.value = !asCaller
        _session.value = Session(
            callId = callId,
            token = token,
            livekitUrl = livekitUrl,
            media = media.lowercase(),
            peerName = peerName,
            asCaller = asCaller,
        )
        CallLog.i(
            "active callId=$callId media=${media.lowercase()} peer=$peerName " +
                "role=${if (asCaller) "caller" else "callee"}",
        )
    }

    fun markAnswered() {
        if (!_answered.value) {
            CallLog.i("answered callId=${_session.value?.callId}")
        }
        _answered.value = true
    }

    fun clear() {
        val id = _session.value?.callId
        _session.value = null
        _answered.value = false
        if (id != null) CallLog.d("cleared session callId=$id")
    }

    fun setIncoming(call: CallDto?) {
        _incoming.value = call
        if (call != null) {
            CallLog.i(
                "incoming callId=${call.id} media=${call.media} " +
                    "group=${call.groupTitle} from=${call.caller?.name}",
            )
        }
    }

    fun clearIncoming() {
        val id = _incoming.value?.id
        _incoming.value = null
        if (id != null) CallLog.d("cleared incoming callId=$id")
    }

    fun markEnded(reason: String) {
        val id = _session.value?.callId ?: _incoming.value?.id
        CallLog.i("ended callId=$id reason=$reason")
        _endedReason.value = reason
        _incoming.value = null
        _session.value = null
        _answered.value = false
    }

    fun consumeEndedReason(): String? {
        val r = _endedReason.value
        _endedReason.value = null
        return r
    }
}
