package com.example.offlinechat

import com.example.offlinechat.routing.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictiveAndMobilityTest {

    @Test
    fun testEWMAPredictionStabilityTracking() {
        val predictionEngine = EWMAPredictionEngine(alpha = 0.3)

        // Simulate stable peer with low latency & 100% delivery success
        for (i in 1..10) {
            predictionEngine.recordPacketDeliveryResult("Node-Reliable", latencyMs = 25L, success = true)
        }

        // Simulate noisy peer with frequent failures and high latency
        for (i in 1..10) {
            predictionEngine.recordPacketDeliveryResult("Node-Unstable", latencyMs = 300L, success = (i % 2 == 0))
        }
        predictionEngine.recordPeerConnectionEvent("Node-Unstable", 1000L, disconnected = true)
        predictionEngine.recordPeerConnectionEvent("Node-Unstable", 1000L, disconnected = true)

        val stableScore = predictionEngine.calculatePredictedStability("Node-Reliable")
        val unstableScore = predictionEngine.calculatePredictedStability("Node-Unstable")

        assertTrue("Stable node stability must exceed unstable node stability", stableScore > unstableScore)
        assertTrue(stableScore >= 0.7f)
        assertTrue(unstableScore <= 0.5f)
    }

    @Test
    fun testEncounterProbabilityCalculation() {
        val tracker = EncounterTracker()

        // Record frequent encounters for Node-Friend
        for (i in 1..5) {
            tracker.recordEncounter("Node-Friend")
        }

        val probFriend = tracker.getEncounterProbability("Node-Friend")
        val probStranger = tracker.getEncounterProbability("Node-Stranger")

        assertTrue(probFriend > probStranger)
    }

    @Test
    fun testPredictionExplainabilityOutput() {
        val predictionEngine = EWMAPredictionEngine()
        predictionEngine.recordPacketDeliveryResult("Node-Relay", 30L, true)

        val candidate = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-Relay",
            nextHopName = "Relay Alpha",
            viaTransport = "BLE_MESH",
            metrics = RouteMetrics(hopCount = 1, averageLatencyMs = 30L, batteryLevel = 85)
        )

        val explanation = predictionEngine.explainRouteDecision(candidate)
        assertTrue(explanation.contains("Relay Alpha"))
        assertTrue(explanation.contains("Predicted Stability"))
    }
}
