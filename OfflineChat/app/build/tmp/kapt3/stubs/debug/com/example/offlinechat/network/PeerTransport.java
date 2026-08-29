package com.example.offlinechat.network;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\tH&J\b\u0010\u0014\u001a\u00020\u0012H&J\u0010\u0010\u0015\u001a\u00020\u00122\u0006\u0010\u0016\u001a\u00020\u000fH&J\u0010\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u0019H&J\u0010\u0010\u001a\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u0019H&J\b\u0010\u001b\u001a\u00020\u0012H&J\b\u0010\u001c\u001a\u00020\u0012H&R\u0018\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0005\u0010\u0006R\u001e\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\t0\b0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u0006R\u001a\u0010\u000b\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\f0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\r\u0010\u0006R\u001a\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0006\u00a8\u0006\u001d"}, d2 = {"Lcom/example/offlinechat/network/PeerTransport;", "", "connectionState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/example/offlinechat/network/ConnectionState;", "getConnectionState", "()Lkotlinx/coroutines/flow/StateFlow;", "discoveredPeers", "", "Lcom/example/offlinechat/network/Peer;", "getDiscoveredPeers", "pairingRequest", "Lcom/example/offlinechat/network/PairingRequest;", "getPairingRequest", "receivedData", "", "getReceivedData", "connectToPeer", "", "peer", "disconnectFromPeer", "sendData", "data", "startAdvertising", "localIdentity", "", "startDiscovery", "stopAdvertising", "stopDiscovery", "app_debug"})
public abstract interface PeerTransport {
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.StateFlow<java.util.List<com.example.offlinechat.network.Peer>> getDiscoveredPeers();
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.ConnectionState> getConnectionState();
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.StateFlow<byte[]> getReceivedData();
    
    @org.jetbrains.annotations.NotNull
    public abstract kotlinx.coroutines.flow.StateFlow<com.example.offlinechat.network.PairingRequest> getPairingRequest();
    
    public abstract void startDiscovery(@org.jetbrains.annotations.NotNull
    java.lang.String localIdentity);
    
    public abstract void stopDiscovery();
    
    public abstract void startAdvertising(@org.jetbrains.annotations.NotNull
    java.lang.String localIdentity);
    
    public abstract void stopAdvertising();
    
    public abstract void connectToPeer(@org.jetbrains.annotations.NotNull
    com.example.offlinechat.network.Peer peer);
    
    public abstract void disconnectFromPeer();
    
    public abstract void sendData(@org.jetbrains.annotations.NotNull
    byte[] data);
}