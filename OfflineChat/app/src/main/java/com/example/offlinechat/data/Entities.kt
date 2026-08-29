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
    val lastMessageAt: Long
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
    val encryptedPayload: String, // Stored encrypted at rest as required by prompt
    val timestamp: Long,
    val status: String, // PENDING, SENDING, SENT, DELIVERED, READ, FAILED
    val hopTrace: String = "[]" // JSON serialized list of HopRecord audit trail
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
