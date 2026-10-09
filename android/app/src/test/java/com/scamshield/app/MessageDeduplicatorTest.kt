package com.scamshield.app

import com.scamshield.app.service.MessageDeduplicator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MessageDeduplicatorTest {

    @Before
    fun setUp() {
        MessageDeduplicator.clear()
    }

    @Test
    fun testFirstMessageNotDuplicate() {
        val isDup = MessageDeduplicator.isDuplicate("WhatsApp", "Alice", "Hello there!")
        assertFalse("First message must not be a duplicate", isDup)
    }

    @Test
    fun testIdenticalMessageIsDuplicate() {
        MessageDeduplicator.isDuplicate("WhatsApp", "Alice", "Hello there!")
        val isDup = MessageDeduplicator.isDuplicate("WhatsApp", "Alice", "Hello there!")
        assertTrue("Subsequent identical message must be detected as duplicate", isDup)
    }

    @Test
    fun testDifferentMessageFromSameSenderNotDuplicate() {
        MessageDeduplicator.isDuplicate("WhatsApp", "Alice", "Hello there!")
        val isDup = MessageDeduplicator.isDuplicate("WhatsApp", "Alice", "Are you free at 5 PM?")
        assertFalse("Different message from same sender must not be duplicate", isDup)
    }

    @Test
    fun testCrossChannelDeduplication() {
        // SMS received via Telephony broadcast
        val smsDup1 = MessageDeduplicator.isDuplicate("SMS", "+919876543210", "Your OTP is 1234")
        assertFalse(smsDup1)

        // Same SMS arriving via notification listener
        val smsDup2 = MessageDeduplicator.isDuplicate("SMS", "+919876543210", "Your OTP is 1234")
        assertTrue("Same SMS from notification must be detected as duplicate", smsDup2)
    }
}
