package com.aivora.app.data.push

import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.model.CallDto
import com.aivora.app.data.model.CallerDto
import com.aivora.app.data.realtime.RealtimeClient
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AivoraFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushRegistrar.registerToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"].orEmpty()
        if (type == "call") {
            handleIncomingCall(message)
            return
        }
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Aivora"
        val body = message.notification?.body
            ?: message.data["preview"]
            ?: message.data["body"]
            ?: "New message"
        val conversationId = message.data["conversation_id"]
        MessageNotifier.show(applicationContext, title, body, conversationId)
    }

    private fun handleIncomingCall(message: RemoteMessage) {
        val callId = message.data["call_id"].orEmpty()
        if (callId.isBlank()) return
        // Push wake: allow WS immediately (bypass cold-start gate).
        com.aivora.app.data.realtime.PresenceLifecycle.allowConnect(applicationContext)
        RealtimeClient.startFromSession(applicationContext)
        if (ActiveCallSession.incoming.value?.id == callId) return
        if (ActiveCallSession.session.value?.callId == callId) return

        val media = message.data["media"]?.ifBlank { null } ?: "audio"
        val callerName = message.data["caller_name"]?.ifBlank { null } ?: "Incoming call"
        val conversationId = message.data["conversation_id"]?.ifBlank { null }
        ActiveCallSession.setIncoming(
            CallDto(
                id = callId,
                media = media,
                conversationId = conversationId,
                status = "ringing",
                caller = CallerDto(name = callerName),
            ),
        )
        MessageNotifier.ensureCallsChannel(applicationContext)
        MessageNotifier.showCall(
            applicationContext,
            callerName,
            if (media == "video") "Incoming video call" else "Incoming voice call",
            conversationId,
            callId,
        )
    }
}
