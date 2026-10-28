package com.aivora.app.data

import android.content.Context
import com.aivora.app.data.local.ChatListCache
import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Kick chat-list disk hydrate as early as Application.onCreate so MainActivity's
 * first Home frame often already has rows (WhatsApp-class cold paint).
 */
object StartupWarm {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)

    @Volatile
    var chatListReady: Boolean = false
        private set

    fun kick(context: Context) {
        if (!started.compareAndSet(false, true)) return
        val app = context.applicationContext
        if (!SessionStore.hasSession(app)) {
            chatListReady = true
            return
        }
        scope.launch {
            try {
                InMemoryChatStore.hydrateFromCache(ChatListCache.load(app))
                // Light warm only — full hydrateRecent stays on Home for scroll/open.
                ChatRepository.hydrateRecentFromDisk(app, limit = 8)
            } finally {
                chatListReady = true
            }
        }
    }
}
