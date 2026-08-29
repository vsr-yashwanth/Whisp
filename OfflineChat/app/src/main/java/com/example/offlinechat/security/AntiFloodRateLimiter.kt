package com.example.offlinechat.security

import java.util.concurrent.ConcurrentHashMap

/**
 * Token-Bucket Anti-Flooding Rate Limiter.
 * Prevents malicious or compromised nodes from spamming high-frequency packets
 * causing CPU, memory, or battery exhaustion.
 */
class AntiFloodRateLimiter(
    private val maxTokensPerPeer: Double = 30.0,
    private val refillRatePerSecond: Double = 15.0
) {

    private data class TokenBucket(
        var tokens: Double,
        var lastRefillTimestamp: Long
    )

    private val buckets = ConcurrentHashMap<String, TokenBucket>()

    /**
     * Checks if a packet from peerId is allowed.
     * Returns true if allowed, false if rate limit exceeded.
     */
    fun allowPacket(peerId: String): Boolean {
        val now = System.currentTimeMillis()
        val bucket = buckets.computeIfAbsent(peerId) {
            TokenBucket(tokens = maxTokensPerPeer, lastRefillTimestamp = now)
        }

        synchronized(bucket) {
            val elapsedSeconds = (now - bucket.lastRefillTimestamp).coerceAtLeast(0L) / 1000.0
            bucket.tokens = (bucket.tokens + elapsedSeconds * refillRatePerSecond).coerceAtMost(maxTokensPerPeer)
            bucket.lastRefillTimestamp = now

            return if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0
                true
            } else {
                false // Rate limited!
            }
        }
    }

    fun reset() {
        buckets.clear()
    }
}
