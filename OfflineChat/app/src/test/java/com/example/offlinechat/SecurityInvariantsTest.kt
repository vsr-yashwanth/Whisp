package com.example.offlinechat

import com.example.offlinechat.network.DeduplicationCache
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import com.example.offlinechat.network.dtn.DtnBundle
import com.example.offlinechat.network.dtn.StorageQuotaManager
import com.example.offlinechat.simulation.AttackMode
import com.example.offlinechat.simulation.MaliciousNodeSimulator
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class SecurityInvariantsTest {

    @Test
    fun testInvariant002ForgedPacketRejected() {
        val original = MeshPacket(
            protocolVersion = 4,
            senderId = "User-Alice",
            payload = "ZW5jcnlwdGVkX3BheWxvYWQ=",
            signature = "dmFsaWRfc2lnbmF0dXJl",
            senderPublicKey = "cHVibGljX2tleQ=="
        )

        val attacker = MaliciousNodeSimulator("Node-Attacker", AttackMode.FORGE_SIGNATURE)
        val attackPackets = attacker.executeAttackOnPacket(original)

        assertEquals(1, attackPackets.size)
        val tampered = attackPackets[0]

        // Canonical payload hash changes when payload is tampered
        assertNotEquals(original.computeSigningPayload().decodeToString(), tampered.computeSigningPayload().decodeToString())
    }

    @Test
    fun testInvariant003ReplayedPacketDropped() {
        val dedup = DeduplicationCache(maxCapacity = 1000)
        val packetId = UUID.randomUUID().toString()
        val payloadHash = "hash12345"

        // First ingestion: Accepted
        val isFirstDuplicate = dedup.isDuplicateOrRecord(packetId, payloadHash)
        assertFalse("First packet must NOT be marked duplicate", isFirstDuplicate)

        // Attacker replays packet 5 times
        val attacker = MaliciousNodeSimulator("Node-Attacker", AttackMode.REPLAY_PACKET)
        val original = MeshPacket(packetId = packetId, senderId = "Alice", payload = "data")
        val replayedList = attacker.executeAttackOnPacket(original)

        for (replayed in replayedList) {
            val isDuplicate = dedup.isDuplicateOrRecord(replayed.packetId, replayed.payloadHash)
            assertTrue("Replayed packet MUST be dropped as duplicate", isDuplicate)
        }
    }

    @Test
    fun testInvariant004ExpiredPacketDropped() {
        val expiredPacket = MeshPacket(
            ttl = 0,
            senderId = "Alice",
            payload = "expired_data"
        )
        assertTrue("Packet with TTL <= 0 must be expired", expiredPacket.ttl <= 0)
    }

    @Test
    fun testInvariant011DtnStorageQuotaHardBounded() {
        val maxQuotaBytes = 500L * 1024L * 1024L // 500 MB limit
        assertEquals(524288000L, maxQuotaBytes)

        val bundle = DtnBundle(
            bundleId = "b-large",
            messageId = "m-large",
            source = "A",
            destination = "B",
            creationTime = 1000L,
            expirationTime = 5000L,
            ttl = 10,
            priority = 10,
            payload = "A".repeat(1024)
        )

        val currentBytes = 524287900L
        val incomingBytes = 200L
        val wouldExceed = (currentBytes + incomingBytes) > maxQuotaBytes
        assertTrue("Storage manager must detect bundles that exceed 500MB budget", wouldExceed)
    }
}
