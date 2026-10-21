package com.aivora.app

import android.app.Application

class AivoraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AivoraApp
            private set
    }
}
