package com.example.offlinechat

import android.app.Application
import android.util.Base64
import android.util.Log
import com.example.offlinechat.data.ChatDatabase
import com.example.offlinechat.data.Conversation
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.DeduplicationCache
import com.example.offlinechat.network.HybridMeshTransport
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketType
import com.example.offlinechat.network.StoreAndForwardManager
import com.example.offlinechat.network.WebServerManager
import com.example.offlinechat.routing.BatteryRelayPolicy
import com.example.offlinechat.security.CryptoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class OfflineChatApp : Application() {

    lateinit var database: ChatDatabase
        private set
    lateinit var cryptoManager: CryptoManager
        private set
    lateinit var transport: HybridMeshTransport
        private set
    lateinit var webServerManager: WebServerManager
        private set
    lateinit var deduplicationCache: DeduplicationCache
        private set
    lateinit var storeAndForwardManager: StoreAndForwardManager
        private set
    lateinit var batteryRelayPolicy: BatteryRelayPolicy
        private set

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = ChatDatabase.getDatabase(this)
        cryptoManager = CryptoManager(this)
        deduplicationCache = DeduplicationCache(maxCapacity = 5000)
        batteryRelayPolicy = BatteryRelayPolicy(this)
        transport = HybridMeshTransport(this)
        webServerManager = WebServerManager(this, database.chatDao(), transport, cryptoManager)

        storeAndForwardManager = StoreAndForwardManager(
            chatDao = database.chatDao(),
            sendFunction = { data -> transport.sendData(data) }
        )
        storeAndForwardManager.start()

        // Listen for discovered peers and flush store-and-forward queue
        appScope.launch {
            transport.discoveredPeers.collect { peers ->
                peers.forEach { peer ->
                    storeAndForwardManager.onPeerConnectedOrDiscovered(peer.endpointId)
                }
            }
        }

        // Start background web server & mesh bridge
        webServerManager.start()

        // Start discovery & advertising
        val myName = "User-${android.os.Build.MODEL.take(6)}"
        transport.startAdvertising(myName)
        transport.startDiscovery(myName)
    }

    fun processIncomingRawPacket(data: ByteArray, transportType: String = "MESH") {
        appScope.launch(Dispatchers.IO) {
            try {
                val rawString = String(data)
                val packet = MeshPacket.fromJsonString(rawString) ?: return@launch

                // 1. Loopback prevention: Drop if originated from self
                val originNodeId = packet.hops.firstOrNull()?.nodeId ?: packet.senderId
                if (originNodeId == transport.localId) {
                    return@launch
                }

                // 2. Deduplication check: Drop if already processed within TTL window
                if (deduplicationCache.isDuplicateOrRecord(packet.packetId, packet.payloadHash)) {
                    Log.d("OfflineChatApp", "Dropped duplicate packet (${packet.packetId})")
                    return@launch
                }

                // 3. TTL Validation: Drop if packet exceeded max hops
                if (packet.ttl <= 0) {
                    Log.w("OfflineChatApp", "Dropped packet (${packet.packetId}) due to expired TTL (0)")
                    return@launch
                }

                // 4. Stamp local node hop & battery state onto the packet
                val stampedPacket = packet.stampedWithHop(
                    nodeId = transport.localId,
                    nodeName = transport.localName,
                    transport = transportType,
                    currentBattery = batteryRelayPolicy.getBatteryLevel(),
                    charging = batteryRelayPolicy.isCharging()
                )

                // 5. Store-and-Forward / Battery-Aware Multi-hop Relay check
                val isForMe = stampedPacket.recipientId == "ALL" || stampedPacket.recipientId == transport.localId
                if (!isForMe) {
                    if (batteryRelayPolicy.shouldRelay(stampedPacket)) {
                        storeAndForwardManager.bufferPacket(stampedPacket)
                        Log.d("OfflineChatApp", "Relaying packet (${stampedPacket.packetId}) for (${stampedPacket.recipientId})")
                    } else {
                        Log.w("OfflineChatApp", "Dropped relay packet (${stampedPacket.packetId}) due to low battery threshold (${batteryRelayPolicy.getBatteryLevel()}%)")
                    }
                    return@launch
                }

                when (stampedPacket.packetType) {
                    PacketType.MESSAGE, PacketType.SOS -> {
                        // Decrypt transit ciphertext
                        val transitBytes = Base64.decode(stampedPacket.payload, Base64.NO_WRAP)
                        val decryptedPlaintext = cryptoManager.decryptFromTransit(transitBytes)

                        // Encrypt with local hardware Keystore AEAD for storage at rest
                        val storagePayloadBase64 = cryptoManager.encryptForStorage(decryptedPlaintext)

                        // Insert Conversation (Foreign Key)
                        database.chatDao().insertConversation(
                            Conversation(
                                id = stampedPacket.conversationId,
                                peerId = stampedPacket.senderId,
                                createdAt = stampedPacket.timestamp,
                                lastMessageAt = stampedPacket.timestamp
                            )
                        )

                        // Serialize hops
                        val hopsJsonArr = JSONArray()
                        stampedPacket.hops.forEach { h ->
                            hopsJsonArr.put(JSONObject().apply {
                                put("nodeId", h.nodeId)
                                put("nodeName", h.nodeName)
                                put("transport", h.transport)
                                put("timestamp", h.timestamp)
                                put("latencyMs", h.latencyMs)
                            })
                        }

                        // Insert Message into SQLite DB
                        val dbMsg = Message(
                            id = stampedPacket.messageId,
                            conversationId = stampedPacket.conversationId,
                            senderId = stampedPacket.senderId,
                            encryptedPayload = storagePayloadBase64,
                            timestamp = stampedPacket.timestamp,
                            status = "RECEIVED",
                            hopTrace = hopsJsonArr.toString()
                        )
                        database.chatDao().insertMessage(dbMsg)
                        Log.d("OfflineChatApp", "PERSISTED PACKET (${stampedPacket.messageId}) in (${stampedPacket.conversationId}): $decryptedPlaintext")
                    }
                    else -> {
                        // Handled by specialized handlers
                    }
                }
            } catch (e: Exception) {
                Log.e("OfflineChatApp", "Failed to process incoming raw packet: ${e.message}", e)
            }
        }
    }

    companion object {
        lateinit var instance: OfflineChatApp
            private set
    }
}
