package com.scamshield.app.service

import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.scamshield.app.data.repository.ScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Android NotificationListenerService that automatically captures and extracts incoming
 * WhatsApp and SMS message notifications, verifies deduplication, and feeds them directly
 * into the existing SCAMSHIELD analysis pipeline in real-time.
 */
class ScamNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: ScanRepository
    private var ttsHelper: TextToSpeechHelper? = null

    override fun onCreate() {
        super.onCreate()
        repository = ScanRepository(applicationContext)
        ttsHelper = TextToSpeechHelper(applicationContext)
        Log.i(TAG, "ScamNotificationListenerService created.")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
        scope.cancel()
        Log.i(TAG, "ScamNotificationListenerService destroyed.")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "ScamNotificationListenerService successfully connected to Android system.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "ScamNotificationListenerService disconnected by system. Requesting rebind...")
        try {
            requestRebind(ComponentName(this, ScamNotificationListenerService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting rebind: ${e.message}")
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // 1. NEVER analyze SCAMSHIELD's own notifications (prevents infinite loop!)
        if (packageName == applicationContext.packageName) {
            return
        }

        // 2. Check if user has protection active in settings
        if (!isProtectionActive(applicationContext)) {
            return
        }

        // 3. Resolve supported messaging application (WhatsApp or SMS)
        val supportedApp = MessageSourceFilter.fromPackageName(applicationContext, packageName) ?: return

        // 4. Ignore irrelevant notifications (ongoing calls, progress, backups, summary groups)
        if (NotificationMessageExtractor.isIgnoredNotification(sbn)) {
            return
        }

        // 5. Extract message content (MessagingStyle messages or standard notifications)
        val extractedMessages = NotificationMessageExtractor.extractMessages(sbn, supportedApp)
        if (extractedMessages.isEmpty()) {
            return
        }

        // 6. Process each extracted message through deduplication and existing analysis pipeline
        for (msg in extractedMessages) {
            if (MessageDeduplicator.isDuplicate(msg.source, msg.sender, msg.message)) {
                Log.d(TAG, "Skipping duplicate notification for source=${msg.source}, sender=${msg.sender}")
                continue
            }

            Log.i(TAG, "Detected incoming message from source=${msg.source}, sender=${msg.sender}, len=${msg.message.length}")

            scope.launch {
                try {
                    val record = repository.processIncomingNotification(
                        content = msg.message,
                        sender = msg.sender,
                        packageName = msg.source
                    )

                    Log.i(TAG, "Message analyzed: score=${record.riskScore}, classification=${record.classification}")

                    // 7. Trigger warning alert if risk is sufficiently high
                    if (record.riskScore >= 70) {
                        Log.w(TAG, "THREAT DETECTED! Risk: ${record.riskScore}, Category: ${record.category}")
                        NotificationHelper.showThreatAlert(applicationContext, record)
                        try {
                            ttsHelper?.speakWarning(record.recommendation)
                        } catch (e: Exception) {
                            Log.w(TAG, "TTS warning error: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error analyzing incoming notification: ${e.message}", e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "ScamShieldListener"
        private const val PREFS_NAME = "scamshield_prefs"
        private const val KEY_PROTECTION_ENABLED = "protection_active"

        fun isProtectionActive(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PROTECTION_ENABLED, true) // Active by default
        }

        fun setProtectionActive(context: Context, active: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, active).apply()
        }

        /**
         * Ensures the NotificationListenerService is bound if notification listener access is enabled.
         * Toggles component state to wake up Android NotificationManagerService after app updates.
         */
        fun ensureServiceBound(context: Context) {
            try {
                val component = ComponentName(context, ScamNotificationListenerService::class.java)
                val pm = context.packageManager
                pm.setComponentEnabledSetting(
                    component,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
                pm.setComponentEnabledSetting(
                    component,
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
                requestRebind(component)
                Log.i(TAG, "ensureServiceBound: Successfully cycled component state and requested rebind")
            } catch (e: Exception) {
                Log.d(TAG, "ensureServiceBound: requestRebind not needed or failed: ${e.message}")
            }
        }
    }
}
