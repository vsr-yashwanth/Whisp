package com.example.offlinechat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001\u001eB%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0011\u0010\u0013\u001a\u00020\u0014H\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0015J\u0018\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\r2\b\u0010\u0018\u001a\u0004\u0018\u00010\tH\u0002J\u0006\u0010\u0019\u001a\u00020\u0014J\u0018\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u001b\u001a\u00020\t2\b\b\u0002\u0010\u001c\u001a\u00020\u001dR\u001a\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001f"}, d2 = {"Lcom/example/offlinechat/ChatViewModel;", "Landroidx/lifecycle/ViewModel;", "transport", "Lcom/example/offlinechat/network/PeerTransport;", "cryptoManager", "Lcom/example/offlinechat/security/CryptoManager;", "chatDao", "Lcom/example/offlinechat/data/ChatDao;", "currentConversationId", "", "(Lcom/example/offlinechat/network/PeerTransport;Lcom/example/offlinechat/security/CryptoManager;Lcom/example/offlinechat/data/ChatDao;Ljava/lang/String;)V", "_messages", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/offlinechat/ChatMessage;", "messages", "Lkotlinx/coroutines/flow/StateFlow;", "getMessages", "()Lkotlinx/coroutines/flow/StateFlow;", "flushPendingMessages", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "parseHopTrace", "Lcom/example/offlinechat/network/HopRecord;", "jsonStr", "sendKeyExchange", "sendMessage", "text", "isEmergency", "", "Factory", "app_debug"})
public final class ChatViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.network.PeerTransport transport = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.security.CryptoManager cryptoManager = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.data.ChatDao chatDao = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String currentConversationId = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.offlinechat.ChatMessage>> _messages = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.ChatMessage>> messages = null;
    
    public ChatViewModel(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.PeerTransport transport, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.security.CryptoManager cryptoManager, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.ChatDao chatDao, @org.jetbrains.annotations.NotNull
    java.lang.String currentConversationId) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.ChatMessage>> getMessages() {
        return null;
    }
    
    private final java.util.List<com.example.offlinechat.network.HopRecord> parseHopTrace(java.lang.String jsonStr) {
        return null;
    }
    
    private final java.lang.Object flushPendingMessages(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    public final void sendMessage(@org.jetbrains.annotations.NotNull
    java.lang.String text, boolean isEmergency) {
    }
    
    public final void sendKeyExchange() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ%\u0010\u000b\u001a\u0002H\f\"\b\b\u0000\u0010\f*\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u0002H\f0\u000fH\u0016\u00a2\u0006\u0002\u0010\u0010R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/example/offlinechat/ChatViewModel$Factory;", "Landroidx/lifecycle/ViewModelProvider$Factory;", "transport", "Lcom/example/offlinechat/network/PeerTransport;", "cryptoManager", "Lcom/example/offlinechat/security/CryptoManager;", "chatDao", "Lcom/example/offlinechat/data/ChatDao;", "conversationId", "", "(Lcom/example/offlinechat/network/PeerTransport;Lcom/example/offlinechat/security/CryptoManager;Lcom/example/offlinechat/data/ChatDao;Ljava/lang/String;)V", "create", "T", "Landroidx/lifecycle/ViewModel;", "modelClass", "Ljava/lang/Class;", "(Ljava/lang/Class;)Landroidx/lifecycle/ViewModel;", "app_debug"})
    public static final class Factory implements androidx.lifecycle.ViewModelProvider.Factory {
        @org.jetbrains.annotations.NotNull
        private final com.example.offlinechat.network.PeerTransport transport = null;
        @org.jetbrains.annotations.NotNull
        private final com.example.offlinechat.security.CryptoManager cryptoManager = null;
        @org.jetbrains.annotations.NotNull
        private final com.example.offlinechat.data.ChatDao chatDao = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String conversationId = null;
        
        public Factory(@org.jetbrains.annotations.NotNull
        com.example.offlinechat.network.PeerTransport transport, @org.jetbrains.annotations.NotNull
        com.example.offlinechat.security.CryptoManager cryptoManager, @org.jetbrains.annotations.NotNull
        com.example.offlinechat.data.ChatDao chatDao, @org.jetbrains.annotations.NotNull
        java.lang.String conversationId) {
            super();
        }
        
        @java.lang.Override
        @kotlin.Suppress(names = {"UNCHECKED_CAST"})
        @org.jetbrains.annotations.NotNull
        public <T extends androidx.lifecycle.ViewModel>T create(@org.jetbrains.annotations.NotNull
        java.lang.Class<T> modelClass) {
            return null;
        }
    }
}