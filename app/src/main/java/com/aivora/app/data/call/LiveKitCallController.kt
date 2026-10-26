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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Thin LiveKit room wrapper for Wave 16.1 in-call screens. */
object LiveKitCallController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var room: Room? = null
    private var eventsJob: Job? = null

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

    private val _speakerOn = MutableStateFlow(true)
    val speakerOn: StateFlow<Boolean> = _speakerOn.asStateFlow()

    /**
     * @param waitForPeer when true (caller), keep mic/camera off until [enableMedia] / remote joins.
     */
    suspend fun connect(
        context: Context,
        url: String,
        token: String,
        enableVideo: Boolean,
        waitForPeer: Boolean,
    ) {
        disconnect()
        val appCtx = context.applicationContext
        val created = LiveKit.create(appCtx)
        room = created
        eventsJob = scope.launch {
            created.events.collect { event ->
                when (event) {
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
        _roomConnected.value = true
        refreshRemoteJoined(created)

        if (waitForPeer && !_remoteJoined.value) {
            // Ringing: join room for fast connect, but do not publish audio/video yet.
            created.localParticipant.setMicrophoneEnabled(false)
            created.localParticipant.setCameraEnabled(false)
            _muted.value = true
            _cameraOff.value = true
        } else {
            enableMedia(enableVideo)
        }
        setSpeaker(appCtx, enableVideo || !waitForPeer)
    }

    /** Unmute / publish after callee accepts (or remote appears). */
    fun enableMedia(enableVideo: Boolean) {
        scope.launch {
            val lp = room?.localParticipant ?: return@launch
            lp.setMicrophoneEnabled(true)
            _muted.value = false
            if (enableVideo) {
                lp.setCameraEnabled(true)
                refreshLocalVideo()
                _cameraOff.value = false
            } else {
                lp.setCameraEnabled(false)
                _cameraOff.value = true
            }
        }
    }

    fun setMuted(muted: Boolean) {
        _muted.value = muted
        scope.launch {
            room?.localParticipant?.setMicrophoneEnabled(!muted)
        }
    }

    fun setCameraOff(off: Boolean) {
        _cameraOff.value = off
        scope.launch {
            room?.localParticipant?.setCameraEnabled(!off)
            if (!off) refreshLocalVideo() else _localVideo.value = null
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
        eventsJob?.cancel()
        eventsJob = null
        room?.disconnect()
        room?.release()
        room = null
        _roomConnected.value = false
        _remoteJoined.value = false
        _remoteVideo.value = null
        _localVideo.value = null
        _muted.value = false
        _cameraOff.value = false
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
