package com.example.offlinechat

import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import com.example.offlinechat.network.PriorityPacketQueue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class StoreAndForwardTest {

    @Test
    fun testSOSPreemptionInPriorityQueue() {
        val queue = PriorityPacketQueue(maxCapacity = 100)

        val normalPacket1 = MeshPacket(
            packetId = "normal-1",
            senderId = "A",
            payload = "payload-normal-1",
            priority = PacketPriority.NORMAL
        )

        val normalPacket2 = MeshPacket(
            packetId = "normal-2",
            senderId = "A",
            payload = "payload-normal-2",
            priority = PacketPriority.NORMAL
        )

        val sosPacket = MeshPacket(
            packetId = "sos-urgent",
            packetType = PacketType.SOS,
            senderId = "A",
            payload = "EMERGENCY_HELP",
            priority = PacketPriority.SOS
        )

        val importantPacket = MeshPacket(
            packetId = "important-1",
            senderId = "A",
            payload = "payload-important",
            priority = PacketPriority.IMPORTANT
        )

        queue.enqueue(normalPacket1)
        queue.enqueue(normalPacket2)
        queue.enqueue(importantPacket)
        queue.enqueue(sosPacket) // Enqueued last!

        // SOS must be polled first despite being enqueued last
        val firstOut = queue.poll()
        assertNotNull(firstOut)
        assertEquals("sos-urgent", firstOut!!.packetId)
        assertEquals(PacketPriority.SOS, firstOut.priority)

        // Important must be polled second
        val secondOut = queue.poll()
        assertNotNull(secondOut)
        assertEquals("important-1", secondOut!!.packetId)
        assertEquals(PacketPriority.IMPORTANT, secondOut.priority)

        // Normal packets follow in FIFO order
        val thirdOut = queue.poll()
        assertNotNull(thirdOut)
        assertEquals("normal-1", thirdOut!!.packetId)

        val fourthOut = queue.poll()
        assertNotNull(fourthOut)
        assertEquals("normal-2", fourthOut!!.packetId)

        assertTrue(queue.isEmpty())
    }

    @Test
    fun testPriorityQueueCapacityAndEviction() {
        val smallQueue = PriorityPacketQueue(maxCapacity = 3)

        val n1 = MeshPacket(packetId = "n1", senderId = "A", payload = "1", priority = PacketPriority.NORMAL)
        val n2 = MeshPacket(packetId = "n2", senderId = "A", payload = "2", priority = PacketPriority.NORMAL)
        val n3 = MeshPacket(packetId = "n3", senderId = "A", payload = "3", priority = PacketPriority.NORMAL)

        assertTrue(smallQueue.enqueue(n1))
        assertTrue(smallQueue.enqueue(n2))
        assertTrue(smallQueue.enqueue(n3))
        assertEquals(3, smallQueue.size())

        // 4th normal packet cannot enter (rejected)
        val n4 = MeshPacket(packetId = "n4", senderId = "A", payload = "4", priority = PacketPriority.NORMAL)
        assertFalse(smallQueue.enqueue(n4))

        // SOS packet MUST evict lowest normal priority item to enter
        val sos = MeshPacket(packetId = "sos-evictor", senderId = "A", payload = "sos", priority = PacketPriority.SOS)
        assertTrue(smallQueue.enqueue(sos))
        assertEquals(3, smallQueue.size())

        // Top polled item must be the SOS packet
        val polled = smallQueue.poll()
        assertNotNull(polled)
        assertEquals("sos-evictor", polled!!.packetId)
    }

    @Test
    fun testStoreAndForwardBufferingAndFlushLogic() {
        val dispatchedPackets = mutableListOf<String>()
        val bufferedPacketsMap = mutableMapOf<String, MeshPacket>()

        // Mock sender function
        val mockSender: (ByteArray) -> Unit = { bytes ->
            val packet = MeshPacket.fromJsonString(String(bytes))
            if (packet != null) {
                dispatchedPackets.add(packet.packetId)
            }
        }

        // Simulate buffering packets for disconnected peer "Node-Charlie"
        val packetForCharlie1 = MeshPacket(
            packetId = "p-charlie-1",
            recipientId = "Node-Charlie",
            senderId = "Node-Alice",
            payload = "secret-1"
        )
        val packetForCharlie2 = MeshPacket(
            packetId = "p-charlie-2",
            recipientId = "Node-Charlie",
            senderId = "Node-Bob",
            payload = "secret-2"
        )
        val packetForDavid = MeshPacket(
            packetId = "p-david-1",
            recipientId = "Node-David",
            senderId = "Node-Alice",
            payload = "secret-3"
        )

        bufferedPacketsMap[packetForCharlie1.packetId] = packetForCharlie1
        bufferedPacketsMap[packetForCharlie2.packetId] = packetForCharlie2
        bufferedPacketsMap[packetForDavid.packetId] = packetForDavid

        assertEquals(3, bufferedPacketsMap.size)
        assertTrue(dispatchedPackets.isEmpty())

        // Event: "Node-Charlie" is discovered / connects!
        val discoveredPeerId = "Node-Charlie"
        val matchingPackets = bufferedPacketsMap.values.filter { it.recipientId == discoveredPeerId || it.recipientId == "ALL" }

        for (p in matchingPackets) {
            mockSender(p.toJsonString().toByteArray())
            bufferedPacketsMap.remove(p.packetId)
        }

        // Verify Charlie's packets were flushed and David's packet remains buffered
        assertEquals(2, dispatchedPackets.size)
        assertTrue(dispatchedPackets.contains("p-charlie-1"))
        assertTrue(dispatchedPackets.contains("p-charlie-2"))
        assertFalse(dispatchedPackets.contains("p-david-1"))

        assertEquals(1, bufferedPacketsMap.size)
        assertTrue(bufferedPacketsMap.containsKey("p-david-1"))
    }
}
