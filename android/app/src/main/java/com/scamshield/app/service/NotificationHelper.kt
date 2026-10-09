package com.scamshield.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.scamshield.app.MainActivity
import com.scamshield.app.R
import com.scamshield.app.data.local.ScanRecordEntity

object NotificationHelper {

    private const val CHANNEL_ID = "scamshield_threat_alerts"
    private const val CHANNEL_NAME = "SCAMSHIELD Threat Warnings"
    private const val CHANNEL_DESC = "Urgent alerts when a high-risk scam message is detected"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showThreatAlert(context: Context, record: ScanRecordEntity) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.scamshield.app.ACTION_VIEW_THREAT"
            putExtra("EXTRA_THREAT_ID", record.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            record.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val locCategory = com.scamshield.app.util.LocalizationHelper.getLocalizedCategory(context, record.category)
        val locRec = com.scamshield.app.util.LocalizationHelper.getLocalizedRecommendation(context, record.recommendation, record.category, record.classification)
        val title = "⚠️ SCAM ALERT (${record.riskScore}/100)"
        val content = "$locCategory: $locRec"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$locCategory\n\n$locRec"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(0xEF4444)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(record.id.toInt(), builder.build())
    }
}
