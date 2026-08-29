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
}
