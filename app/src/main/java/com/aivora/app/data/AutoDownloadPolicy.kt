package com.aivora.app.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import com.aivora.app.data.local.AutoDownloadPrefs

/** Wave 58 — which media kinds may auto-fetch on the current network. */
object AutoDownloadPolicy {
    enum class Kind { Photo, Audio, Video, File }

    fun allows(context: Context, kind: Kind): Boolean {
        val snap = AutoDownloadPrefs.load(context)
        val rule = when (networkBucket(context)) {
            Bucket.Wifi -> snap.wifi
            Bucket.Roaming -> snap.roaming
            Bucket.Mobile -> snap.mobile
        }
        return when (rule) {
            "none" -> false
            "photos" -> kind == Kind.Photo
            "photos_audio" -> kind == Kind.Photo || kind == Kind.Audio
            else -> true
        }
    }

    private enum class Bucket { Wifi, Roaming, Mobile }

    private fun networkBucket(context: Context): Bucket {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val caps = cm?.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val wifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
        if (wifi) return Bucket.Wifi
        val roaming = runCatching {
            context.getSystemService(TelephonyManager::class.java)?.isNetworkRoaming == true
        }.getOrDefault(false)
        return if (roaming) Bucket.Roaming else Bucket.Mobile
    }
}
