package com.example.offlinechat

import com.example.offlinechat.network.PartitionManager
import com.example.offlinechat.network.Peer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PartitionAndReconciliationTest {

    @Test
    fun testPartitionDetectionAndEpochAdvancement() {
        val manager = PartitionManager(localNodeId = "Local-Node")

        // Initial full mesh topology (4 peers)
        val initialPeers = listOf(
            Peer("P1", "Peer 1"),
            Peer("P2", "Peer 2"),
            Peer("P3", "Peer 3"),
            Peer("P4", "Peer 4")
        )
        manager.onPeerTopologyUpdated(initialPeers)
        assertEquals(1L, manager.partitionStatus.value.currentEpoch)
        assertFalse(manager.partitionStatus.value.isPartitioned)

        // Simulate partition: 3 peers suddenly lost, only 1 peer remains
        val partitionedPeers = listOf(
            Peer("P1", "Peer 1")
        )
        manager.onPeerTopologyUpdated(partitionedPeers)

        // Epoch must advance and partition state must be detected
        assertTrue(manager.partitionStatus.value.isPartitioned)
        assertEquals(2L, manager.partitionStatus.value.currentEpoch)

        // Simulate reconciliation: Network reconnects with all peers
        manager.onPeerTopologyUpdated(initialPeers)
        assertFalse(manager.partitionStatus.value.isPartitioned)
        assertEquals(3L, manager.partitionStatus.value.currentEpoch)
        assertEquals("RECONCILIATION_COMPLETE", manager.partitionStatus.value.reconciliationStatus)
    }

    @Test
    fun testEpochSyncHandling() {
        val manager = PartitionManager(localNodeId = "Local-Node")

        val syncPayload = org.json.JSONObject().apply {
            put("epoch", 15L)
            put("senderId", "Gateway-Node")
            put("timestamp", System.currentTimeMillis())
            put("memberCount", 20)
        }.toString()

        manager.handleIncomingEpochSync(syncPayload)
        assertEquals(15L, manager.partitionStatus.value.currentEpoch)
        assertEquals("STATE_RECONCILED", manager.partitionStatus.value.reconciliationStatus)
    }
}
