package com.example.battery

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for the Battery app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class BatteryApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Application initialization code can go here
    }
}