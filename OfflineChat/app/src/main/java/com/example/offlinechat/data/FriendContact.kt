package com.example.offlinechat.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "friends",
    indices = [Index("blockchainId"), Index("username")]
)
data class FriendContact(
    @PrimaryKey val username: String,
    val blockchainId: String,
    val displayName: String,
    val role: String = "USER",
    val addedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val unreadCount: Int = 0,
    val lastMessageSnippet: String? = null,
    val lastMessageTime: Long = 0L
)
