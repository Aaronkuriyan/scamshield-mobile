package com.scamshield.app.service

import android.app.Notification
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern

data class ExtractedMessage(
    val source: String,
    val sender: String,
    val message: String,
    val timestamp: Long
)

/**
 * Extracts clean, legitimate message data from Android notifications.
 * Handles NotificationCompat.MessagingStyle for WhatsApp and standard notification structures.
 * Filters out system noise, ongoing events, calls, and group summary placeholders.
 */
object NotificationMessageExtractor {

    private val SUMMARY_PATTERN = Pattern.compile("^\\d+\\s+(?:new\\s+|unread\\s+)?messages?$", Pattern.CASE_INSENSITIVE)
    private val GENERIC_PLACEHOLDER_PATTERN = Pattern.compile("^(?:new\\s+message|\\d+\\s+conversations?)$", Pattern.CASE_INSENSITIVE)

    /**
     * Determines whether a notification should be completely ignored (calls, system, ongoing, etc.).
     */
    fun isIgnoredNotification(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification ?: return true

        // Ignore ongoing events (calls in progress, backups, transfers)
        if ((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0) {
            return true
        }

        // Ignore group summaries (WhatsApp posts individual notifications for child messages)
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return true
        }

        // Ignore calls, services, progress
        val category = notification.category
        if (category == Notification.CATEGORY_CALL ||
            category == Notification.CATEGORY_SERVICE ||
            category == Notification.CATEGORY_PROGRESS ||
            category == Notification.CATEGORY_SYSTEM
        ) {
            return true
        }

        val extras = notification.extras ?: return false
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()

        // Ignore WhatsApp call and system status notifications
        if (isCallOrSystemStatus(title, text)) {
            return true
        }

        return false
    }

    private fun isCallOrSystemStatus(title: String, text: String): Boolean {
        val combined = "$title $text".lowercase()
        return combined.contains("incoming voice call") ||
                combined.contains("incoming video call") ||
                combined.contains("ongoing call") ||
                combined.contains("missed voice call") ||
                combined.contains("missed video call") ||
                combined.contains("calling...") ||
                combined.contains("backup in progress") ||
                combined.contains("backing up") ||
                combined.contains("checking for new messages") ||
                combined.contains("whatsapp web is currently active") ||
                combined.contains("whatsapp web")
    }

    /**
     * Extracts all legitimate messages from the notification.
     * Returns a list because MessagingStyle notifications can contain recent conversation entries.
     */
    fun extractMessages(sbn: StatusBarNotification, source: SupportedApp): List<ExtractedMessage> {
        val notification = sbn.notification ?: return emptyList()
        val postTime = if (sbn.postTime > 0) sbn.postTime else System.currentTimeMillis()
        val extras = notification.extras
        val defaultTitle = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()

        // 1. Try NotificationCompat.MessagingStyle (Primary for modern WhatsApp)
        val messagingStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification)
        if (messagingStyle != null && messagingStyle.messages.isNotEmpty()) {
            val conversationTitle = messagingStyle.conversationTitle?.toString()?.trim().orEmpty()
            val result = mutableListOf<ExtractedMessage>()

            for (msg in messagingStyle.messages) {
                val body = msg.text?.toString()?.trim().orEmpty()
                if (isValidMessageBody(body)) {
                    val personName = msg.person?.name?.toString()?.trim().orEmpty()
                    val sender = when {
                        personName.isNotBlank() && conversationTitle.isNotBlank() ->
                            "$personName ($conversationTitle)"
                        personName.isNotBlank() -> personName
                        conversationTitle.isNotBlank() -> conversationTitle
                        defaultTitle.isNotBlank() -> defaultTitle
                        else -> source.displayName
                    }
                    val timestamp = if (msg.timestamp > 0) msg.timestamp else postTime
                    result.add(
                        ExtractedMessage(
                            source = source.displayName,
                            sender = sender,
                            message = body,
                            timestamp = timestamp
                        )
                    )
                }
            }
            if (result.isNotEmpty()) {
                return result
            }
        }

        // 2. Fallback to standard Notification extras (SMS, InboxStyle, or simple notifications)
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim().orEmpty()
        val normalText = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        var messageBody = if (bigText.isNotBlank()) bigText else normalText

        if (messageBody.isBlank() || !isValidMessageBody(messageBody)) {
            // Check Notification.EXTRA_TEXT_LINES (InboxStyle fallback)
            val lines = extras?.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            if (!lines.isNullOrEmpty()) {
                val lastLine = lines.lastOrNull()?.toString()?.trim().orEmpty()
                if (isValidMessageBody(lastLine)) {
                    messageBody = lastLine
                }
            }
        }

        if (!isValidMessageBody(messageBody)) {
            return emptyList()
        }

        // Clean trailing "(X messages)" from title if present
        val cleanedTitle = defaultTitle.replace(Regex("\\s*\\(\\d+\\s*(?:new\\s+)?messages?\\)$", RegexOption.IGNORE_CASE), "").trim()
        val sender = if (cleanedTitle.isNotBlank()) cleanedTitle else source.displayName

        return listOf(
            ExtractedMessage(
                source = source.displayName,
                sender = sender,
                message = messageBody,
                timestamp = postTime
            )
        )
    }

    private fun isValidMessageBody(body: String): Boolean {
        if (body.isBlank()) return false
        if (SUMMARY_PATTERN.matcher(body).matches()) return false
        if (GENERIC_PLACEHOLDER_PATTERN.matcher(body).matches()) return false
        if (body.equals("WhatsApp", ignoreCase = true)) return false
        return true
    }
}
