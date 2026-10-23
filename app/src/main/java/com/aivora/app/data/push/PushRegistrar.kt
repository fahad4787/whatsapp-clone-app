package com.aivora.app.data.push

import android.content.Context
import android.os.Build
import android.util.Log
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.PushTokenBody
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object PushRegistrar {
    private const val TAG = "PushRegistrar"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun refresh(context: Context) {
        val token = SessionStore.accessToken(context)
        if (token.isNullOrBlank()) return
        scope.launch {
            try {
                val fcmToken = FirebaseMessaging.getInstance().token.await()
                if (fcmToken.isNullOrBlank()) return@launch
                registerToken(context, fcmToken)
            } catch (e: Exception) {
                Log.w(TAG, "FCM token unavailable (add google-services.json?)", e)
            }
        }
    }

    fun registerToken(context: Context, fcmToken: String) {
        val access = SessionStore.accessToken(context) ?: return
        scope.launch {
            try {
                val api = NetworkModule.api(NetworkModule.okHttp { access })
                api.putMePushToken(
                    PushTokenBody(
                        pushToken = fcmToken,
                        platform = "android",
                        deviceName = Build.MODEL,
                    ),
                )
                Log.i(TAG, "Push token registered")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register push token", e)
            }
        }
    }
}
