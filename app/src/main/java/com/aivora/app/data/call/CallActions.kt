package com.aivora.app.data.call

import com.aivora.app.AivoraApp
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.model.CallDto
import com.aivora.app.data.push.MessageNotifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Call state changes that must happen even when no call screen is being drawn:
 * Compose stops recomposing while the app is in the background.
 */
object CallActions {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false

    private val finalStatuses = setOf("ended", "rejected", "missed")

    /** Starts the call timer as soon as both sides are connected. */
    fun install() {
        if (started) return
        started = true
        scope.launch {
            combine(ActiveCallSession.answered, LiveKitCallController.remoteJoined) { a, b -> a && b }
                .collect { live -> if (live) ActiveCallSession.markLive() }
        }
    }

    /** Tells the server this phone is ringing for [callId], so the caller sees "Ringing". */
    fun reportRinging(callId: String?) {
        if (callId.isNullOrBlank()) return
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { NetworkModule.api().postCallsIdAction(callId, "ringing") }
            }.onFailure { CallLog.w("ringing api failed callId=$callId", it) }
        }
    }

    /** Hang up from outside the call screen (notification button, app swiped away). */
    fun hangUp() {
        scope.launch {
            val callId = ActiveCallSession.session.value?.callId
            LiveKitCallController.disconnect()
            if (callId == null) return@launch
            CallLog.i("hangUp outside call screen callId=$callId")
            ActiveCallSession.markEnded("ended")
            runCatching {
                withContext(Dispatchers.IO) { NetworkModule.api().postCallsIdAction(callId, "end") }
            }.onFailure { CallLog.w("end api failed callId=$callId", it) }
        }
    }

    /** The server says [call] is over: stop ringing and leave the room. */
    fun applyRemoteEnd(call: CallDto?, rejected: Boolean = false) {
        val reason = when {
            rejected -> "rejected"
            call?.endReason.equals("rejected", true) -> "rejected"
            call?.endReason.equals("cancelled", true) -> "cancelled"
            call?.endReason.equals("timeout", true) -> "timeout"
            call?.status.equals("missed", true) -> "missed"
            else -> "ended"
        }
        call?.id?.let { id ->
            runCatching { MessageNotifier.cancelCall(AivoraApp.instance, id) }
        }
        scope.launch {
            val activeId = ActiveCallSession.session.value?.callId
            val incomingId = ActiveCallSession.incoming.value?.id
            val matchesIncoming = incomingId != null && (call?.id == null || call.id == incomingId)
            val matchesActive = activeId != null && (call?.id == null || call.id == activeId)
            CallLog.i(
                "remote end id=${call?.id} reason=$reason " +
                    "matchIncoming=$matchesIncoming matchActive=$matchesActive",
            )
            // Always tear down local UI so one side never stays on the call screen alone.
            if (matchesIncoming) ActiveCallSession.clearIncoming()
            if (matchesActive) {
                LiveKitCallController.disconnect()
                ActiveCallSession.markEnded(reason)
            }
        }
    }

    /**
     * Catches up on "answered" / "ended" events missed while the socket was down or the
     * process was asleep, so nobody is left ringing or "Calling" on a dead call.
     */
    fun resync() {
        val ids = listOfNotNull(
            ActiveCallSession.session.value?.callId,
            ActiveCallSession.incoming.value?.id,
        ).distinct()
        ids.forEach { id ->
            scope.launch {
                val call = runCatching {
                    withContext(Dispatchers.IO) {
                        val body = NetworkModule.api().getCallsId(id).string()
                        NetworkModule.json.decodeFromString(CallDto.serializer(), body)
                    }
                }.onFailure { CallLog.w("resync failed callId=$id", it) }.getOrNull()
                    ?: return@launch
                val status = call.status?.lowercase()
                val session = ActiveCallSession.session.value
                when {
                    status in finalStatuses -> applyRemoteEnd(call)
                    status == "active" && session?.callId == id && session.asCaller -> {
                        ActiveCallSession.markAnswered()
                        LiveKitCallController.enableMedia(session.media == "video")
                    }
                }
            }
        }
    }
}
