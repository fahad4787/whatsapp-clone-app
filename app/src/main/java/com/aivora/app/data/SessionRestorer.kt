package com.aivora.app.data

import android.content.Context
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SessionRestorer {
    /** Best-effort token refresh so a cold start after 15+ minutes still works. */
    suspend fun ensureFresh(context: Context): Boolean = withContext(Dispatchers.IO) {
        val app = context.applicationContext
        if (!SessionStore.hasSession(app)) return@withContext false
        if (SessionStore.refreshToken(app).isNullOrBlank()) {
            return@withContext !SessionStore.accessToken(app).isNullOrBlank()
        }
        NetworkModule.refreshAccessBlocking() != null ||
            !SessionStore.accessToken(app).isNullOrBlank()
    }
}
