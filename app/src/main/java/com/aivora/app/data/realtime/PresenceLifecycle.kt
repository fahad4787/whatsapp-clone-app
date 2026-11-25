package com.aivora.app.data.realtime

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallActions
import com.aivora.app.data.call.LiveKitCallController
import com.aivora.app.data.local.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * App foreground = online (WS connected). Minimized / closed = offline (WS closed),
 * except while a call is ringing or running: its "answered" / "ended" events arrive on the WS.
 *
 * WS connect is gated by [allowConnect] so cold start does not build OkHttp/WS
 * before the first Home frame (see MainActivity).
 */
object PresenceLifecycle {
    private var registered = false
    private val connectAllowed = AtomicBoolean(false)
    @Volatile private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** True while any screen of the app is visible. */
    @Volatile var foreground = false
        private set

    fun install(appContext: Context) {
        if (registered) return
        registered = true
        val app = appContext.applicationContext
        this.appContext = app
        CallActions.install()
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    foreground = true
                    maybeStart()
                    LiveKitCallController.setBackgrounded(false)
                    CallActions.resync()
                }

                override fun onStop(owner: LifecycleOwner) {
                    foreground = false
                    LiveKitCallController.setBackgrounded(true)
                    // Closing the socket makes the server broadcast offline to peers.
                    if (!inCall()) RealtimeClient.stop()
                }
            },
        )
        // A call that finishes while the app is hidden lets the socket close after all.
        scope.launch {
            var wasInCall = false
            combine(ActiveCallSession.session, ActiveCallSession.incoming) { s, i ->
                s != null || i != null
            }.distinctUntilChanged().collect { now ->
                if (wasInCall && !now && !foreground) RealtimeClient.stop()
                wasInCall = now
            }
        }
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

    private fun inCall(): Boolean =
        ActiveCallSession.session.value != null || ActiveCallSession.incoming.value != null

    private fun maybeStart() {
        if (!connectAllowed.get()) return
        val app = appContext ?: return
        if (!SessionStore.hasSession(app)) return
        val state = ProcessLifecycleOwner.get().lifecycle.currentState
        if (!state.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) return
        RealtimeClient.startFromSession(app)
    }
}
