package com.aivora.app.data.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AivoraFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        PushRegistrar.registerToken(applicationContext, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
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
}
