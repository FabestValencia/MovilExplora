package com.example.movilexplora

import android.app.Application
import com.mapbox.common.MapboxOptions
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovilExploraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        val config = mapOf(
            "cloud_name" to "ducmuziql",
            "api_key" to BuildConfig.CLOUDINARY_API_KEY,
            "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
            "secure" to true
        )
        MediaManager.init(this, config)
    }
}
