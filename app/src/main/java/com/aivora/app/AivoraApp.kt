package com.aivora.app

import android.app.Application
import com.aivora.app.data.push.PushRegistrar

class AivoraApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        PushRegistrar.refresh(this)
    }

    companion object {
        lateinit var instance: AivoraApp
            private set
    }
}
