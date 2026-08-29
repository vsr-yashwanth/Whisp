package com.example.offlinechat.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0012\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000e\u0018\u00002\u00020\u0001B!\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005\u00a2\u0006\u0002\u0010\bJ\u0016\u0010%\u001a\u00020\u00072\u0006\u0010&\u001a\u00020\u00062\u0006\u0010\'\u001a\u00020\nJ\b\u0010(\u001a\u00020\u0007H\u0002J\b\u0010)\u001a\u00020\u0007H\u0002J\u0006\u0010*\u001a\u00020\u0007J\b\u0010+\u001a\u00020\u0007H\u0002J\b\u0010,\u001a\u00020\u0007H\u0002J\u0006\u0010-\u001a\u00020\u0007J\b\u0010.\u001a\u00020\u0007H\u0002J\b\u0010/\u001a\u00020\u0007H\u0002J\u0010\u00100\u001a\u00020\u00072\u0006\u00101\u001a\u00020\u0014H\u0002R\u000e\u0010\t\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082D\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00140\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0015\u001a\u0004\u0018\u00010\u0016X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0017\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00120\u00110\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00140\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001aR\u000e\u0010\u001c\u001a\u00020\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020 X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010!\u001a\u0004\u0018\u00010\"X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010#\u001a\u0004\u0018\u00010$X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00062"}, d2 = {"Lcom/example/offlinechat/network/GlobalRelayManager;", "", "context", "Landroid/content/Context;", "onPacketReceived", "Lkotlin/Function1;", "", "", "(Landroid/content/Context;Lkotlin/jvm/functions/Function1;)V", "POST_URL", "", "RAW_STREAM_URL", "RELAY_TOPIC", "UDP_PORT", "", "_globalPeers", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/example/offlinechat/network/Peer;", "_isGatewayActive", "", "connectivityManager", "Landroid/net/ConnectivityManager;", "globalPeers", "Lkotlinx/coroutines/flow/StateFlow;", "getGlobalPeers", "()Lkotlinx/coroutines/flow/StateFlow;", "isGatewayActive", "isListeningUdp", "networkCallback", "Landroid/net/ConnectivityManager$NetworkCallback;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "streamJob", "Lkotlinx/coroutines/Job;", "udpSocket", "Ljava/net/DatagramSocket;", "broadcastGlobalPacket", "packetBytes", "hopsJson", "checkCurrentConnectivity", "registerNetworkCallback", "start", "startGlobalStreamListener", "startUdpListener", "stop", "stopGlobalStreamListener", "stopUdpListener", "updateGatewayState", "active", "app_debug"})
public final class GlobalRelayManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final kotlin.jvm.functions.Function1<byte[], kotlin.Unit> onPacketReceived = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isGatewayActive = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGatewayActive = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.offlinechat.network.Peer>> _globalPeers = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> globalPeers = null;
    @org.jetbrains.annotations.Nullable
    private final android.net.ConnectivityManager connectivityManager = null;
    @org.jetbrains.annotations.Nullable
    private android.net.ConnectivityManager.NetworkCallback networkCallback;
    @org.jetbrains.annotations.Nullable
    private kotlinx.coroutines.Job streamJob;
    @org.jetbrains.annotations.Nullable
    private java.net.DatagramSocket udpSocket;
    private boolean isListeningUdp = false;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String RELAY_TOPIC = "whisp_mesh_global_p2p_channel";
    @org.jetbrains.annotations.NotNull
    private final java.lang.String RAW_STREAM_URL = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String POST_URL = null;
    private final int UDP_PORT = 8888;
    
    public GlobalRelayManager(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super byte[], kotlin.Unit> onPacketReceived) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGatewayActive() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> getGlobalPeers() {
        return null;
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
    
    private final void checkCurrentConnectivity() {
    }
    
    private final void registerNetworkCallback() {
    }
    
    private final void updateGatewayState(boolean active) {
    }
    
    private final void startGlobalStreamListener() {
    }
    
    private final void stopGlobalStreamListener() {
    }
    
    public final void broadcastGlobalPacket(@org.jetbrains.annotations.NotNull
    byte[] packetBytes, @org.jetbrains.annotations.NotNull
    java.lang.String hopsJson) {
    }
    
    private final void startUdpListener() {
    }
    
    private final void stopUdpListener() {
    }
}