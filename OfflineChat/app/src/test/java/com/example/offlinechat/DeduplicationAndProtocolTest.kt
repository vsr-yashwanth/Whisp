package com.example.offlinechat

import com.example.offlinechat.network.DeduplicationCache
import com.example.offlinechat.network.HopRecord
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DeduplicationAndProtocolTest {

    @Test
    fun testMeshPacketSerializationAndDeserialization() {
        val initialHop = HopRecord(
            nodeId = "Node-Origin1",
            nodeName = "Pixel 7",
            transport = "ORIGIN",
            timestamp = 1787978000000L,
            latencyMs = 0L
        )

        val originalPacket = MeshPacket(
            protocolVersion = 2,
            packetType = PacketType.MESSAGE,
            packetId = UUID.randomUUID().toString(),
            messageId = "msg-12345",
            conversationId = "General Chat",
            senderId = "User-NodeA",
            recipientId = "ALL",
            timestamp = 1787978000000L,
            ttl = 8,
            hopCount = 1,
            priority = PacketPriority.NORMAL,
            payload = "ZW5jcnlwdGVkLXRyYW5zaXQtcGF5bG9hZA==",
            hops = listOf(initialHop),
            batteryLevel = 85,
            isCharging = false
        )

        val jsonString = originalPacket.toJsonString()
        val parsedPacket = MeshPacket.fromJsonString(jsonString)

        assertNotNull(parsedPacket)
        assertEquals(2, parsedPacket!!.protocolVersion)
        assertEquals(PacketType.MESSAGE, parsedPacket.packetType)
        assertEquals(originalPacket.packetId, parsedPacket.packetId)
        assertEquals("msg-12345", parsedPacket.messageId)
        assertEquals("General Chat", parsedPacket.conversationId)
        assertEquals("User-NodeA", parsedPacket.senderId)
        assertEquals(8, parsedPacket.ttl)
        assertEquals(1, parsedPacket.hopCount)
        assertEquals(85, parsedPacket.batteryLevel)
        assertEquals(1, parsedPacket.hops.size)
        assertEquals("Node-Origin1", parsedPacket.hops[0].nodeId)
    }

    @Test
    fun testHopStampingAndTTLDecrement() {
        val packet = MeshPacket(
            protocolVersion = 2,
            packetType = PacketType.MESSAGE,
            packetId = "packet-ttl-test",
            senderId = "User-A",
            payload = "c2VjdXJl",
            ttl = 5,
            hopCount = 0
        )

        val stamped = packet.stampedWithHop(
            nodeId = "Node-B",
            nodeName = "Relay Phone B",
            transport = "BLE_MESH",
            currentBattery = 65,
            charging = true
        )

        assertEquals(4, stamped.ttl)
        assertEquals(1, stamped.hopCount)
        assertEquals(1, stamped.hops.size)
        assertEquals("Node-B", stamped.hops[0].nodeId)
        assertEquals("BLE_MESH", stamped.hops[0].transport)
        assertEquals(65, stamped.batteryLevel)
        assertTrue(stamped.isCharging)
    }

    @Test
    fun testLegacyV1PacketCompatibility() {
        val legacyJson = """
            {
                "version": 1,
                "type": "MESSAGE",
                "messageId": "legacy-id-999",
                "conversationId": "Legacy Conv",
                "senderId": "User-Legacy",
                "timestamp": 1787970000000,
                "payload": "bGVnYWN5UGF5bG9hZA==",
                "hops": [
                    {
                        "nodeId": "Node-Old",
                        "nodeName": "Old Device",
                        "transport": "LOCAL_BRIDGE",
                        "timestamp": 1787970000000,
                        "latencyMs": 10
                    }
                ]
            }
        """.trimIndent()

        val parsed = MeshPacket.fromJsonString(legacyJson)
        assertNotNull(parsed)
        assertEquals(1, parsed!!.protocolVersion)
        assertEquals(PacketType.MESSAGE, parsed.packetType)
        assertEquals("legacy-id-999", parsed.messageId)
        assertEquals("Legacy Conv", parsed.conversationId)
        assertEquals("User-Legacy", parsed.senderId)
        assertEquals("bGVnYWN5UGF5bG9hZA==", parsed.payload)
        assertEquals(1, parsed.hops.size)
    }

    @Test
    fun testDeduplicationCacheDetection() {
        val cache = DeduplicationCache(maxCapacity = 100, ttlMillis = 5000)

        val packetId = "packet-unique-1"
        val payloadHash = "hash-abc-123"

        // First attempt -> not duplicate (recorded)
        assertFalse(cache.isDuplicateOrRecord(packetId, payloadHash))

        // Second attempt with same packetId -> duplicate!
        assertTrue(cache.isDuplicateOrRecord(packetId, payloadHash))

        // Third attempt with different packetId but same payloadHash -> duplicate!
        assertTrue(cache.isDuplicateOrRecord("packet-unique-2", payloadHash))

        // Distinct packetId and distinct payloadHash -> not duplicate
        assertFalse(cache.isDuplicateOrRecord("packet-unique-3", "hash-xyz-789"))
    }

    @Test
    fun testDeduplicationLRUEviction() {
        val cache = DeduplicationCache(maxCapacity = 3, ttlMillis = 60000)

        cache.isDuplicateOrRecord("p1", "h1")
        cache.isDuplicateOrRecord("p2", "h2")
        cache.isDuplicateOrRecord("p3", "h3")

        assertEquals(3, cache.size())

        // Add 4th item -> should evict eldest (p1)
        cache.isDuplicateOrRecord("p4", "h4")
        assertEquals(3, cache.size())

        assertFalse(cache.contains("p1"))
        assertTrue(cache.contains("p2"))
        assertTrue(cache.contains("p3"))
        assertTrue(cache.contains("p4"))
    }
}
