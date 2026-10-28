package com.aivora.app

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.aivora.app.data.StartupWarm
import com.aivora.app.data.realtime.PresenceLifecycle
import okio.Path.Companion.toOkioPath

class AivoraApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Register only — WS connect waits for first frame (MainActivity.allowConnect).
        PresenceLifecycle.install(this)
        // Parallel disk hydrate so Home often paints with rows on frame 1.
        StartupWarm.kick(this)
        // Firebase + FCM: deferred until after first frame (see MainActivity / PushRegistrar).
    }

    override fun newImageLoader(context: Context): ImageLoader {
        val am = context.getSystemService(ActivityManager::class.java)
        // Mate-class phones often report ~192–256MB heap — treat ≤256 as low-RAM (F9).
        val lowRam = am?.isLowRamDevice == true || (am?.memoryClass ?: 256) <= 256
        val memPercent = if (lowRam) 0.08 else 0.15
        val diskBytes = if (lowRam) 24L * 1024 * 1024 else 48L * 1024 * 1024
        return ImageLoader.Builder(context)
            .crossfade(false)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = memPercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_images").toOkioPath())
                    .maxSizeBytes(diskBytes)
                    .build()
            }
            .build()
    }

    companion object {
        lateinit var instance: AivoraApp
            private set
    }
}
