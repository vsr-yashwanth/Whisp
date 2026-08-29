package com.example.offlinechat.routing

import java.util.concurrent.ConcurrentHashMap
import kotlin.math.exp

data class PeerHistoricalStats(
    val peerId: String,
    var samplesCount: Int = 0,
    var ewmaLatencyMs: Double = 30.0,
    var ewmaPacketLossRate: Double = 0.0,
    var disconnectCount: Int = 0,
    var totalConnectionDurationMs: Long = 60_000L,
    var successfulDeliveries: Int = 0,
    var failedDeliveries: Int = 0,
    var lastSeenTimestamp: Long = System.currentTimeMillis(),
    var encounterCount: Int = 1
)

interface PredictionEngine {
    fun recordPacketDeliveryResult(peerId: String, latencyMs: Long, success: Boolean)
    fun recordPeerConnectionEvent(peerId: String, durationMs: Long, disconnected: Boolean)
    fun recordPeerEncounter(peerId: String)
    fun calculatePredictedStability(peerId: String): Float
    fun explainRouteDecision(candidate: RouteCandidate): String
}

class EWMAPredictionEngine(
    private val alpha: Double = 0.2 // Weight for Exponentially Weighted Moving Average
) : PredictionEngine {

    private val peerStats = ConcurrentHashMap<String, PeerHistoricalStats>()

    override fun recordPacketDeliveryResult(peerId: String, latencyMs: Long, success: Boolean) {
        val stats = peerStats.computeIfAbsent(peerId) { PeerHistoricalStats(peerId) }
        synchronized(stats) {
            stats.samplesCount++
            stats.ewmaLatencyMs = (alpha * latencyMs) + ((1.0 - alpha) * stats.ewmaLatencyMs)
            val lossInstant = if (success) 0.0 else 1.0
            stats.ewmaPacketLossRate = (alpha * lossInstant) + ((1.0 - alpha) * stats.ewmaPacketLossRate)
            if (success) stats.successfulDeliveries++ else stats.failedDeliveries++
            stats.lastSeenTimestamp = System.currentTimeMillis()
        }
    }

    override fun recordPeerConnectionEvent(peerId: String, durationMs: Long, disconnected: Boolean) {
        val stats = peerStats.computeIfAbsent(peerId) { PeerHistoricalStats(peerId) }
        synchronized(stats) {
            stats.totalConnectionDurationMs += durationMs
            if (disconnected) stats.disconnectCount++
            stats.lastSeenTimestamp = System.currentTimeMillis()
        }
    }

    override fun recordPeerEncounter(peerId: String) {
        val stats = peerStats.computeIfAbsent(peerId) { PeerHistoricalStats(peerId) }
        synchronized(stats) {
            stats.encounterCount++
            stats.lastSeenTimestamp = System.currentTimeMillis()
        }
    }

    override fun calculatePredictedStability(peerId: String): Float {
        val stats = peerStats[peerId] ?: return 0.5f // Neutral default for new peer
        synchronized(stats) {
            // 1. Delivery Success Ratio (0.0 to 1.0)
            val totalDeliveries = stats.successfulDeliveries + stats.failedDeliveries
            val deliveryRatio = if (totalDeliveries > 0) stats.successfulDeliveries.toDouble() / totalDeliveries else 0.8

            // 2. Latency Quality (1.0 = <50ms, decreasing for high latency)
            val latencyQuality = (1.0 - (stats.ewmaLatencyMs / 1000.0)).coerceIn(0.1, 1.0)

            // 3. Loss Quality (1.0 = 0% loss)
            val lossQuality = (1.0 - stats.ewmaPacketLossRate).coerceIn(0.0, 1.0)

            // 4. Disconnect penalty factor
            val disconnectPenalty = (stats.disconnectCount * 0.05).coerceAtMost(0.4)

            val stability = (deliveryRatio * 0.4 + latencyQuality * 0.3 + lossQuality * 0.3) - disconnectPenalty
            return stability.toFloat().coerceIn(0.05f, 1.0f)
        }
    }

    override fun explainRouteDecision(candidate: RouteCandidate): String {
        val stats = peerStats[candidate.nextHopNodeId]
        val stability = calculatePredictedStability(candidate.nextHopNodeId)
        val sb = StringBuilder()
        sb.append("Route via ${candidate.nextHopName} (${candidate.viaTransport}):\n")
        sb.append("• Predicted Stability: ${"%.1f".format(stability * 100)}%\n")
        sb.append("• Hops: ${candidate.metrics.hopCount} | Latency: ${candidate.metrics.averageLatencyMs}ms\n")

        if (stats != null) {
            sb.append("• Historical Deliveries: ${stats.successfulDeliveries} OK / ${stats.failedDeliveries} FAIL\n")
            if (stats.disconnectCount > 2) {
                sb.append("⚠️ Frequent disconnections observed (${stats.disconnectCount} drops)\n")
            }
        } else {
            sb.append("• Newly discovered peer (provisional routing score)\n")
        }

        if (candidate.metrics.batteryLevel in 0..20 && !candidate.metrics.isCharging) {
            sb.append("⚠️ Low battery relay device (${candidate.metrics.batteryLevel}%)\n")
        }

        return sb.toString().trim()
    }
}
