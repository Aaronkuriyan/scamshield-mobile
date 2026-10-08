package com.scamshield.app.service

import android.app.Notification
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
import java.util.concurrent.ConcurrentHashMap

class ScamNotificationListenerService : NotificationListenerService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var repository: ScanRepository
    private var ttsHelper: TextToSpeechHelper? = null

    // Cache to prevent duplicate processing (Key -> Timestamp)
    private val processedCache = ConcurrentHashMap<String, Long>()
    private val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes

    override fun onCreate() {
        super.onCreate()
        repository = ScanRepository(applicationContext)
        ttsHelper = TextToSpeechHelper(applicationContext)
        Log.i(TAG, "ScamNotificationListenerService started and monitoring.")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsHelper?.shutdown()
        scope.cancel()
        Log.i(TAG, "ScamNotificationListenerService stopped.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName ?: return

        // 1. NEVER analyze SCAMSHIELD's own notifications (prevents infinite loop!)
        if (packageName == applicationContext.packageName) {
            return
        }

        // 2. Filter out non-message system apps
        if (isIgnoredSystemPackage(packageName)) {
            return
        }

        // 3. Check if protection toggle is turned on
        if (!isProtectionActive(applicationContext)) {
            return
        }

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        // Extract title and text safely
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val messageBody = when {
            bigText.isNotBlank() -> bigText
            text.isNotBlank() -> text
            else -> ""
        }

        if (messageBody.isBlank()) {
            return
        }

        val fullContent = if (title.isNotBlank()) "$title: $messageBody" else messageBody

        // 4. Deduplication: Avoid repeatedly warning about the same notification
        val cacheKey = "$packageName:${fullContent.hashCode()}"
        val now = System.currentTimeMillis()
        val lastProcessed = processedCache[cacheKey]

        if (lastProcessed != null && (now - lastProcessed) < CACHE_EXPIRY_MS) {
            Log.d(TAG, "Skipping duplicate notification from $packageName")
            return
        }
        processedCache[cacheKey] = now

        // Clean up expired cache items periodically
        if (processedCache.size > 200) {
            val iterator = processedCache.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > CACHE_EXPIRY_MS) {
                    iterator.remove()
                }
            }
        }

        // 5. Asynchronous Analysis Pipeline
        scope.launch {
            try {
                Log.i(TAG, "Analyzing incoming notification from $packageName")
                val record = repository.processIncomingNotification(
                    content = fullContent,
                    sender = title.ifBlank { packageName },
                    packageName = packageName
                )

                // 6. Trigger prompt warning if risk is sufficiently high
                if (record.riskScore >= 70) {
                    Log.w(TAG, "THREAT DETECTED! Risk: ${record.riskScore}, Category: ${record.category}")
                    NotificationHelper.showThreatAlert(applicationContext, record)
                    ttsHelper?.speakWarning(record.recommendation)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification: ${e.message}", e)
            }
        }
    }

    private fun isIgnoredSystemPackage(pkg: String): Boolean {
        return when (pkg) {
            "android",
            "com.android.systemui",
            "com.google.android.googlequicksearchbox",
            "com.android.providers.downloads",
            "com.android.vending" -> true
            else -> false
        }
    }

    companion object {
        private const val TAG = "ScamShieldListener"
        private const val PREFS_NAME = "scamshield_prefs"
        private const val KEY_PROTECTION_ENABLED = "protection_active"

        fun isProtectionActive(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PROTECTION_ENABLED, true) // ON by default
        }

        fun setProtectionActive(context: Context, active: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_PROTECTION_ENABLED, active).apply()
        }
    }
}
