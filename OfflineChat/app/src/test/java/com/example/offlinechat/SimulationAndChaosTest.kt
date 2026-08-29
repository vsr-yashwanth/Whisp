package com.example.offlinechat

import com.example.offlinechat.simulation.ChaosConfig
import com.example.offlinechat.simulation.SimulatedNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulationAndChaosTest {

    @Test
    fun testScenarioAStableMesh() {
        val net = SimulatedNetwork("Scenario A — Stable Mesh", ChaosConfig(seed = 12345L, packetLossRate = 0.0f))

        // Create 10 nodes in a mesh
        for (i in 1..10) {
            net.addNode("Node-$i", "Device $i")
        }
        for (i in 1..9) {
            net.connectNodes("Node-$i", "Node-${i + 1}", latencyMs = 15L)
        }
        net.connectNodes("Node-10", "Node-1", latencyMs = 20L)

        // Dispatch 50 packets across the mesh
        for (i in 1..50) {
            val src = "Node-${(i % 10) + 1}"
            val dst = "Node-${((i + 1) % 10) + 1}"
            net.dispatchPacket(src, dst, "Payload $i")
        }

        val report = net.generateBenchmarkReport()
        assertEquals(50, report.totalPacketsSent)
        assertEquals(50, report.totalPacketsDelivered)
        assertEquals(100.0f, report.deliveryRatePercent, 0.1f)
        assertTrue(report.averageLatencyMs > 0.0)
    }

    @Test
    fun testScenarioCPartitionAndHealing() {
        val net = SimulatedNetwork("Scenario C — Partition", ChaosConfig(seed = 99999L))

        for (i in 1..10) net.addNode("A-$i", "Group A $i")
        for (i in 1..10) net.addNode("B-$i", "Group B $i")

        // Inter-connect within groups
        for (i in 1..9) {
            net.connectNodes("A-$i", "A-${i + 1}")
            net.connectNodes("B-$i", "B-${i + 1}")
        }
        // Gateway bridge link
        net.connectNodes("A-10", "B-1")

        // Simulate Partition: Bridge link severed
        net.simulatePartition(
            groupAIds = (1..10).map { "A-$it" }.toSet(),
            groupBIds = (1..10).map { "B-$it" }.toSet()
        )

        // Packets sent during partition are buffered in DTN storage
        net.dispatchPacket("A-1", "B-10", "Cross-partition message")

        // Reconnect and heal partition
        net.healPartition()

        val report = net.generateBenchmarkReport()
        assertEquals(1, report.totalPacketsDelivered) // DTN auto-flush on reconnect
        assertEquals(1, report.partitionsEncountered)
    }

    @Test
    fun testSimulationReproducibilityWithSeeds() {
        val config1 = ChaosConfig(seed = 849217L, packetLossRate = 0.1f, nodeFailureProbability = 0.05f)
        val config2 = ChaosConfig(seed = 849217L, packetLossRate = 0.1f, nodeFailureProbability = 0.05f)

        val sim1 = SimulatedNetwork("Test 1", config1)
        val sim2 = SimulatedNetwork("Test 2", config2)

        for (i in 1..5) {
            sim1.addNode("N-$i", "Node $i")
            sim2.addNode("N-$i", "Node $i")
        }
        for (i in 1..4) {
            sim1.connectNodes("N-$i", "N-${i + 1}")
            sim2.connectNodes("N-$i", "N-${i + 1}")
        }

        for (i in 1..20) {
            sim1.dispatchPacket("N-1", "N-5", "Data $i")
            sim2.dispatchPacket("N-1", "N-5", "Data $i")
        }

        val report1 = sim1.generateBenchmarkReport()
        val report2 = sim2.generateBenchmarkReport()

        // Same random seed must produce identical delivery rates and latency metrics!
        assertEquals(report1.totalPacketsDelivered, report2.totalPacketsDelivered)
        assertEquals(report1.deliveryRatePercent, report2.deliveryRatePercent, 0.001f)
        assertEquals(report1.averageLatencyMs, report2.averageLatencyMs, 0.001)
    }
}
