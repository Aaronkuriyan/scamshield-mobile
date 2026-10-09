package com.scamshield.app.util

import android.content.Context
import com.scamshield.app.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppFormatters {

    fun getSourceAppName(context: Context, packageName: String): String {
        return when {
            packageName.contains("whatsapp", ignoreCase = true) -> "WhatsApp"
            packageName.contains("telegram", ignoreCase = true) -> "Telegram"
            packageName.contains("messaging", ignoreCase = true) -> context.getString(R.string.app_messages)
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
                    context.getString(R.string.app_messages)
                }
            }
        }
    }

    fun formatRelativeTime(context: Context, timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        if (diff < 60_000L) {
            return context.getString(R.string.time_just_now)
        }
        if (diff < 3600_000L) {
            val mins = (diff / 60_000L).toInt().coerceAtLeast(1)
            return context.getString(R.string.time_min_ago, mins)
        }

        val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance().apply { timeInMillis = now }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return if (msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            msgCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        ) {
            context.getString(R.string.time_today, timeFormat.format(Date(timestamp)))
        } else if (msgCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1
        ) {
            context.getString(R.string.time_yesterday, timeFormat.format(Date(timestamp)))
        } else {
            val fullFormat = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
            fullFormat.format(Date(timestamp))
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        if (diff < 60_000L) {
            return "Just now"
        }
        if (diff < 3600_000L) {
            val mins = (diff / 60_000L).toInt().coerceAtLeast(1)
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
