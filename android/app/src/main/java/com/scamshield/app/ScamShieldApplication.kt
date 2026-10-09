package com.scamshield.app

import android.app.Application
import com.scamshield.app.service.NotificationHelper
import com.scamshield.app.service.ScamNotificationListenerService

class ScamShieldApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize notification channels at application start
        NotificationHelper.createNotificationChannel(this)

        // Ensure notification listener service is bound if permission is granted
        ScamNotificationListenerService.ensureServiceBound(this)
    }
}
