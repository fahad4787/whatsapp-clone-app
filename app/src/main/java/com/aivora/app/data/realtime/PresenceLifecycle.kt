package com.aivora.app.data.realtime

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aivora.app.data.local.SessionStore
import java.util.concurrent.atomic.AtomicBoolean

/**
 * App foreground = online (WS connected). Minimized / closed = offline (WS closed).
 *
 * WS connect is gated by [allowConnect] so cold start does not build OkHttp/WS
 * before the first Home frame (see MainActivity).
 */
object PresenceLifecycle {
    private var registered = false
    private val connectAllowed = AtomicBoolean(false)
    @Volatile private var appContext: Context? = null

    fun install(appContext: Context) {
        if (registered) return
        registered = true
        val app = appContext.applicationContext
        this.appContext = app
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    maybeStart()
                }

                override fun onStop(owner: LifecycleOwner) {
                    // Closing the socket makes the server broadcast offline to peers.
                    RealtimeClient.stop()
                }
            },
        )
        // Do not start WS here — wait for [allowConnect] after first frame.
    }

    /**
     * Call after first Compose frame. Enables WS for this process and starts
     * immediately if the app is already in foreground with a session.
     */
    fun allowConnect(context: Context? = null) {
        if (context != null) appContext = context.applicationContext
        connectAllowed.set(true)
        maybeStart()
    }

    private fun maybeStart() {
        if (!connectAllowed.get()) return
        val app = appContext ?: return
        if (!SessionStore.hasSession(app)) return
        val state = ProcessLifecycleOwner.get().lifecycle.currentState
        if (!state.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return
        RealtimeClient.startFromSession(app)
    }
}
