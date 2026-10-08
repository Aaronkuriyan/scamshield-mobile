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
            title = "Bank Account Block & OTP Phishing",
            sender = "SBI-ALERT",
            packageName = "com.google.android.apps.messaging",
            content = "Dear Customer, your SBI bank account is blocked today. Click http://bit.ly/sbi-unblock immediately and send your OTP to verify KYC.",
            expectedType = "HIGH RISK SCAM"
        ),
        TestCase(
            title = "Electricity Power Disconnection",
            sender = "BESCOM-BILL",
            packageName = "com.google.android.apps.messaging",
            content = "Dear Consumer, your electricity power will be disconnected tonight at 9:30 PM due to unpaid bill. Immediately call electricity officer at 9876543210.",
            expectedType = "HIGH RISK SCAM"
        ),
        TestCase(
            title = "Google Pay UPI PIN Fraud",
            sender = "+91 9988776655",
            packageName = "com.whatsapp",
            content = "Approve collect request of Rs 5000 on PhonePe or enter UPI PIN to receive your cashback reward now.",
            expectedType = "HIGH RISK SCAM"
        ),
        TestCase(
            title = "KBC Lottery Lucky Draw",
            sender = "+91 9123456789",
            packageName = "com.whatsapp",
            content = "Congratulations! You won Rs 25,00,000 in KBC Lucky Draw. Pay Rs 5,000 processing fee to claim your prize immediately.",
            expectedType = "HIGH RISK SCAM"
        ),
        TestCase(
            title = "Legitimate Bank Advisory (No Alert)",
            sender = "HDFC-BANK",
            packageName = "com.google.android.apps.messaging",
            content = "Dear Customer, never share your OTP, PIN or password with anyone. HDFC Bank never asks for confidential codes.",
            expectedType = "SAFE ADVISORY"
        ),
        TestCase(
            title = "Normal Family Conversation (No Alert)",
            sender = "Granddaughter Ananya",
            packageName = "com.whatsapp",
            content = "Hey Grandpa! Just reached home. Did you have your evening tea? Call me later!",
            expectedType = "SAFE CHAT"
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
