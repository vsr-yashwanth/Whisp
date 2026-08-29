package com.example.offlinechat;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0016\u001a\u00020\u0017H\u0016J\u0018\u0010\u0018\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u001a2\b\b\u0002\u0010\u001b\u001a\u00020\u001cR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0006@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u001e\u0010\u000b\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\n@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u001e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u0005\u001a\u00020\u000e@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u001e\u0010\u0013\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u0012@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0015\u00a8\u0006\u001e"}, d2 = {"Lcom/example/offlinechat/OfflineChatApp;", "Landroid/app/Application;", "()V", "appScope", "Lkotlinx/coroutines/CoroutineScope;", "<set-?>", "Lcom/example/offlinechat/security/CryptoManager;", "cryptoManager", "getCryptoManager", "()Lcom/example/offlinechat/security/CryptoManager;", "Lcom/example/offlinechat/data/ChatDatabase;", "database", "getDatabase", "()Lcom/example/offlinechat/data/ChatDatabase;", "Lcom/example/offlinechat/network/HybridMeshTransport;", "transport", "getTransport", "()Lcom/example/offlinechat/network/HybridMeshTransport;", "Lcom/example/offlinechat/network/WebServerManager;", "webServerManager", "getWebServerManager", "()Lcom/example/offlinechat/network/WebServerManager;", "onCreate", "", "processIncomingRawPacket", "data", "", "transportType", "", "Companion", "app_debug"})
public final class OfflineChatApp extends android.app.Application {
    private com.example.offlinechat.data.ChatDatabase database;
    private com.example.offlinechat.security.CryptoManager cryptoManager;
    private com.example.offlinechat.network.HybridMeshTransport transport;
    private com.example.offlinechat.network.WebServerManager webServerManager;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope appScope = null;
    private static com.example.offlinechat.OfflineChatApp instance;
    @org.jetbrains.annotations.NotNull
    public static final com.example.offlinechat.OfflineChatApp.Companion Companion = null;
    
    public OfflineChatApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.data.ChatDatabase getDatabase() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.security.CryptoManager getCryptoManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.network.HybridMeshTransport getTransport() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.network.WebServerManager getWebServerManager() {
        return null;
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    public final void processIncomingRawPacket(@org.jetbrains.annotations.NotNull
    byte[] data, @org.jetbrains.annotations.NotNull
    java.lang.String transportType) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u001e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0003\u001a\u00020\u0004@BX\u0086.\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007\u00a8\u0006\b"}, d2 = {"Lcom/example/offlinechat/OfflineChatApp$Companion;", "", "()V", "<set-?>", "Lcom/example/offlinechat/OfflineChatApp;", "instance", "getInstance", "()Lcom/example/offlinechat/OfflineChatApp;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final com.example.offlinechat.OfflineChatApp getInstance() {
            return null;
        }
    }
}