package com.aivora.app.ui.navigation

import android.content.Intent
import android.net.Uri

/**
 * Maps call-link intents to in-app routes.
 * Staging hosts: Render API + custom scheme `aivora://call/{code}` (not wa.zirvik.com).
 */
object DeepLinks {
    private const val StagingHost = "whatsapp-clone-backend-hep1.onrender.com"

    fun routeFrom(intent: Intent?): String? = routeFrom(intent?.data)

    fun routeFrom(uri: Uri?): String? {
        if (uri == null) return null
        // aivora://call/{code}
        if (uri.scheme == "aivora" && uri.host == "call") {
            val code = uri.pathSegments?.firstOrNull()?.trim().orEmpty()
            if (code.isNotEmpty()) return Routes.callLinkJoin(code)
        }
        // https://whatsapp-clone-backend-hep1.onrender.com/call/{code}
        if (uri.scheme == "https" && uri.host == StagingHost) {
            val segments = uri.pathSegments ?: return null
            if (segments.size >= 2 && segments[0] == "call") {
                val code = segments[1].trim()
                if (code.isNotEmpty()) return Routes.callLinkJoin(code)
            }
        }
        return null
    }
}
