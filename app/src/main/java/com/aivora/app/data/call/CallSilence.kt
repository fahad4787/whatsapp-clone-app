package com.aivora.app.data.call

import com.aivora.app.data.local.InMemoryChatStore
import com.aivora.app.data.local.InMemoryPrivacyStore
import com.aivora.app.data.model.CallDto

/** Wave 52 — client-side guard matching server Silence unknown callers. */
object CallSilence {
    fun shouldSuppressRing(call: CallDto): Boolean {
        val privacy = InMemoryPrivacyStore.dto ?: return false
        if (!privacy.silenceUnknownCallers) return false
        // Shared conversation (group / existing DM) → always ring.
        if (!call.conversationId.isNullOrBlank()) return false
        val initiator = call.initiatorId?.takeIf { it.isNotBlank() }
            ?: call.caller?.id?.takeIf { it.isNotBlank() }
            ?: return false
        return InMemoryChatStore.chats.value.none { chat ->
            !chat.isGroup && chat.peerUserId == initiator
        }
    }
}
