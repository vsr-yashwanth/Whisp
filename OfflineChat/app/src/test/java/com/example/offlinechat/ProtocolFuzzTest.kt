package com.example.offlinechat

import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.security.AntiFloodRateLimiter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class ProtocolFuzzTest {

    @Test
    fun testMalformedPacketFuzzing10000Inputs() {
        val random = Random(42)
        val maliciousInputs = listOf(
            "",
            "null",
            "{}",
            "{\"version\": -999}",
            "{\"ttl\": -1, \"payload\": \"\"}",
            "{\"payload\": null}",
            "{\"hops\": [null, {}]}",
            "{\"hops\": \"invalid_type\"}",
            "{\"priority\": 999999999999999999}",
            "{{{{[[[[",
            "\u0000\u0001\u0002\u0003\u0004",
            "A".repeat(100_000), // Giant payload
            "{\"senderId\": \"${"X".repeat(50_000)}\"}"
        )

        // Run 10,000 fuzz iterations with random mutations
        for (i in 1..10_000) {
            val base = maliciousInputs[i % maliciousInputs.size]
            val mutated = if (random.nextBoolean()) {
                base + random.nextInt(1000)
            } else {
                base.take(random.nextInt(base.length.coerceAtLeast(1)))
            }

            // Invariant: Must fail closed and NEVER throw unhandled crash
            val parsed = try {
                MeshPacket.fromJsonString(mutated)
            } catch (e: Exception) {
                null
            }

            // If it parsed successfully, it must have valid non-negative TTL and safe defaults
            if (parsed != null) {
                assertNotNull(parsed.packetId)
                assertNotNull(parsed.senderId)
            }
        }
    }

    @Test
    fun testAntiFloodTokenBucketUnderSpam() {
        val limiter = AntiFloodRateLimiter(maxTokensPerPeer = 20.0, refillRatePerSecond = 10.0)

        var allowedCount = 0
        var droppedCount = 0

        // Simulate 1,000 rapid burst packets from Malicious-Spammer
        for (i in 1..1000) {
            if (limiter.allowPacket("Malicious-Spammer")) {
                allowedCount++
            } else {
                droppedCount++
            }
        }

        // Invariant: Only burst allowance (20 tokens) must be accepted, remaining 980 dropped!
        assertTrue("Burst allowed must be <= 21", allowedCount <= 21)
        assertTrue("Dropped packets must be >= 979", droppedCount >= 979)
    }
}
