package com.aivora.app

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import com.aivora.app.data.realtime.PresenceLifecycle
import okio.Path.Companion.toOkioPath

class AivoraApp : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        instance = this
        // App foreground ↔ WS online; background/kill ↔ offline for peers.
        PresenceLifecycle.install(this)
        // Push registration runs after first Activity frame (see MainActivity) — keeps cold start light.
    }

    override fun newImageLoader(context: Context): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(false)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.18)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_images").toOkioPath())
                    .maxSizeBytes(48L * 1024 * 1024)
                    .build()
            }
            .build()

    companion object {
        lateinit var instance: AivoraApp
            private set
    }
}
