package com.example.offlinechat.network

import android.content.Context
import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

class HybridMeshTransport(private val context: Context) : PeerTransport {

    private val nearbyTransport = NearbyConnectionsTransport(context)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _discoveredPeers = MutableStateFlow<List<Peer>>(emptyList())
    override val discoveredPeers: StateFlow<List<Peer>> = _discoveredPeers.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _receivedData = MutableStateFlow<ByteArray?>(null)
    override val receivedData: StateFlow<ByteArray?> = _receivedData.asStateFlow()

    override val pairingRequest: StateFlow<PairingRequest?> = nearbyTransport.pairingRequest

    val localId = "Node-${Build.MODEL.replace(" ", "")}"
    val localName = "${Build.MANUFACTURER} ${Build.MODEL}"

    val globalRelayManager = GlobalRelayManager(context) { globalData ->
        com.example.offlinechat.OfflineChatApp.instance.processIncomingRawPacket(globalData, "GLOBAL_RELAY")
        feedReceivedData(globalData, "GLOBAL_RELAY")
    }
    val isGlobalGatewayActive: StateFlow<Boolean> = globalRelayManager.isGatewayActive

    private val activeBridgeEndpoints = ConcurrentHashMap<String, String>() // PeerId -> BaseURL
    private val peerDisplayNames = ConcurrentHashMap<String, String>() // PeerId -> Name
    private var discoveryJob: Job? = null

    init {
        globalRelayManager.start()

        // Collect from nearby transport
        scope.launch {
            nearbyTransport.discoveredPeers.collect { peers ->
                updateCombinedPeers(peers, globalRelayManager.globalPeers.value)
            }
        }

        scope.launch {
            globalRelayManager.globalPeers.collect { gPeers ->
                updateCombinedPeers(nearbyTransport.discoveredPeers.value, gPeers)
            }
        }

        scope.launch {
            nearbyTransport.receivedData.collect { data ->
                data?.let {
                    com.example.offlinechat.OfflineChatApp.instance.processIncomingRawPacket(it, "BLE_MESH")
                    feedReceivedData(it, "BLE_MESH")
                }
            }
        }

        scope.launch {
            nearbyTransport.connectionState.collect { state ->
                if (state == ConnectionState.CONNECTED) {
                    _connectionState.value = ConnectionState.CONNECTED
                } else if (activeBridgeEndpoints.isNotEmpty() || isGlobalGatewayActive.value) {
                    _connectionState.value = ConnectionState.CONNECTED
                } else {
                    _connectionState.value = state
                }
            }
        }
    }

    private fun updateCombinedPeers(nearbyPeers: List<Peer>, globalPeers: List<Peer>) {
        val bridgePeers = activeBridgeEndpoints.map { (id, url) ->
            val name = peerDisplayNames[id] ?: if (url.contains("10.0.2.2")) "Connected Phone (Bridge)" else "Laptop Wi-Fi Relay"
            Peer(id, name)
        }
        val combined = (nearbyPeers + bridgePeers + globalPeers).distinctBy { it.endpointId }
        _discoveredPeers.value = combined
        if (combined.isNotEmpty() || isGlobalGatewayActive.value) {
            _connectionState.value = ConnectionState.CONNECTED
        }
    }

    override fun startDiscovery(localIdentity: String) {
        nearbyTransport.startDiscovery(localIdentity)
        startBridgeDiscovery()
    }

    override fun stopDiscovery() {
        nearbyTransport.stopDiscovery()
        discoveryJob?.cancel()
        discoveryJob = null
    }

    override fun startAdvertising(localIdentity: String) {
        nearbyTransport.startAdvertising(localIdentity)
    }

    override fun stopAdvertising() {
        nearbyTransport.stopAdvertising()
    }

    override fun connectToPeer(peer: Peer) {
        if (activeBridgeEndpoints.containsKey(peer.endpointId) || peer.endpointId.startsWith("GlobalRelay")) {
            _connectionState.value = ConnectionState.CONNECTED
            return
        }
        nearbyTransport.connectToPeer(peer)
    }

    override fun disconnectFromPeer() {
        activeBridgeEndpoints.clear()
        nearbyTransport.disconnectFromPeer()
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override fun sendData(data: ByteArray) {
        // Stamp local hop onto the packet
        val stampedBytes = try {
            val json = JSONObject(String(data))
            val hopsArray = json.optJSONArray("hops") ?: JSONArray()
            val hopObj = JSONObject().apply {
                put("nodeId", localId)
                put("nodeName", localName)
                put("transport", "ORIGIN")
                put("timestamp", System.currentTimeMillis())
                put("latencyMs", 0L)
            }
            hopsArray.put(hopObj)
            json.put("hops", hopsArray)
            json.toString().toByteArray()
        } catch (e: Exception) {
            data
        }

        // 1. Send via Nearby Connections if connected over physical BLE/Wi-Fi Direct
        try {
            nearbyTransport.sendData(stampedBytes)
        } catch (e: Exception) {
            Log.d("HybridTransport", "Nearby send skipped: ${e.message}")
        }

        // 2. Broadcast via Global Cloud WebSocket Relay & Wi-Fi UDP Broadcast
        globalRelayManager.broadcastGlobalPacket(stampedBytes, "GlobalInternetRelay")

        // 3. Broadcast via Local Wi-Fi Mesh Relay & LAN Bridge
        val payloadBase64 = Base64.encodeToString(stampedBytes, Base64.NO_WRAP)
        val jsonPayload = JSONObject().apply {
            put("payload", payloadBase64)
        }.toString()

        scope.launch(Dispatchers.IO) {
            val targets = activeBridgeEndpoints.values.toMutableSet()
            targets.add("http://10.0.2.2:8088")
            targets.add("http://10.9.255.239:8088")
            targets.add("http://10.0.2.2:8080")
            targets.add("http://127.0.0.1:8081")

            for (baseUrl in targets) {
                try {
                    val url = URL("$baseUrl/api/mesh/packet")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.connectTimeout = 1500
                    conn.readTimeout = 1500
                    conn.doOutput = true

                    OutputStreamWriter(conn.outputStream).use { writer ->
                        writer.write(jsonPayload)
                        writer.flush()
                    }

                    conn.responseCode
                    conn.disconnect()
                } catch (e: Exception) {
                    // Ignore offline bridge target
                }
            }
        }
    }

    fun feedReceivedData(data: ByteArray, transportType: String = "LOCAL_BRIDGE") {
        val stampedData = try {
            val json = JSONObject(String(data))
            
            // Prevent duplicate echoing of own dispatched packets
            val originNodeId = json.optJSONArray("hops")?.optJSONObject(0)?.optString("nodeId") ?: ""
            if (originNodeId == localId) {
                return
            }

            val hopsArray = json.optJSONArray("hops") ?: JSONArray()
            val prevTimestamp = if (hopsArray.length() > 0) {
                hopsArray.getJSONObject(hopsArray.length() - 1).optLong("timestamp", System.currentTimeMillis())
            } else {
                json.optLong("timestamp", System.currentTimeMillis())
            }
            val now = System.currentTimeMillis()
            val latency = (now - prevTimestamp).coerceAtLeast(0L)

            val hopObj = JSONObject().apply {
                put("nodeId", localId)
                put("nodeName", localName)
                put("transport", transportType)
                put("timestamp", now)
                put("latencyMs", latency)
            }
            hopsArray.put(hopObj)
            json.put("hops", hopsArray)
            json.toString().toByteArray()
        } catch (e: Exception) {
            data
        }

        _receivedData.value = stampedData
        _connectionState.value = ConnectionState.CONNECTED
    }

    private fun startBridgeDiscovery() {
        if (discoveryJob?.isActive == true) return

        discoveryJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val candidateUrls = listOf(
                    "http://10.0.2.2:8088",
                    "http://10.9.255.239:8088",
                    "http://10.0.2.2:8080",
                    "http://127.0.0.1:8081"
                )

                for (baseUrl in candidateUrls) {
                    try {
                        val url = URL("$baseUrl/api/mesh/ping")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "GET"
                        conn.connectTimeout = 1200
                        conn.readTimeout = 1200

                        if (conn.responseCode == 200) {
                            val response = conn.inputStream.bufferedReader().readText()
                            val json = JSONObject(response)
                            val peerId = json.optString("id", baseUrl)
                            val peerName = json.optString("name", "Mesh Node")
                            if (peerId != localId) {
                                activeBridgeEndpoints[peerId] = baseUrl
                                peerDisplayNames[peerId] = peerName
                                updateCombinedPeers(nearbyTransport.discoveredPeers.value, globalRelayManager.globalPeers.value)
                            }
                        }
                        conn.disconnect()
                    } catch (e: Exception) {
                        // Ignore offline candidate
                    }
                }

                delay(3000)
            }
        }
    }
}
