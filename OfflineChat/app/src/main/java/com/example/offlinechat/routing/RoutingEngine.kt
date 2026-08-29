package com.example.offlinechat.routing

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

class RoutingEngine(
    private val config: RoutingConfig = RoutingConfig()
) {
    // DestinationNodeId -> List of Candidate Routes
    private val routingTable = ConcurrentHashMap<String, MutableList<RouteCandidate>>()

    /**
     * Calculates a penalty score for a route candidate.
     * LOWER SCORE = BETTER ROUTE.
     */
    fun calculateRouteScore(candidate: RouteCandidate): Float {
        val m = candidate.metrics
        var score = 0.0f

        // 1. Hop Count Penalty
        score += m.hopCount * config.wHop

        // 2. Latency Penalty
        score += m.averageLatencyMs * config.wLatency

        // 3. Packet Loss Penalty (0.0 to 1.0)
        score += m.packetLossRate * config.wPacketLoss

        // 4. Battery Penalty (Encourages battery conservation of relay peers)
        if (!m.isCharging && m.batteryLevel >= 0) {
            when {
                m.batteryLevel < 10 -> score += config.wCriticalBattery
                m.batteryLevel < 20 -> score += config.wLowBattery
                m.batteryLevel < 50 -> score += 10.0f
            }
        }

        // 5. Instability & Delivery Failure Penalty
        val failureRate = (1.0f - m.recentDeliverySuccessRate).coerceIn(0.0f, 1.0f)
        score += failureRate * config.wInstability

        // 6. Transport Type preference (Fast local Wi-Fi vs BLE)
        when (candidate.viaTransport) {
            "LOCAL_BRIDGE", "WIFI_DIRECT" -> score -= 5.0f // Bonus for high-bandwidth local Wi-Fi
            "BLE_MESH" -> score += 5.0f
            "GLOBAL_RELAY" -> score += 8.0f // WAN cloud fallback
        }

        return score
    }

    /**
     * Registers or updates a route candidate in the routing table.
     */
    fun registerOrUpdateRoute(candidate: RouteCandidate) {
        val candidates = routingTable.computeIfAbsent(candidate.destinationNodeId) { mutableListOf() }
        synchronized(candidates) {
            candidates.removeAll { it.nextHopNodeId == candidate.nextHopNodeId && it.viaTransport == candidate.viaTransport }
            candidates.add(candidate)
        }
    }

    /**
     * Returns the best candidate route for a given destination based on lowest penalty score.
     */
    fun getBestRoute(destinationNodeId: String): RouteCandidate? {
        val candidates = routingTable[destinationNodeId] ?: return null
        val now = System.currentTimeMillis()

        synchronized(candidates) {
            // Remove stale routes
            candidates.removeAll { now - it.lastUpdated > config.maxRouteAgeMillis }
            if (candidates.isEmpty()) return null

            return candidates.minByOrNull { calculateRouteScore(it) }
        }
    }

    /**
     * Invalidates a route when a peer fails to acknowledge or disconnects.
     */
    fun invalidateRoute(destinationNodeId: String, nextHopNodeId: String) {
        val candidates = routingTable[destinationNodeId] ?: return
        synchronized(candidates) {
            val removed = candidates.removeAll { it.nextHopNodeId == nextHopNodeId }
            if (removed) {
                Log.d("RoutingEngine", "Invalidated route to ($destinationNodeId) via ($nextHopNodeId)")
            }
        }
    }

    /**
     * Invalidate all routes using a specific next-hop node (e.g. when peer disconnects)
     */
    fun invalidateAllRoutesViaNextHop(nextHopNodeId: String) {
        routingTable.forEach { (dest, candidates) ->
            synchronized(candidates) {
                candidates.removeAll { it.nextHopNodeId == nextHopNodeId }
            }
        }
    }

    fun getAllActiveRoutes(): Map<String, List<RouteCandidate>> {
        val now = System.currentTimeMillis()
        val result = mutableMapOf<String, List<RouteCandidate>>()
        routingTable.forEach { (dest, list) ->
            synchronized(list) {
                list.removeAll { now - it.lastUpdated > config.maxRouteAgeMillis }
                if (list.isNotEmpty()) {
                    result[dest] = list.toList()
                }
            }
        }
        return result
    }

    fun clear() {
        routingTable.clear()
    }
}
