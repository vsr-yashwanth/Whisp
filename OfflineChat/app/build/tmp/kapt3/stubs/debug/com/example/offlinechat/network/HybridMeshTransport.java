package com.example.offlinechat.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000p\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0010\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\nH\u0016J\b\u00100\u001a\u00020.H\u0016J\u0018\u00101\u001a\u00020.2\u0006\u00102\u001a\u00020\f2\b\b\u0002\u00103\u001a\u00020\u000fJ\u0010\u00104\u001a\u00020.2\u0006\u00102\u001a\u00020\fH\u0016J\u0010\u00105\u001a\u00020.2\u0006\u00106\u001a\u00020\u000fH\u0016J\b\u00107\u001a\u00020.H\u0002J\u0010\u00108\u001a\u00020.2\u0006\u00106\u001a\u00020\u000fH\u0016J\b\u00109\u001a\u00020.H\u0016J\b\u0010:\u001a\u00020.H\u0016J$\u0010;\u001a\u00020.2\f\u0010<\u001a\b\u0012\u0004\u0012\u00020\n0\t2\f\u0010=\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u0010\u0010\u0016\u001a\u0004\u0018\u00010\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0018\u001a\u00020\u0019\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0017\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0013R\u0011\u0010\u001e\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010!\u001a\u00020\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010 R\u000e\u0010#\u001a\u00020$X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010%\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010&0\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u0013R\u001a\u0010(\u001a\u000e\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u0010)\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0013R\u000e\u0010+\u001a\u00020,X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006>"}, d2 = {"Lcom/example/offlinechat/network/HybridMeshTransport;", "Lcom/example/offlinechat/network/PeerTransport;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "_connectionState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/offlinechat/network/ConnectionState;", "_discoveredPeers", "", "Lcom/example/offlinechat/network/Peer;", "_receivedData", "", "activeBridgeEndpoints", "Ljava/util/concurrent/ConcurrentHashMap;", "", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "discoveredPeers", "getDiscoveredPeers", "discoveryJob", "Lkotlinx/coroutines/Job;", "globalRelayManager", "Lcom/example/offlinechat/network/GlobalRelayManager;", "getGlobalRelayManager", "()Lcom/example/offlinechat/network/GlobalRelayManager;", "isGlobalGatewayActive", "", "localId", "getLocalId", "()Ljava/lang/String;", "localName", "getLocalName", "nearbyTransport", "Lcom/example/offlinechat/network/NearbyConnectionsTransport;", "pairingRequest", "Lcom/example/offlinechat/network/PairingRequest;", "getPairingRequest", "peerDisplayNames", "receivedData", "getReceivedData", "scope", "Lkotlinx/coroutines/CoroutineScope;", "connectToPeer", "", "peer", "disconnectFromPeer", "feedReceivedData", "data", "transportType", "sendData", "startAdvertising", "localIdentity", "startBridgeDiscovery", "startDiscovery", "stopAdvertising", "stopDiscovery", "updateCombinedPeers", "nearbyPeers", "globalPeers", "app_debug"})
public final class HybridMeshTransport implements com.example.offlinechat.network.PeerTransport {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.network.NearbyConnectionsTransport nearbyTransport = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.example.offlinechat.network.Peer>> _discoveredPeers = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> discoveredPeers = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.offlinechat.network.ConnectionState> _connectionState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.ConnectionState> connectionState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<byte[]> _receivedData = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<byte[]> receivedData = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.PairingRequest> pairingRequest = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String localId = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String localName = null;
    @org.jetbrains.annotations.NotNull
    private final com.example.offlinechat.network.GlobalRelayManager globalRelayManager = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGlobalGatewayActive = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> activeBridgeEndpoints = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.concurrent.ConcurrentHashMap<java.lang.String, java.lang.String> peerDisplayNames = null;
    @org.jetbrains.annotations.Nullable
    private kotlinx.coroutines.Job discoveryJob;
    
    public HybridMeshTransport(@org.jetbrains.annotations.NotNull
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
    public kotlinx.coroutines.flow.StateFlow<byte[]> getReceivedData() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.PairingRequest> getPairingRequest() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLocalId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getLocalName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final com.example.offlinechat.network.GlobalRelayManager getGlobalRelayManager() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGlobalGatewayActive() {
        return null;
    }
    
    private final void updateCombinedPeers(java.util.List<com.example.offlinechat.network.Peer> nearbyPeers, java.util.List<com.example.offlinechat.network.Peer> globalPeers) {
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
    
    public final void feedReceivedData(@org.jetbrains.annotations.NotNull
    byte[] data, @org.jetbrains.annotations.NotNull
    java.lang.String transportType) {
    }
    
    private final void startBridgeDiscovery() {
    }
}