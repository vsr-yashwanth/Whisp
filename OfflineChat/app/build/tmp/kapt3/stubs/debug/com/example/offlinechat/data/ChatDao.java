package com.example.offlinechat.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0016\bg\u0018\u00002\u00020\u0001J\u0011\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u0019\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\rJ\u0014\u0010\u000e\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000fH\'J\u000e\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\n0\u000fH\'J\u001f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u00102\u0006\u0010\u0015\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u0014\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00100\u000fH\'J\u001c\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00110\u00100\u000f2\u0006\u0010\u0019\u001a\u00020\u0007H\'J\u001f\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00110\u00102\u0006\u0010\u0019\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ)\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00140\u00102\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\u001c\u001a\u00020\nH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001dJ\u000e\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\n0\u000fH\'J\u0019\u0010\u001f\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ\u0019\u0010 \u001a\u00020\u00032\u0006\u0010!\u001a\u00020\u0014H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\"J\u0019\u0010#\u001a\u00020\u00032\u0006\u0010$\u001a\u00020\u0017H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010%J\u0019\u0010&\u001a\u00020\u00032\u0006\u0010\'\u001a\u00020\u0011H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010(J!\u0010)\u001a\u00020\u00032\u0006\u0010*\u001a\u00020\u00072\u0006\u0010+\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010,\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006-"}, d2 = {"Lcom/example/offlinechat/data/ChatDao;", "", "clearAllMessages", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteBufferedPacket", "packetId", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteExpiredBufferedPackets", "", "now", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllMessages", "Lkotlinx/coroutines/flow/Flow;", "", "Lcom/example/offlinechat/data/Message;", "getBufferedPacketCount", "getBufferedPacketsForRecipient", "Lcom/example/offlinechat/data/BufferedPacket;", "recipientId", "getConversations", "Lcom/example/offlinechat/data/Conversation;", "getMessagesForConversation", "conversationId", "getPendingMessages", "getTopBufferedPackets", "limit", "(JILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTotalMessageCount", "incrementRetryCount", "insertBufferedPacket", "packet", "(Lcom/example/offlinechat/data/BufferedPacket;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertConversation", "conversation", "(Lcom/example/offlinechat/data/Conversation;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertMessage", "message", "(Lcom/example/offlinechat/data/Message;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateMessageStatus", "messageId", "status", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@androidx.room.Dao
public abstract interface ChatDao {
    
    @androidx.room.Insert(onConflict = 5)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertConversation(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.Conversation conversation, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM conversations ORDER BY lastMessageAt DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.offlinechat.data.Conversation>> getConversations();
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertMessage(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.Message message, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.offlinechat.data.Message>> getMessagesForConversation(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId);
    
    @androidx.room.Query(value = "SELECT * FROM messages WHERE conversationId = :conversationId AND status = \'PENDING\' ORDER BY timestamp ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getPendingMessages(@org.jetbrains.annotations.NotNull
    java.lang.String conversationId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.offlinechat.data.Message>> $completion);
    
    @androidx.room.Query(value = "UPDATE messages SET status = :status WHERE id = :messageId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object updateMessageStatus(@org.jetbrains.annotations.NotNull
    java.lang.String messageId, @org.jetbrains.annotations.NotNull
    java.lang.String status, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM messages")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getTotalMessageCount();
    
    @androidx.room.Query(value = "SELECT * FROM messages ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.example.offlinechat.data.Message>> getAllMessages();
    
    @androidx.room.Query(value = "DELETE FROM messages")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object clearAllMessages(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object insertBufferedPacket(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.BufferedPacket packet, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM buffered_packets WHERE recipientId = :recipientId OR recipientId = \'ALL\' ORDER BY priority DESC, createdAt ASC")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getBufferedPacketsForRecipient(@org.jetbrains.annotations.NotNull
    java.lang.String recipientId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.offlinechat.data.BufferedPacket>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM buffered_packets WHERE expiresAt > :now ORDER BY priority DESC, createdAt ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTopBufferedPackets(long now, int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.util.List<com.example.offlinechat.data.BufferedPacket>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM buffered_packets WHERE packetId = :packetId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteBufferedPacket(@org.jetbrains.annotations.NotNull
    java.lang.String packetId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM buffered_packets WHERE expiresAt <= :now")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object deleteExpiredBufferedPackets(long now, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "UPDATE buffered_packets SET retryCount = retryCount + 1 WHERE packetId = :packetId")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object incrementRetryCount(@org.jetbrains.annotations.NotNull
    java.lang.String packetId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM buffered_packets")
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getBufferedPacketCount();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}