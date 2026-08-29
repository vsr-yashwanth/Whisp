package com.example.offlinechat.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u000f\u001a\u00020\u000eR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/example/offlinechat/network/WebServerManager;", "", "context", "Landroid/content/Context;", "chatDao", "Lcom/example/offlinechat/data/ChatDao;", "transport", "Lcom/example/offlinechat/network/PeerTransport;", "cryptoManager", "Lcom/example/offlinechat/security/CryptoManager;", "(Landroid/content/Context;Lcom/example/offlinechat/data/ChatDao;Lcom/example/offlinechat/network/PeerTransport;Lcom/example/offlinechat/security/CryptoManager;)V", "server", "Lio/ktor/server/engine/ApplicationEngine;", "start", "", "stop", "app_debug"})
public final class WebServerManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.data.ChatDao chatDao = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.network.PeerTransport transport = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.security.CryptoManager cryptoManager = null;
    @org.jetbrains.annotations.Nullable
    private io.ktor.server.engine.ApplicationEngine server;
    
    public WebServerManager(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.data.ChatDao chatDao, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.PeerTransport transport, @org.jetbrains.annotations.NotNull
    com.example.offlinechat.security.CryptoManager cryptoManager) {
        super();
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
}