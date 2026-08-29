package com.example.offlinechat

import com.example.offlinechat.routing.RouteCandidate
import com.example.offlinechat.routing.RouteMetrics
import com.example.offlinechat.routing.RoutingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoutingEngineTest {

    @Test
    fun testBestRouteSelectionBasedOnMultiFactorScoring() {
        val engine = RoutingEngine()

        // Path A: via Node-B (1 hop, 20ms, 90% battery, Wi-Fi Direct)
        val routeA = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-B",
            nextHopName = "Relay Phone B",
            viaTransport = "WIFI_DIRECT",
            metrics = RouteMetrics(
                hopCount = 1,
                averageLatencyMs = 20L,
                packetLossRate = 0.01f,
                batteryLevel = 90,
                isCharging = false,
                transportType = "WIFI_DIRECT"
            )
        )

        // Path B: via Node-C (3 hops, 350ms, 70% battery, BLE)
        val routeB = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-C",
            nextHopName = "Relay Phone C",
            viaTransport = "BLE_MESH",
            metrics = RouteMetrics(
                hopCount = 3,
                averageLatencyMs = 350L,
                packetLossRate = 0.15f,
                batteryLevel = 70,
                isCharging = false,
                transportType = "BLE_MESH"
            )
        )

        engine.registerOrUpdateRoute(routeA)
        engine.registerOrUpdateRoute(routeB)

        val bestRoute = engine.getBestRoute("Node-Target")
        assertNotNull(bestRoute)
        assertEquals("Node-B", bestRoute!!.nextHopNodeId)
        assertTrue(engine.calculateRouteScore(routeA) < engine.calculateRouteScore(routeB))
    }

    @Test
    fun testBatteryAwareRelayAvoidance() {
        val engine = RoutingEngine()

        // Route A: 1 hop but Relay Node is dying (8% battery)
        val dyingRelayRoute = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-Dying",
            nextHopName = "Low Battery Phone",
            viaTransport = "BLE_MESH",
            metrics = RouteMetrics(
                hopCount = 1,
                averageLatencyMs = 30L,
                batteryLevel = 8, // <10% critical battery!
                isCharging = false
            )
        )

        // Route B: 2 hops but Relay Node has 95% battery and is charging
        val healthyRelayRoute = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-Healthy",
            nextHopName = "High Battery Phone",
            viaTransport = "BLE_MESH",
            metrics = RouteMetrics(
                hopCount = 2,
                averageLatencyMs = 60L,
                batteryLevel = 95,
                isCharging = true
            )
        )

        engine.registerOrUpdateRoute(dyingRelayRoute)
        engine.registerOrUpdateRoute(healthyRelayRoute)

        // The engine MUST avoid the dying node and choose the healthy node to preserve the network
        val selected = engine.getBestRoute("Node-Target")
        assertNotNull(selected)
        assertEquals("Node-Healthy", selected!!.nextHopNodeId)
    }

    @Test
    fun testRouteInvalidationAndSelfHealingFallback() {
        val engine = RoutingEngine()

        val primaryRoute = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-Primary",
            nextHopName = "Primary Gateway",
            viaTransport = "LOCAL_BRIDGE",
            metrics = RouteMetrics(hopCount = 1, averageLatencyMs = 10L)
        )

        val backupRoute = RouteCandidate(
            destinationNodeId = "Node-Target",
            nextHopNodeId = "Node-Backup",
            nextHopName = "Backup Relay",
            viaTransport = "BLE_MESH",
            metrics = RouteMetrics(hopCount = 2, averageLatencyMs = 80L)
        )

        engine.registerOrUpdateRoute(primaryRoute)
        engine.registerOrUpdateRoute(backupRoute)

        assertEquals("Node-Primary", engine.getBestRoute("Node-Target")?.nextHopNodeId)

        // Simulate Node-Primary disconnection
        engine.invalidateRoute("Node-Target", "Node-Primary")

        // Route self-heals by instantly selecting backup
        val recoveredRoute = engine.getBestRoute("Node-Target")
        assertNotNull(recoveredRoute)
        assertEquals("Node-Backup", recoveredRoute!!.nextHopNodeId)

        // If backup also fails
        engine.invalidateRoute("Node-Target", "Node-Backup")
        assertNull(engine.getBestRoute("Node-Target"))
    }
}
