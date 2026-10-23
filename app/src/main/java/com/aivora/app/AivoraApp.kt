package com.aivora.app

import android.app.Application

class AivoraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        // Push registration runs after first Activity frame (see MainActivity) — keeps cold start light.
    }

    companion object {
        lateinit var instance: AivoraApp
            private set
    }
}
