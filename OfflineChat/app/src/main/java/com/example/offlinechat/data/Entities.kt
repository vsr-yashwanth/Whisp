package com.example.offlinechat.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val peerId: String,
    val createdAt: Long,
    val lastMessageAt: Long,
    val conversationType: String = "GENERAL", // GENERAL, DIRECT, EMERGENCY_SOS
    val displayName: String = id,
    val participantBlockchainId: String = ""
)

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Conversation::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("conversationId")]
)
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val encryptedPayload: String, // Stored encrypted at rest
    val timestamp: Long,
    val status: String, // PENDING, SENDING, SENT, DELIVERED, READ, FAILED
    val hopTrace: String = "[]", // JSON serialized list of HopRecord audit trail
    val senderBlockchainId: String = "",
    val recipientBlockchainId: String = "ALL"
)

@Entity(
    tableName = "buffered_packets",
    indices = [Index("recipientId"), Index("expiresAt"), Index("priority")]
)
data class BufferedPacket(
    @PrimaryKey val packetId: String,
    val messageId: String,
    val recipientId: String,       // Target node ID or "ALL"
    val conversationId: String,
    val priority: Int,             // Higher number = higher priority (e.g. 100 for SOS)
    val ttl: Int,
    val createdAt: Long,
    val expiresAt: Long,
    val retryCount: Int = 0,
    val rawJsonPayload: String     // Full serialized MeshPacket string (transit-encrypted)
)

@Entity(
    tableName = "dtn_bundles",
    indices = [
        Index("destination"),
        Index("expirationTime"),
        Index("priority"),
        Index("custodyState")
    ]
)
data class DtnBundleEntity(
    @PrimaryKey val bundleId: String,
    val messageId: String,
    val source: String,
    val destination: String,
    val creationTime: Long,
    val expirationTime: Long,
    val ttl: Int,
    val priority: Int,
    val hopCount: Int,
    val replicationCount: Int,
    val maxReplications: Int,
    val payload: String,
    val payloadHash: String,
    val custodyState: String, // RECEIVED, STORED, FORWARDING, FORWARDED, DELIVERED, EXPIRED, DROPPED
    val deliveryProbability: Float,
    val sizeBytes: Long,
    val rawJson: String
)

@Entity(
    tableName = "peer_encounters",
    indices = [Index("peerId"), Index("lastSeen")]
)
data class PeerEncounterEntity(
    @PrimaryKey val peerId: String,
    val firstSeen: Long,
    val lastSeen: Long,
    val encounterCount: Int,
    val averageIntervalSeconds: Long,
    val lastTransport: String,
    val estimatedStability: Float
)

@Entity(
    tableName = "crdt_operations",
    indices = [Index("documentId"), Index("lamportClock"), Index("actorId")]
)
data class CrdtOperationEntity(
    @PrimaryKey val opId: String,
    val documentId: String,
    val actorId: String,
    val lamportClock: Long,
    val timestamp: Long,
    val operationType: String, // ADD, REMOVE, UPDATE
    val key: String,
    val valueJson: String
)

@Entity(tableName = "network_epochs")
data class NetworkEpochEntity(
    @PrimaryKey val epochNumber: Long,
    val timestamp: Long,
    val detectedPartitionCount: Int,
    val knownMemberCount: Int,
    val stateHash: String
)
