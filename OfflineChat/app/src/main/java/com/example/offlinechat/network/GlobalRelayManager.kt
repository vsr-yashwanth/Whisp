package com.example.offlinechat.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.util.concurrent.TimeUnit

class GlobalRelayManager(
    private val context: Context,
    private val onPacketReceived: (ByteArray) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isGatewayActive = MutableStateFlow(false)
    val isGatewayActive: StateFlow<Boolean> = _isGatewayActive.asStateFlow()

    private val _globalPeers = MutableStateFlow<List<Peer>>(emptyList())
    val globalPeers: StateFlow<List<Peer>> = _globalPeers.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var streamJob: Job? = null
    private var udpSocket: DatagramSocket? = null
    private var isListeningUdp = false

    private val RELAY_TOPIC = "whisp_mesh_global_p2p_channel"
    private val RAW_STREAM_URL = "https://ntfy.sh/$RELAY_TOPIC/raw"
    private val POST_URL = "https://ntfy.sh/$RELAY_TOPIC"
    private val UDP_PORT = 8888

    fun start() {
        checkCurrentConnectivity()
        registerNetworkCallback()
        startUdpListener()
        startGlobalStreamListener()
    }

    fun stop() {
        stopGlobalStreamListener()
        stopUdpListener()
        networkCallback?.let {
            try { connectivityManager?.unregisterNetworkCallback(it) } catch (e: Exception) {}
        }
        networkCallback = null
        _isGatewayActive.value = false
    }

    private fun checkCurrentConnectivity() {
        try {
            val activeNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
            val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            updateGatewayState(isConnected)
        } catch (e: Exception) {
            updateGatewayState(false)
        }
    }

    private fun registerNetworkCallback() {
        if (networkCallback != null) return
        try {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    updateGatewayState(true)
                }

                override fun onLost(network: Network) {
                    checkCurrentConnectivity()
                }

                override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    updateGatewayState(hasInternet)
                }
            }
            connectivityManager?.registerNetworkCallback(builder.build(), callback)
            networkCallback = callback
        } catch (e: Exception) {
            Log.e("GlobalRelayManager", "Failed to register network callback", e)
        }
    }

    private fun updateGatewayState(active: Boolean) {
        _isGatewayActive.value = active
        if (active) {
            val globalPeerNode = Peer(
                endpointId = "GlobalRelay-Gateway",
                name = "Global Internet Gateway (Worldwide)"
            )
            _globalPeers.value = listOf(globalPeerNode)
            startGlobalStreamListener()
        } else {
            _globalPeers.value = emptyList()
            stopGlobalStreamListener()
        }
    }

    private fun startGlobalStreamListener() {
        if (streamJob?.isActive == true) return

        streamJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                if (!_isGatewayActive.value) {
                    delay(3000)
                    continue
                }

                var connection: HttpURLConnection? = null
                var reader: BufferedReader? = null
                try {
                    val url = URL(RAW_STREAM_URL)
                    connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 0 // Stream indefinitely
                    connection.requestMethod = "GET"

                    if (connection.responseCode in 200..299) {
                        reader = BufferedReader(InputStreamReader(connection.inputStream))
                        while (isActive) {
                            val line = reader.readLine() ?: break
                            val trimmed = line.trim()
                            if (trimmed.isNotBlank()) {
                                try {
                                    val rawBytes = Base64.decode(trimmed, Base64.NO_WRAP)
                                    onPacketReceived(rawBytes)
                                    Log.d("GlobalRelayManager", "Received packet from Cloud Stream")
                                } catch (e: Exception) {
                                    Log.e("GlobalRelayManager", "Error decoding stream line", e)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("GlobalRelayManager", "Stream disconnected (${e.message}), reconnecting in 3s...")
                } finally {
                    try { reader?.close() } catch (e: Exception) {}
                    try { connection?.disconnect() } catch (e: Exception) {}
                }

                delay(3000)
            }
        }
    }

    private fun stopGlobalStreamListener() {
        streamJob?.cancel()
        streamJob = null
    }

    fun broadcastGlobalPacket(packetBytes: ByteArray, hopsJson: String) {
        scope.launch(Dispatchers.IO) {
            val base64Payload = Base64.encodeToString(packetBytes, Base64.NO_WRAP)

            // 1. Broadcast over Global Cloud Relay (Worldwide reach)
            if (_isGatewayActive.value) {
                try {
                    val url = URL(POST_URL)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.doOutput = true
                    OutputStreamWriter(conn.outputStream).use { it.write(base64Payload) }
                    val code = conn.responseCode
                    conn.disconnect()
                    Log.d("GlobalRelayManager", "Dispatched packet over Global Cloud Gateway ($code)")
                } catch (e: Exception) {
                    Log.e("GlobalRelayManager", "Global POST error: ${e.message}")
                }
            }

            // 2. Broadcast over Local Wi-Fi UDP (Subnet reach without internet)
            try {
                val udpData = base64Payload.toByteArray()
                val broadcastAddr = InetAddress.getByName("255.255.255.255")
                val packet = DatagramPacket(udpData, udpData.size, broadcastAddr, UDP_PORT)
                val socket = DatagramSocket()
                socket.broadcast = true
                socket.send(packet)
                socket.close()
            } catch (e: Exception) {
                // Ignore UDP broadcast errors on restricted interfaces
            }
        }
    }

    private fun startUdpListener() {
        if (isListeningUdp) return
        isListeningUdp = true
        scope.launch(Dispatchers.IO) {
            try {
                udpSocket = DatagramSocket(UDP_PORT)
                udpSocket?.broadcast = true
                val buffer = ByteArray(8192)

                while (isActive && isListeningUdp) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    udpSocket?.receive(packet)
                    val base64Str = String(packet.data, 0, packet.length).trim()
                    if (base64Str.isNotBlank()) {
                        try {
                            val rawData = Base64.decode(base64Str, Base64.NO_WRAP)
                            onPacketReceived(rawData)
                        } catch (e: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.d("GlobalRelayManager", "UDP listener closed: ${e.message}")
            }
        }
    }

    private fun stopUdpListener() {
        isListeningUdp = false
        udpSocket?.close()
        udpSocket = null
    }
}
