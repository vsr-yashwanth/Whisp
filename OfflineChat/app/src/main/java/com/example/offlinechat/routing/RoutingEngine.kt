package com.example.offlinechat.routing

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

class RoutingEngine(
    private val config: RoutingConfig = RoutingConfig(),
    val predictionEngine: PredictionEngine = EWMAPredictionEngine(),
    val encounterTracker: EncounterTracker = EncounterTracker()
) {
    // DestinationNodeId -> List of Candidate Routes
    private val routingTable = ConcurrentHashMap<String, MutableList<RouteCandidate>>()

    /**
     * Calculates a multi-factor penalty score for a route candidate.
     * LOWER SCORE = BETTER ROUTE.
     *
     * In V3, the score blends instantaneous network telemetry with
     * predictive link stability and encounter probability.
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

        // 5. Predictive Stability Factor (Higher stability = lower penalty score)
        val predictedStability = predictionEngine.calculatePredictedStability(candidate.nextHopNodeId)
        val instabilityPenalty = (1.0f - predictedStability) * 60.0f
        score += instabilityPenalty

        // 6. Destination Encounter Probability (Frequent encounters with destination reduce penalty)
        val encounterProb = encounterTracker.getEncounterProbability(candidate.destinationNodeId)
        val encounterBonus = encounterProb * 20.0f
        score -= encounterBonus

        // 7. Transport Type preference (Fast local Wi-Fi vs BLE)
        when (candidate.viaTransport) {
            "LOCAL_BRIDGE", "WIFI_DIRECT" -> score -= 10.0f // Bonus for high-bandwidth local Wi-Fi
            "BLE_MESH" -> score += 5.0f
            "GLOBAL_RELAY" -> score += 8.0f // WAN cloud fallback
        }

        return score
    }

    fun explainRoute(candidate: RouteCandidate): String {
        return predictionEngine.explainRouteDecision(candidate)
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
        encounterTracker.recordEncounter(candidate.nextHopNodeId, candidate.viaTransport)
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
                predictionEngine.recordPeerConnectionEvent(nextHopNodeId, 0L, disconnected = true)
                Log.d("RoutingEngine", "Invalidated route to ($destinationNodeId) via ($nextHopNodeId)")
            }
        }
    }

    /**
     * Invalidate all routes using a specific next-hop node (e.g. when peer disconnects)
     */
    fun invalidateAllRoutesViaNextHop(nextHopNodeId: String) {
        routingTable.forEach { (_, candidates) ->
            synchronized(candidates) {
                candidates.removeAll { it.nextHopNodeId == nextHopNodeId }
            }
        }
        predictionEngine.recordPeerConnectionEvent(nextHopNodeId, 0L, disconnected = true)
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
