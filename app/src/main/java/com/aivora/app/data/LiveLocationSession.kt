package com.aivora.app.data

import android.annotation.SuppressLint
import android.content.Context
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.LiveLocationBody
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/** Foreground-only live location pings while the process is alive. */
object LiveLocationSession {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val jobs = ConcurrentHashMap<String, Job>()

    fun start(context: Context, messageId: String, conversationId: String) {
        if (jobs.containsKey(messageId)) return
        val app = context.applicationContext
        val fused = LocationServices.getFusedLocationProviderClient(app)
        val job = scope.launch {
            while (isActive) {
                delay(30_000)
                ping(app, fused, messageId, conversationId)
            }
        }
        jobs[messageId] = job
        scope.launch { ping(app, fused, messageId, conversationId) }
    }

    fun stop(messageId: String? = null) {
        if (messageId == null) {
            jobs.values.forEach { it.cancel() }
            jobs.clear()
        } else {
            jobs.remove(messageId)?.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun ping(
        context: Context,
        fused: com.google.android.gms.location.FusedLocationProviderClient,
        messageId: String,
        conversationId: String,
    ) {
        val token = SessionStore.accessToken(context) ?: return
        if (token.isBlank()) return
        val uid = SessionStore.userId(context) ?: return
        val loc = runCatching {
            val cts = CancellationTokenSource()
            fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token).await()
                ?: fused.lastLocation.await()
        }.getOrNull() ?: return
        runCatching {
            val api = NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
            val dto = api.putMessagesIdLiveLocation(
                messageId,
                LiveLocationBody(
                    lat = loc.latitude,
                    lng = loc.longitude,
                    accuracy = loc.accuracy.toDouble(),
                ),
            )
            InMemoryChatStore.upsertMessage(conversationId, dto.toLocalMessage(uid))
        }.onFailure {
            // Expired / ended — stop pinging this share.
            stop(messageId)
        }
    }
}
