package com.example.offlinechat.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\u000eH\u0016J\b\u0010)\u001a\u00020\'H\u0016J\u0010\u0010*\u001a\u00020\'2\u0006\u0010+\u001a\u00020\u0012H\u0016J\u0010\u0010,\u001a\u00020\'2\u0006\u0010-\u001a\u00020\u0006H\u0016J\u0010\u0010.\u001a\u00020\'2\u0006\u0010-\u001a\u00020\u0006H\u0016J\b\u0010/\u001a\u00020\'H\u0016J\b\u00100\u001a\u00020\'H\u0016R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0017X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u001c\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000e0\r0\u0017X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0019R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010 \u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00100\u0017X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u0019R\u000e\u0010\"\u001a\u00020#X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010$\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u0017X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0019\u00a8\u00061"}, d2 = {"Lcom/example/offlinechat/network/NearbyConnectionsTransport;", "Lcom/example/offlinechat/network/PeerTransport;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "SERVICE_ID", "", "STRATEGY", "Lcom/google/android/gms/nearby/connection/Strategy;", "_connectionState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/offlinechat/network/ConnectionState;", "_discoveredPeers", "", "Lcom/example/offlinechat/network/Peer;", "_pairingRequest", "Lcom/example/offlinechat/network/PairingRequest;", "_receivedData", "", "connectedEndpointId", "connectionLifecycleCallback", "Lcom/google/android/gms/nearby/connection/ConnectionLifecycleCallback;", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "connectionsClient", "Lcom/google/android/gms/nearby/connection/ConnectionsClient;", "discoveredPeers", "getDiscoveredPeers", "endpointDiscoveryCallback", "Lcom/google/android/gms/nearby/connection/EndpointDiscoveryCallback;", "pairingRequest", "getPairingRequest", "payloadCallback", "Lcom/google/android/gms/nearby/connection/PayloadCallback;", "receivedData", "getReceivedData", "connectToPeer", "", "peer", "disconnectFromPeer", "sendData", "data", "startAdvertising", "localIdentity", "startDiscovery", "stopAdvertising", "stopDiscovery", "app_debug"})
public final class NearbyConnectionsTransport implements com.example.offlinechat.network.PeerTransport {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.android.gms.nearby.connection.ConnectionsClient connectionsClient = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.android.gms.nearby.connection.Strategy STRATEGY = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String SERVICE_ID = "com.example.offlinechat.SERVICE";
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.offlinechat.network.Peer>> _discoveredPeers = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> discoveredPeers = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.offlinechat.network.ConnectionState> _connectionState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.ConnectionState> connectionState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.offlinechat.network.PairingRequest> _pairingRequest = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.PairingRequest> pairingRequest = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<byte[]> _receivedData = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<byte[]> receivedData = null;
    @org.jetbrains.annotations.Nullable
    private java.lang.String connectedEndpointId;
    @org.jetbrains.annotations.NotNull
    private final com.google.android.gms.nearby.connection.ConnectionLifecycleCallback connectionLifecycleCallback = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.android.gms.nearby.connection.EndpointDiscoveryCallback endpointDiscoveryCallback = null;
    @org.jetbrains.annotations.NotNull
    private final com.google.android.gms.nearby.connection.PayloadCallback payloadCallback = null;
    
    public NearbyConnectionsTransport(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> getDiscoveredPeers() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.ConnectionState> getConnectionState() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.PairingRequest> getPairingRequest() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.StateFlow<byte[]> getReceivedData() {
        return null;
    }
    
    @java.lang.Override
    public void startDiscovery(@org.jetbrains.annotations.NotNull
    java.lang.String localIdentity) {
    }
    
    @java.lang.Override
    public void stopDiscovery() {
    }
    
    @java.lang.Override
    public void startAdvertising(@org.jetbrains.annotations.NotNull
    java.lang.String localIdentity) {
    }
    
    @java.lang.Override
    public void stopAdvertising() {
    }
    
    @java.lang.Override
    public void connectToPeer(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.Peer peer) {
    }
    
    @java.lang.Override
    public void disconnectFromPeer() {
    }
    
    @java.lang.Override
    public void sendData(@org.jetbrains.annotations.NotNull
    byte[] data) {
    }
}