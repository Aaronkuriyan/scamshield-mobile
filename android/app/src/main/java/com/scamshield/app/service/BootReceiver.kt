package com.scamshield.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Ensures ScamNotificationListenerService is active and bound upon device restart.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i("ScamShieldBoot", "Device boot completed. Re-binding ScamNotificationListenerService.")
            ScamNotificationListenerService.ensureServiceBound(context)
        }
    }
}
