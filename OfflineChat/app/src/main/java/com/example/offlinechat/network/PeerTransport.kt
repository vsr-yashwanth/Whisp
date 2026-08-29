package com.example.offlinechat.network

import kotlinx.coroutines.flow.StateFlow

data class Peer(
    val endpointId: String,
    val name: String
)

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}

data class PairingRequest(
    val endpointId: String,
    val peerName: String,
    val authenticationToken: String,
    val accept: () -> Unit,
    val reject: () -> Unit
)

interface PeerTransport {
    val discoveredPeers: StateFlow<List<Peer>>
    val connectionState: StateFlow<ConnectionState>
    val receivedData: StateFlow<ByteArray?>
    val pairingRequest: StateFlow<PairingRequest?>

    fun startDiscovery(localIdentity: String)
    fun stopDiscovery()
    
    fun startAdvertising(localIdentity: String)
    fun stopAdvertising()

    fun connectToPeer(peer: Peer)
    fun disconnectFromPeer()
    
    fun sendData(data: ByteArray)
}
