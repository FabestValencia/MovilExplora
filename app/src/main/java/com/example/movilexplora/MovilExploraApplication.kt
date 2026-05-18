package com.example.movilexplora

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovilExploraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mapOf(
            "cloud_name" to "ducmuziql",
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}

