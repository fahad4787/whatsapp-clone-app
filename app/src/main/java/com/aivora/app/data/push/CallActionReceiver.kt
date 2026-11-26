package com.aivora.app.data.push

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.call.ActiveCallSession
import com.aivora.app.data.call.CallLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Decline from the incoming-call notification, without opening the app. */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != MessageNotifier.ACTION_DECLINE_CALL) return
        val callId = intent.getStringExtra(MessageNotifier.EXTRA_CALL_ID) ?: return
        CallLog.i("decline from notification callId=$callId")
        MessageNotifier.cancelCall(context, callId)
        if (ActiveCallSession.incoming.value?.id == callId) ActiveCallSession.clearIncoming()
        val pending = goAsync()
        scope.launch {
            try {
                runCatching { NetworkModule.api().postCallsIdAction(callId, "reject") }
                    .onFailure { CallLog.w("reject api failed callId=$callId", it) }
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
