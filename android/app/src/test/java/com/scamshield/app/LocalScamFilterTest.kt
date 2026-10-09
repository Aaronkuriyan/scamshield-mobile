package com.scamshield.app

import com.scamshield.app.engine.LocalScamFilter
import org.junit.Assert.*
import org.junit.Test

class LocalScamFilterTest {

    @Test
    fun testSafeNormalChat() {
        val result = LocalScamFilter.evaluateLocally("Hey Grandpa, are we having tea together?")
        assertTrue(result.isSafe)
        assertEquals("SAFE", result.classification)
        assertFalse(result.shouldQueryBackend)
        assertEquals(0, result.riskScore)
    }

    @Test
    fun testProtectiveBankAdvisoryNotFlagged() {
        val advisory = "Dear Customer, never share your OTP, PIN or password with anyone. Bank never calls asking for codes."
        val result = LocalScamFilter.evaluateLocally(advisory)
        assertTrue("Protective warning must be safe, got score: ${result.riskScore}", result.isSafe)
        assertEquals("SAFE", result.classification)
        assertFalse(result.shouldQueryBackend)
    }

    @Test
    fun testOtpPhishingScam() {
        val scam = "Your account is blocked today. Click http://bit.ly/sbi-fix immediately and enter your OTP to verify KYC."
        val result = LocalScamFilter.evaluateLocally(scam)
        assertFalse(result.isSafe)
        assertEquals("SCAM", result.classification)
        assertTrue(result.shouldQueryBackend)
        assertTrue(result.riskScore >= 70)
        assertTrue(result.indicators.isNotEmpty())
    }

    @Test
    fun testUpiPinFraud() {
        val scam = "Approve collect request of Rs 5000 on PhonePe or enter UPI PIN to receive cashback reward now."
        val result = LocalScamFilter.evaluateLocally(scam)
        assertFalse(result.isSafe)
        assertEquals("SCAM", result.classification)
        assertEquals("UPI Scam", result.category)
        assertTrue(result.riskScore >= 70)
    }

    @Test
    fun testElectricityDisconnectionExtortion() {
        val scam = "Dear Consumer, your electricity power will be disconnected tonight at 9:30 PM due to unpaid bill. Immediately contact officer."
        val result = LocalScamFilter.evaluateLocally(scam)
        assertFalse(result.isSafe)
        assertEquals("SCAM", result.classification)
        assertTrue(result.riskScore >= 70)
    }

    @Test
    fun testRemoteAccessTakeover() {
        val scam = "Urgent: Customer support team needs you to install AnyDesk app to verify your phone banking."
        val result = LocalScamFilter.evaluateLocally(scam)
        assertFalse(result.isSafe)
        assertEquals("SCAM", result.classification)
        assertEquals("Remote Access Scam", result.category)
        assertTrue(result.riskScore >= 70)
    }

    @Test
    fun testPromptSafeMessage() {
        val safe = "Hey, I'll call you at 5 PM."
        val result = LocalScamFilter.evaluateLocally(safe)
        assertTrue(result.isSafe)
        assertEquals("SAFE", result.classification)
        assertEquals(0, result.riskScore)
    }

    @Test
    fun testPromptSuspiciousMessage() {
        val suspicious = "Your delivery failed. Pay ₹50 using this link to reschedule your delivery."
        val result = LocalScamFilter.evaluateLocally(suspicious)
        assertTrue("Suspicious message should have risk > 25, got ${result.riskScore}", result.riskScore in 25..69)
        assertEquals("SUSPICIOUS", result.classification)
    }

    @Test
    fun testPromptHighRiskMessage() {
        val highRisk = "Your bank account will be blocked today. Verify your KYC immediately using this link."
        val result = LocalScamFilter.evaluateLocally(highRisk)
        assertFalse(result.isSafe)
        assertTrue("High risk score must be >= 70, got ${result.riskScore}", result.riskScore >= 70)
        assertEquals("SCAM", result.classification)
    }
}
