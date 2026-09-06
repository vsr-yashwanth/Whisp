package com.example.offlinechat.simulation

import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import java.util.UUID

enum class AttackMode {
    DROP_ALL,
    DROP_RANDOM,
    REPLAY_PACKET,
    FORGE_SIGNATURE,
    FAKE_ROUTE_POISON,
    FLOOD_SPAM
}

/**
 * Malicious Node Simulator for adversarial security and chaos testing.
 */
class MaliciousNodeSimulator(
    val nodeId: String,
    var activeAttack: AttackMode = AttackMode.REPLAY_PACKET
) {

    fun executeAttackOnPacket(originalPacket: MeshPacket): List<MeshPacket> {
        return when (activeAttack) {
            AttackMode.DROP_ALL -> {
                emptyList() // Blackhole attack
            }
            AttackMode.DROP_RANDOM -> {
                if (Math.random() < 0.5) emptyList() else listOf(originalPacket)
            }
            AttackMode.REPLAY_PACKET -> {
                // Replay attack: send original packet 10 times
                List(10) { originalPacket }
            }
            AttackMode.FORGE_SIGNATURE -> {
                // Tamper with payload while keeping bogus signature
                val forged = originalPacket.copy(
                    payload = "dGFtcGVyZWRfcGF5bG9hZA==",
                    signature = "ZmFrZV9zaWduYXR1cmVfaGVyZQ=="
                )
                listOf(forged)
            }
            AttackMode.FAKE_ROUTE_POISON -> {
                // Fabricate impossible battery level and 0ms latency to poison route tables
                val poisoned = originalPacket.copy(
                    batteryLevel = 100,
                    isCharging = true,
                    ttl = 50
                )
                listOf(poisoned)
            }
            AttackMode.FLOOD_SPAM -> {
                // Spam high frequency forged packets
                (1..50).map { i ->
                    MeshPacket(
                        protocolVersion = 4,
                        packetType = PacketType.MESSAGE,
                        packetId = UUID.randomUUID().toString(),
                        senderId = nodeId,
                        recipientId = "ALL",
                        payload = "Zmxvb2RfcGF5bG9hZA==",
                        priority = PacketPriority.NORMAL
                    )
                }
            }
        }
    }
}
