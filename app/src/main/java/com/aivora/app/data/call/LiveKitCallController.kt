package com.aivora.app.data.call

import android.app.ActivityManager
import android.content.Context
import android.media.AudioManager
import com.aivora.app.data.local.CallPrivacyPrefs
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.participant.VideoTrackPublishDefaults
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.LocalVideoTrackOptions
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoPreset169
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
import livekit.org.webrtc.PeerConnection
import livekit.org.webrtc.RendererCommon

/** Thin LiveKit room wrapper for Wave 16.1 in-call screens. */
object LiveKitCallController {
    private const val RENDERER_ROOM_TAG = 0x0A17_0001

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
     * Bind a TextureViewRenderer to this room's EGL (required — own EglBase = blank frames).
     * Idempotent: skip if this view was already initialized for the current room.
     */
    fun initRenderer(view: TextureViewRenderer, fill: Boolean = true) {
        val r = room
        if (r == null) {
            CallLog.w("initRenderer before room ready")
            return
        }
        if (view.getTag(RENDERER_ROOM_TAG) !== r) {
            r.initVideoRenderer(view)
            view.setTag(RENDERER_ROOM_TAG, r)
        }
        view.setScalingType(
            if (fill) RendererCommon.ScalingType.SCALE_ASPECT_FILL
            else RendererCommon.ScalingType.SCALE_ASPECT_FIT,
        )
        view.setEnableHardwareScaler(true)
    }

    /**
     * @param waitForPeer when true (audio caller), keep mic unpublished until peer joins.
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

        val preset = videoPresetForDevice(appCtx)
        val created = LiveKit.create(
            appCtx,
            options = RoomOptions(
                // Subscribe only needed layers — cuts decode cost on Mate-class devices.
                adaptiveStream = true,
                dynacast = true,
                videoTrackCaptureDefaults = LocalVideoTrackOptions(
                    position = CameraPosition.FRONT,
                    captureParams = preset.capture,
                ),
                videoTrackPublishDefaults = VideoTrackPublishDefaults(
                    videoEncoding = preset.encoding,
                    simulcast = true,
                ),
            ),
        )
        room = created
        eventsJob = scope.launch {
            created.events.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> {
                        refreshRemoteJoined(created)
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
                        refreshRemoteJoined(created)
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
                    is RoomEvent.TrackPublished -> {
                        if (event.participant == created.localParticipant &&
                            event.publication.kind == Track.Kind.VIDEO
                        ) {
                            refreshLocalVideo()
                        }
                    }
                    is RoomEvent.TrackUnpublished -> {
                        if (event.participant == created.localParticipant &&
                            event.publication.kind == Track.Kind.VIDEO
                        ) {
                            refreshLocalVideo()
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
        val protectIp = CallPrivacyPrefs.protectIpCalls()
        val connectOpts = if (protectIp) {
            // Empty iceServers → LiveKit merges server TURN/STUN; RELAY hides local IP.
            val rtc = PeerConnection.RTCConfiguration(emptyList()).apply {
                iceTransportsType = PeerConnection.IceTransportsType.RELAY
            }
            ConnectOptions(rtcConfig = rtc)
        } else {
            ConnectOptions()
        }
        CallLog.i("livekit connect protectIp=$protectIp forceRelay=$protectIp")
        created.connect(url, token, connectOpts)
        if (gen != connectGeneration) return

        _roomConnected.value = true
        refreshRemoteJoined(created)

        val pending = pendingEnableVideo
        pendingEnableVideo = null
        when {
            pending != null -> publishMedia(pending)
            // Video: publish camera while ringing so self-preview works.
            enableVideo -> publishMedia(true)
            waitForPeer && !_remoteJoined.value -> {
                _muted.value = false
                _cameraOff.value = true
            }
            else -> publishMedia(false)
        }
        setSpeaker(appCtx, enableVideo)
    }

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
                if (!off) {
                    delay(80)
                    refreshLocalVideo()
                } else {
                    _localVideo.value = null
                }
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
                } else {
                    lp.setCameraEnabled(false)
                }
            }
            if (result.isSuccess) {
                _muted.value = false
                _cameraOff.value = !enableVideo
                if (enableVideo) {
                    // Publication can lag a frame after setCameraEnabled.
                    repeat(6) {
                        refreshLocalVideo()
                        if (_localVideo.value != null) return
                        delay(80)
                    }
                } else {
                    _localVideo.value = null
                }
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
        pendingEnableVideo = enableVideo
        CallLog.e("publishMedia exhausted retries; queued pending enable")
    }

    private fun refreshRemoteJoined(room: Room) {
        _remoteJoined.value = room.remoteParticipants.isNotEmpty()
    }

    private fun refreshLocalVideo() {
        val lp = room?.localParticipant ?: return
        // Prefer camera source publication.
        val cameraPub = lp.getTrackPublication(Track.Source.CAMERA)
        val cameraTrack = cameraPub?.track
        if (cameraTrack is LocalVideoTrack) {
            _localVideo.value = cameraTrack
            return
        }
        if (cameraTrack is VideoTrack) {
            _localVideo.value = cameraTrack
            return
        }
        // List is Pair<TrackPublication, Track?>
        for ((pub, track) in lp.videoTrackPublications) {
            val t = track ?: pub.track
            if (t is LocalVideoTrack || t is VideoTrack) {
                _localVideo.value = t as VideoTrack
                return
            }
        }
        if (_cameraOff.value) _localVideo.value = null
    }

    /** Mate-class: H360 keeps encode cheap; mid/high: H540 for clearer WA-like video. */
    private fun videoPresetForDevice(context: Context): VideoPreset169 {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val lowRam = am?.isLowRamDevice == true
        val heapMb = (Runtime.getRuntime().maxMemory() / (1024L * 1024L)).toInt()
        if (com.aivora.app.data.local.AutoDownloadPrefs.load(context).lessDataCalls) {
            return VideoPreset169.H360
        }
        return when {
            lowRam || heapMb <= 192 -> VideoPreset169.H360
            heapMb <= 384 -> VideoPreset169.H540
            else -> VideoPreset169.H720
        }
    }
}
