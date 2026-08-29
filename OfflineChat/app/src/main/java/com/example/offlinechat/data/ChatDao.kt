package com.example.offlinechat.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversation(conversation: Conversation)

    @Query("SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    fun getConversations(): Flow<List<Conversation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND status = 'PENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(conversationId: String): List<Message>
    
    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessageCount(): Flow<Int>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    // Store-and-Forward Buffered Packet Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBufferedPacket(packet: BufferedPacket)

    @Query("SELECT * FROM buffered_packets WHERE recipientId = :recipientId OR recipientId = 'ALL' ORDER BY priority DESC, createdAt ASC")
    suspend fun getBufferedPacketsForRecipient(recipientId: String): List<BufferedPacket>

    @Query("SELECT * FROM buffered_packets WHERE expiresAt > :now ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    suspend fun getTopBufferedPackets(now: Long, limit: Int = 100): List<BufferedPacket>

    @Query("DELETE FROM buffered_packets WHERE packetId = :packetId")
    suspend fun deleteBufferedPacket(packetId: String)

    @Query("DELETE FROM buffered_packets WHERE expiresAt <= :now")
    suspend fun deleteExpiredBufferedPackets(now: Long): Int

    @Query("UPDATE buffered_packets SET retryCount = retryCount + 1 WHERE packetId = :packetId")
    suspend fun incrementRetryCount(packetId: String)

    @Query("SELECT COUNT(*) FROM buffered_packets")
    fun getBufferedPacketCount(): Flow<Int>
}
