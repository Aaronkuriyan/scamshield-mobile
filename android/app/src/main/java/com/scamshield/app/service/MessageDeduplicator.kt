package com.scamshield.app.service

import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe deduplication engine across SMS and WhatsApp channels.
 * Prevents identical incoming notifications/broadcasts from being processed or analyzed multiple times.
 */
object MessageDeduplicator {

    // Cache of hash keys to timestamp
    private val processedCache = ConcurrentHashMap<String, Long>()
    private const val CACHE_EXPIRY_MS = 10 * 60 * 1000L // 10 minutes

    /**
     * Checks if a message has already been processed within the expiry window.
     * If not, marks it as processed and returns false.
     */
    fun isDuplicate(source: String, sender: String, message: String): Boolean {
        val now = System.currentTimeMillis()
        val cleanSender = sender.trim().lowercase()
        val cleanMessage = message.trim()
        val key = "$source:$cleanSender:${cleanMessage.hashCode()}"

        val lastSeen = processedCache[key]
        if (lastSeen != null && (now - lastSeen) < CACHE_EXPIRY_MS) {
            return true
        }

        processedCache[key] = now

        // Cleanup old entries if cache grows
        if (processedCache.size > 250) {
            val iterator = processedCache.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (now - entry.value > CACHE_EXPIRY_MS) {
                    iterator.remove()
                }
            }
        }

        return false
    }

    /**
     * Clears the deduplication cache (useful for testing).
     */
    fun clear() {
        processedCache.clear()
    }
}
