package com.example.offlinechat.network

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NearbyConnectionsTransport(private val context: Context) : PeerTransport {

    private val connectionsClient = Nearby.getConnectionsClient(context)
    
    // P2P_CLUSTER allows discovering and advertising simultaneously, creating a local mesh.
    private val STRATEGY = Strategy.P2P_CLUSTER 

    private val SERVICE_ID = "com.example.offlinechat.SERVICE"

    private val _discoveredPeers = MutableStateFlow<List<Peer>>(emptyList())
    override val discoveredPeers: StateFlow<List<Peer>> = _discoveredPeers.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _pairingRequest = MutableStateFlow<PairingRequest?>(null)
    override val pairingRequest: StateFlow<PairingRequest?> = _pairingRequest.asStateFlow()

    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    override val receivedData: StateFlow<ByteArray?> = _receivedData.asStateFlow()

    private var connectedEndpointId: String? = null

    // Callbacks for advertising and connections
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Display pairing request to user for manual verification
            _pairingRequest.value = PairingRequest(
                endpointId = endpointId,
                peerName = info.endpointName,
                authenticationToken = info.authenticationToken,
                accept = {
                    connectionsClient.acceptConnection(endpointId, payloadCallback)
                    _pairingRequest.value = null
                },
                reject = {
                    connectionsClient.rejectConnection(endpointId)
                    _pairingRequest.value = null
                }
            )
            _connectionState.value = ConnectionState.CONNECTING
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    connectedEndpointId = endpointId
                    _connectionState.value = ConnectionState.CONNECTED
                    
                    // Stop searching/advertising once connected (for 1-to-1 chat)
                    stopDiscovery()
                    stopAdvertising()
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    _connectionState.value = ConnectionState.DISCONNECTED
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            if (endpointId == connectedEndpointId) {
                connectedEndpointId = null
                _connectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }

    // Callbacks for discovery
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            val currentList = _discoveredPeers.value.toMutableList()
            if (currentList.none { it.endpointId == endpointId }) {
                currentList.add(Peer(endpointId, info.endpointName))
                _discoveredPeers.value = currentList
            }
        }

        override fun onEndpointLost(endpointId: String) {
            val currentList = _discoveredPeers.value.toMutableList()
            currentList.removeAll { it.endpointId == endpointId }
            _discoveredPeers.value = currentList
        }
    }

    // Callbacks for receiving data
    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                _receivedData.value = payload.asBytes()
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Handle payload transfer progress if sending large files in the future.
        }
    }

    override fun startDiscovery(localIdentity: String) {
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(SERVICE_ID, endpointDiscoveryCallback, options)
            .addOnSuccessListener { Log.d("NearbyTransport", "Discovery started") }
            .addOnFailureListener { e -> Log.e("NearbyTransport", "Discovery failed", e) }
    }

    override fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        _discoveredPeers.value = emptyList()
    }

    override fun startAdvertising(localIdentity: String) {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising(localIdentity, SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { Log.d("NearbyTransport", "Advertising started") }
            .addOnFailureListener { e -> Log.e("NearbyTransport", "Advertising failed", e) }
    }

    override fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    override fun connectToPeer(peer: Peer) {
        _connectionState.value = ConnectionState.CONNECTING
        connectionsClient.requestConnection(
            "WhispUser", // Placeholder, will be replaced with actual user identity
            peer.endpointId,
            connectionLifecycleCallback
        ).addOnFailureListener { e ->
            Log.e("NearbyTransport", "Connection request failed", e)
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    override fun disconnectFromPeer() {
        connectedEndpointId?.let {
            connectionsClient.disconnectFromEndpoint(it)
        }
        connectedEndpointId = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun sendData(data: ByteArray) {
        connectedEndpointId?.let {
            connectionsClient.sendPayload(it, Payload.fromBytes(data))
        }
    }
}
