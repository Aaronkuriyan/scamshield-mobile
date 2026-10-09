package com.scamshield.app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.scamshield.app.data.repository.ScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Real-time SMS BroadcastReceiver for incoming SMS detection.
 * Intercepts Telephony.SMS_RECEIVED, extracts sender and multi-part message content,
 * deduplicates against the shared cache, and feeds into the existing SCAMSHIELD analysis pipeline.
 */
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        // 1. Check if ScamShield protection is active
        if (!ScamNotificationListenerService.isProtectionActive(context)) {
            Log.d(TAG, "Protection inactive, ignoring incoming SMS.")
            return
        }

        val messages = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse SMS messages from intent: ${e.message}")
            null
        }

        if (messages.isNullOrEmpty()) {
            return
        }

        // 2. Extract sender and concatenate multipart SMS segments
        val sender = messages[0].displayOriginatingAddress
            ?: messages[0].originatingAddress
            ?: "SMS"

        val bodyBuilder = StringBuilder()
        for (sms in messages) {
            val part = sms.displayMessageBody ?: sms.messageBody ?: ""
            bodyBuilder.append(part)
        }
        val fullContent = bodyBuilder.toString().trim()

        if (fullContent.isBlank()) {
            return
        }

        // 3. Deduplication check (shared with notification listener)
        if (MessageDeduplicator.isDuplicate("SMS", sender, fullContent)) {
            Log.d(TAG, "SMS already processed by notification listener, skipping duplicate.")
            return
        }

        Log.i(TAG, "Detected incoming SMS from sender: $sender, len=${fullContent.length}")

        // 4. Asynchronously process via existing repository using goAsync()
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        val repository = ScanRepository(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val record = repository.processIncomingNotification(
                    content = fullContent,
                    sender = sender,
                    packageName = "SMS"
                )

                Log.i(TAG, "SMS analyzed: score=${record.riskScore}, classification=${record.classification}")

                // 5. Trigger warning alert if high-risk threat
                if (record.riskScore >= 70) {
                    Log.w(TAG, "THREAT DETECTED in SMS! Triggering alert.")
                    NotificationHelper.showThreatAlert(appContext, record)
                    try {
                        TextToSpeechHelper(appContext).speakWarning(record.recommendation)
                    } catch (e: Exception) {
                        Log.w(TAG, "TTS warning error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing incoming SMS: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "ScamShieldSmsReceiver"
    }
}
