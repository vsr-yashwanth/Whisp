package com.example.offlinechat.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

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
