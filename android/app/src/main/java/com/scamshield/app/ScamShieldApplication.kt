package com.scamshield.app

import android.app.Application
import com.scamshield.app.service.NotificationHelper

class ScamShieldApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels at application start
        NotificationHelper.createNotificationChannel(this)
    }
}
