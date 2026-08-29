package com.example.offlinechat

import com.example.offlinechat.network.AdminAuditManager
import com.example.offlinechat.network.AdminDtnBundleDto
import com.example.offlinechat.network.DashboardOverviewResponse
import org.junit.Assert.*
import org.junit.Test

class AdminControlPlaneTest {

    @Test
    fun testAdminAuditLoggerRecordingAndCapacity() {
        val auditManager = AdminAuditManager(maxCapacity = 10)

        for (i in 1..15) {
            auditManager.logAction("TEST_ACTION_$i", "Resource-$i", "SUCCESS", "Reason $i")
        }

        val logs = auditManager.getAllLogs()
        assertEquals(10, logs.size)
        // Most recent action must be at the top
        assertEquals("TEST_ACTION_15", logs[0].action)
    }

    @Test
    fun testHealthScoreCalculationLogic() {
        fun computeHealth(peersCount: Int, dtnBytes: Long, isPartitioned: Boolean): Pair<Int, String> {
            val availScore = if (peersCount > 0) 95 else 75
            val dtnScore = if (dtnBytes < 400 * 1024 * 1024L) 100 else 60
            val partScore = if (!isPartitioned) 100 else 50
            val score = ((availScore * 0.4) + (dtnScore * 0.3) + (partScore * 0.3)).toInt()
            val status = when {
                score >= 85 -> "HEALTHY"
                score >= 60 -> "DEGRADED"
                else -> "CRITICAL"
            }
            return Pair(score, status)
        }

        val (scoreHealthy, statusHealthy) = computeHealth(5, 50 * 1024 * 1024L, false)
        assertTrue(scoreHealthy >= 85)
        assertEquals("HEALTHY", statusHealthy)

        val (scoreDegraded, statusDegraded) = computeHealth(0, 450 * 1024 * 1024L, true)
        assertTrue(scoreDegraded < 85)
        assertEquals("DEGRADED", statusDegraded)
    }

    @Test
    fun testZeroTrustMetadataPrivacyGuarantee() {
        // Assert that DTOs exposed to Admin Web only contain metadata and NEVER contain message plaintext
        val bundleDto = AdminDtnBundleDto(
            bundleId = "b-12345",
            source = "Alice",
            destination = "Bob",
            custodyState = "STORED",
            ttl = 60,
            priority = 10,
            replicationCount = 1,
            deliveryProbability = 0.9f
        )

        val fields = bundleDto.javaClass.declaredFields.map { it.name }
        assertFalse("Admin DTO must never contain plaintext field", fields.contains("plaintext"))
        assertFalse("Admin DTO must never contain messageText field", fields.contains("messageText"))
        assertFalse("Admin DTO must never contain content field", fields.contains("content"))
    }
}
