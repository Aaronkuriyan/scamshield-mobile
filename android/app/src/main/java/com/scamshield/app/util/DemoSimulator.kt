package com.scamshield.app.util

import android.content.Context
import com.scamshield.app.data.local.ScanRecordEntity
import com.scamshield.app.data.repository.ScanRepository
import com.scamshield.app.service.NotificationHelper
import com.scamshield.app.service.TextToSpeechHelper

object DemoSimulator {

    data class TestCase(
        val title: String,
        val sender: String,
        val packageName: String,
        val content: String,
        val expectedType: String
    )

    val PRELOADED_TEST_CASES = listOf(
        TestCase(
            title = "Test 1: Safe Normal Chat",
            sender = "Priya",
            packageName = "com.whatsapp",
            content = "Hey, are we meeting at 5 PM?",
            expectedType = "SAFE"
        ),
        TestCase(
            title = "Test 2: Suspicious Delivery Payment",
            sender = "SPEED-POST",
            packageName = "com.google.android.apps.messaging",
            content = "Your delivery failed. Pay ₹50 using this link to reschedule: http://track-parcel-redeliver.in",
            expectedType = "SUSPICIOUS"
        ),
        TestCase(
            title = "Test 3: High Risk Bank Account KYC Phishing",
            sender = "SBI-ALERT",
            packageName = "com.google.android.apps.messaging",
            content = "Your bank account will be blocked today. Verify your KYC immediately using this link: http://sbi-kyc-update.net",
            expectedType = "HIGH RISK"
        ),
        TestCase(
            title = "Test 4: Legitimate Bank Advisory (Never Share OTP)",
            sender = "HDFC-BANK",
            packageName = "com.google.android.apps.messaging",
            content = "Never share your bank OTP with anyone. Bank officials will never ask for it.",
            expectedType = "SAFE (NO FALSE POSITIVE)"
        ),
        TestCase(
            title = "Test 5: UPI Collect / PIN Scam",
            sender = "+91 9876543210",
            packageName = "com.whatsapp",
            content = "Approve collect request of ₹5,000 on PhonePe or enter UPI PIN to receive lottery cash reward.",
            expectedType = "HIGH RISK"
        )
    )

    suspend fun runSimulation(
        context: Context,
        testCase: TestCase,
        ttsHelper: TextToSpeechHelper? = null
    ): ScanRecordEntity {
        val repository = ScanRepository(context)
        val record = repository.processIncomingNotification(
            content = testCase.content,
            sender = testCase.sender,
            packageName = testCase.packageName
        )

        if (record.isThreat) {
            NotificationHelper.showThreatAlert(context, record)
            ttsHelper?.speakWarning(record.recommendation)
        }

        return record
    }
}
