package com.scamshield.app.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppFormatters {

    fun getSourceAppName(context: Context, packageName: String): String {
        return when {
            packageName.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
            packageName.contains("telegram", ignoreCase = true) -> "Telegram"
            packageName.contains("messaging", ignoreCase = true) -> "Messages"
            packageName.contains("mms", ignoreCase = true) -> "SMS"
            packageName.contains("signal", ignoreCase = true) -> "Signal"
            packageName.contains("instagram", ignoreCase = true) -> "Instagram"
            packageName.equals("SMS", ignoreCase = true) -> "SMS"
            else -> {
                try {
                    val pm = context.packageManager
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (e: Exception) {
                    "Messages"
                }
            }
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        if (diff < 60_000L) {
            return "Just now"
        }
        if (diff < 3600_000L) {
            val mins = (diff / 60_000L).toInt()
            return "$mins min ago"
        }

        val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return if (msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        ) {
            "Today • " + timeFormat.format(Date(timestamp))
        } else if (msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1
        ) {
            "Yesterday • " + timeFormat.format(Date(timestamp))
        } else {
            val fullFormat = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
            fullFormat.format(Date(timestamp))
        }
    }
}
