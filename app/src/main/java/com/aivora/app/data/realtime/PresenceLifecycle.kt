package com.aivora.app.data.realtime

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aivora.app.data.local.SessionStore

/**
 * App foreground = online (WS connected). Minimized / closed = offline (WS closed).
 * Presence is no longer tied to having a chat screen open.
 */
object PresenceLifecycle {
    private var registered = false

    fun install(appContext: Context) {
        if (registered) return
        registered = true
        val app = appContext.applicationContext
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (SessionStore.hasSession(app)) {
                        RealtimeClient.startFromSession(app)
                    }
                }

                override fun onStop(owner: LifecycleOwner) {
                    // Closing the socket makes the server broadcast offline to peers.
                    RealtimeClient.stop()
                }
            },
        )
        if (SessionStore.hasSession(app) &&
            ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                androidx.lifecycle.Lifecycle.State.STARTED,
            )
        ) {
            RealtimeClient.startFromSession(app)
        }
    }
}
