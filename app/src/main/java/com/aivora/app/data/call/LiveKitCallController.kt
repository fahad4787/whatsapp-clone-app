package com.aivora.app.data.call

import android.content.Context
import android.media.AudioManager
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Thin LiveKit room wrapper for Wave 16.1 in-call screens. */
object LiveKitCallController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var room: Room? = null
    private var eventsJob: Job? = null
    private var appContext: Context? = null
    private var connectGeneration = 0

    /** Enable video after connect if [enableMedia] raced ahead of publisher setup. */
    private var pendingEnableVideo: Boolean? = null

    private val _roomConnected = MutableStateFlow(false)
    val roomConnected: StateFlow<Boolean> = _roomConnected.asStateFlow()

    /** True when at least one remote participant is in the LiveKit room. */
    private val _remoteJoined = MutableStateFlow(false)
    val remoteJoined: StateFlow<Boolean> = _remoteJoined.asStateFlow()

    private val _remoteVideo = MutableStateFlow<VideoTrack?>(null)
    val remoteVideo: StateFlow<VideoTrack?> = _remoteVideo.asStateFlow()

    private val _localVideo = MutableStateFlow<VideoTrack?>(null)
    val localVideo: StateFlow<VideoTrack?> = _localVideo.asStateFlow()

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted.asStateFlow()

    private val _cameraOff = MutableStateFlow(false)
    val cameraOff: StateFlow<Boolean> = _cameraOff.asStateFlow()

    /** Voice = earpiece (false); video = speaker. Never auto-flip on accept. */
    private val _speakerOn = MutableStateFlow(false)
    val speakerOn: StateFlow<Boolean> = _speakerOn.asStateFlow()

    /**
     * @param waitForPeer when true (caller), keep mic/camera unpublished until [enableMedia] / remote joins.
     */
    suspend fun connect(
        context: Context,
        url: String,
        token: String,
        enableVideo: Boolean,
        waitForPeer: Boolean,
    ) {
        disconnect()
        val gen = ++connectGeneration
        val appCtx = context.applicationContext
        appContext = appCtx
        CallLog.i("livekit connect video=$enableVideo waitForPeer=$waitForPeer gen=$gen")
        val created = LiveKit.create(appCtx)
        room = created
        eventsJob = scope.launch {
            created.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> {
                        // Publisher PC is ready — flush any enable that arrived during connect.
                        pendingEnableVideo?.let { pending ->
                            pendingEnableVideo = null
                            scope.launch { publishMedia(pending) }
                        }
                    }
                    is RoomEvent.ParticipantConnected -> {
                        refreshRemoteJoined(created)
                        if (_remoteJoined.value) {
                            ActiveCallSession.markAnswered()
                            enableMedia(enableVideo)
                        }
                    }
                    is RoomEvent.ParticipantDisconnected -> {
                        refreshRemoteJoined(created)
                    }
                    is RoomEvent.TrackSubscribed -> {
                        val track = event.track
                        if (track is VideoTrack && event.participant != created.localParticipant) {
                            _remoteVideo.value = track
                        }
                    }
                    is RoomEvent.TrackUnsubscribed -> {
                        if (event.track === _remoteVideo.value) {
                            _remoteVideo.value = null
                        }
                    }
                    is RoomEvent.Disconnected -> {
                        _roomConnected.value = false
                        _remoteJoined.value = false
                    }
                    else -> Unit
                }
            }
        }
        created.connect(url, token)
        if (gen != connectGeneration) return

        _roomConnected.value = true
        refreshRemoteJoined(created)

        val pending = pendingEnableVideo
        pendingEnableVideo = null
        when {
            pending != null -> publishMedia(pending)
            waitForPeer && !_remoteJoined.value -> {
                // Ringing: joined for fast connect; tracks stay unpublished (no publish API).
                _muted.value = false
                _cameraOff.value = !enableVideo
            }
            else -> publishMedia(enableVideo)
        }
        // Voice → earpiece; video → speaker. Same for caller and callee.
        setSpeaker(appCtx, enableVideo)
    }

    /** Unmute / publish after callee accepts (or remote appears). */
    fun enableMedia(enableVideo: Boolean) {
        scope.launch {
            if (!_roomConnected.value || room == null) {
                pendingEnableVideo = enableVideo
                return@launch
            }
            publishMedia(enableVideo)
        }
    }

    fun setMuted(muted: Boolean) {
        _muted.value = muted
        scope.launch {
            runCatching {
                room?.localParticipant?.setMicrophoneEnabled(!muted)
            }.onFailure { CallLog.w("setMuted failed", it) }
        }
    }

    fun setCameraOff(off: Boolean) {
        _cameraOff.value = off
        scope.launch {
            runCatching {
                room?.localParticipant?.setCameraEnabled(!off)
                if (!off) refreshLocalVideo() else _localVideo.value = null
            }.onFailure { CallLog.w("setCameraOff failed", it) }
        }
    }

    fun setSpeaker(context: Context, on: Boolean) {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        am.isSpeakerphoneOn = on
        _speakerOn.value = on
    }

    fun flipCamera() {
        val track = _localVideo.value as? LocalVideoTrack ?: return
        val next = if (track.options.position == CameraPosition.FRONT) {
            CameraPosition.BACK
        } else {
            CameraPosition.FRONT
        }
        track.switchCamera(position = next)
    }

    fun disconnect() {
        connectGeneration++
        CallLog.d("livekit disconnect gen=$connectGeneration")
        eventsJob?.cancel()
        eventsJob = null
        pendingEnableVideo = null
        room?.disconnect()
        room?.release()
        room = null
        appContext?.let { ctx ->
            val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            @Suppress("DEPRECATION")
            am.isSpeakerphoneOn = false
            am.mode = AudioManager.MODE_NORMAL
        }
        appContext = null
        _roomConnected.value = false
        _remoteJoined.value = false
        _remoteVideo.value = null
        _localVideo.value = null
        _muted.value = false
        _cameraOff.value = false
        _speakerOn.value = false
    }

    private suspend fun publishMedia(enableVideo: Boolean) {
        // Slow devices (Xsmart Mate 10): connect() can return before engine.publisher exists.
        repeat(8) { attempt ->
            val lp = room?.localParticipant
            if (lp == null || !_roomConnected.value) {
                pendingEnableVideo = enableVideo
                return
            }
            val result = runCatching {
                lp.setMicrophoneEnabled(true)
                if (enableVideo) {
                    lp.setCameraEnabled(true)
                    refreshLocalVideo()
                } else {
                    lp.setCameraEnabled(false)
                }
            }
            if (result.isSuccess) {
                _muted.value = false
                _cameraOff.value = !enableVideo
                return
            }
            val err = result.exceptionOrNull()
            val publisherNotReady =
                err is IllegalStateException &&
                    (err.message?.contains("publisher", ignoreCase = true) == true)
            if (!publisherNotReady) {
                CallLog.e("publishMedia failed", err)
                return
            }
            CallLog.w("publisher not ready, retry ${attempt + 1}")
            delay(150L * (attempt + 1))
        }
        // Keep trying once Connected fires.
        pendingEnableVideo = enableVideo
        CallLog.e("publishMedia exhausted retries; queued pending enable")
    }

    private fun refreshRemoteJoined(room: Room) {
        _remoteJoined.value = room.remoteParticipants.isNotEmpty()
    }

    private fun refreshLocalVideo() {
        room?.localParticipant?.videoTrackPublications?.forEach { (_, track) ->
            if (track is LocalVideoTrack) {
                _localVideo.value = track
            } else if (track is VideoTrack) {
                _localVideo.value = track
            }
        }
    }
}
