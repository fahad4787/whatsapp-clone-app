package com.aivora.app.ui.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Simple tap-to-toggle voice note recorder (m4a / AAC). */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAtMs: Long = 0L

    val isRecording: Boolean get() = recorder != null

    fun start(): Boolean {
        stopInternal(discard = true)
        val dir = File(context.cacheDir, "voice").apply { mkdirs() }
        val out = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= 31) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        return try {
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioEncodingBitRate(96_000)
            rec.setAudioSamplingRate(44_100)
            rec.setOutputFile(out.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            file = out
            startedAtMs = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            runCatching { rec.release() }
            out.delete()
            recorder = null
            file = null
            false
        }
    }

    /** 0..1 mic level for live waveform while recording. */
    fun amplitude(): Float {
        val amp = runCatching { recorder?.maxAmplitude ?: 0 }.getOrDefault(0)
        return (amp / 16000f).coerceIn(0.12f, 1f)
    }

    fun elapsedSec(): Int {
        if (recorder == null || startedAtMs == 0L) return 0
        return ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt().coerceAtLeast(0)
    }

    /** Stops recording; returns file + duration seconds, or null if cancelled/failed. */
    fun stop(): Pair<File, Int>? {
        val out = file
        val started = startedAtMs
        val rec = recorder
        recorder = null
        file = null
        startedAtMs = 0L
        if (rec == null || out == null) return null
        return try {
            rec.stop()
            rec.release()
            val secs = ((System.currentTimeMillis() - started) / 1000L).toInt().coerceAtLeast(1)
            if (!out.exists() || out.length() < 32) {
                out.delete()
                null
            } else {
                out to secs
            }
        } catch (_: Exception) {
            runCatching { rec.release() }
            out.delete()
            null
        }
    }

    fun cancel() = stopInternal(discard = true)

    private fun stopInternal(discard: Boolean) {
        val rec = recorder
        val out = file
        recorder = null
        file = null
        startedAtMs = 0L
        if (rec != null) {
            runCatching {
                rec.stop()
                rec.release()
            }
            runCatching { rec.release() }
        }
        if (discard) out?.delete()
    }
}
