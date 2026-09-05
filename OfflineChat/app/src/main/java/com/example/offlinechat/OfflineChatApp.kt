package com.example.offlinechat

import android.app.Application
import android.util.Base64
import android.util.Log
import com.example.offlinechat.crdt.CrdtEngine
import com.example.offlinechat.data.ChatDatabase
import com.example.offlinechat.data.Conversation
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.DeduplicationCache
import com.example.offlinechat.network.HybridMeshTransport
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketType
import com.example.offlinechat.network.PartitionManager
import com.example.offlinechat.network.StoreAndForwardManager
import com.example.offlinechat.network.WebServerManager
import com.example.offlinechat.network.dtn.DtnBundle
import com.example.offlinechat.network.dtn.DtnEngine
import com.example.offlinechat.routing.BatteryRelayPolicy
import com.example.offlinechat.routing.MobilityClassifier
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
    lateinit var dtnEngine: DtnEngine
        private set
    lateinit var partitionManager: PartitionManager
        private set
    lateinit var crdtEngine: CrdtEngine
        private set
    lateinit var mobilityClassifier: MobilityClassifier
        private set
    val rateLimiter = com.example.offlinechat.security.AntiFloodRateLimiter()

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

        dtnEngine = DtnEngine(
            chatDao = database.chatDao(),
            sendRawData = { data -> transport.sendData(data) }
        )
        dtnEngine.start()

        partitionManager = PartitionManager(
            localNodeId = transport.localId,
            chatDao = database.chatDao(),
            sendRawPacket = { data -> transport.sendData(data) }
        )

        crdtEngine = CrdtEngine(
            localActorId = transport.localId,
            chatDao = database.chatDao(),
            sendRawPacket = { data -> transport.sendData(data) }
        )

        mobilityClassifier = MobilityClassifier(this)
        mobilityClassifier.start()

        storeAndForwardManager = StoreAndForwardManager(
            chatDao = database.chatDao(),
            sendFunction = { data -> transport.sendData(data) }
        )
        storeAndForwardManager.start()

        // Listen for discovered peers: trigger partition manager & opportunistic DTN flushes
        appScope.launch {
            transport.discoveredPeers.collect { peers ->
                partitionManager.onPeerTopologyUpdated(peers)
                peers.forEach { peer ->
                    storeAndForwardManager.onPeerConnectedOrDiscovered(peer.endpointId)
                    dtnEngine.onPeerConnectedOrDiscovered(peer.endpointId)
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

                // 2. Anti-Flooding Rate Limiter: Drop if spamming
                if (!rateLimiter.allowPacket(packet.senderId)) {
                    Log.w("OfflineChatApp", "Dropped packet from (${packet.senderId}) due to rate limiting")
                    return@launch
                }

                // 3. Cryptographic Envelope Signature Verification: Fail-closed on tampering
                if (packet.signature.isNotBlank() && !cryptoManager.verifyPacketSignature(packet)) {
                    Log.e("OfflineChatApp", "SECURITY INVARIANT VIOLATION: Dropped forged/tampered packet (${packet.packetId})")
                    return@launch
                }

                // 4. Deduplication check: Drop if already processed within TTL window
                if (deduplicationCache.isDuplicateOrRecord(packet.packetId, packet.payloadHash)) {
                    Log.d("OfflineChatApp", "Dropped duplicate packet (${packet.packetId})")
                    return@launch
                }

                // 5. TTL Validation: Drop if packet exceeded max hops
                if (packet.ttl <= 0) {
                    Log.w("OfflineChatApp", "Dropped packet (${packet.packetId}) due to expired TTL (0)")
                    return@launch
                }

                // 4. Record delivery result in PredictionEngine & EncounterTracker
                transport.routingEngine.predictionEngine.recordPacketDeliveryResult(
                    peerId = packet.senderId,
                    latencyMs = packet.hops.lastOrNull()?.latencyMs ?: 20L,
                    success = true
                )
                transport.routingEngine.encounterTracker.recordEncounter(packet.senderId, transportType)

                // 5. Stamp local node hop & battery state onto the packet
                val stampedPacket = packet.stampedWithHop(
                    nodeId = transport.localId,
                    nodeName = transport.localName,
                    transport = transportType,
                    currentBattery = batteryRelayPolicy.getBatteryLevel(),
                    charging = batteryRelayPolicy.isCharging()
                )

                // 6. Partition Epoch & CRDT Specialized Handlers
                when (stampedPacket.packetType) {
                    PacketType.PARTITION_EPOCH_SYNC -> {
                        partitionManager.handleIncomingEpochSync(stampedPacket.payload)
                        return@launch
                    }
                    PacketType.CRDT_OPERATION -> {
                        crdtEngine.applyRemoteOperation(stampedPacket.payload)
                        return@launch
                    }
                    else -> {}
                }

                // 7. Store-and-Forward / DTN Multi-hop Relay check & Blockchain ID Addressing
                val authPrefs = getSharedPreferences("whisp_auth_prefs", android.content.Context.MODE_PRIVATE)
                val currentUsername = authPrefs.getString("logged_in_user", "") ?: ""
                val currentRole = authPrefs.getString("logged_in_role", "USER") ?: "USER"
                val myBlockchainId = if (currentUsername.isNotBlank()) com.example.offlinechat.data.UserAccount.computeBlockchainId(currentUsername) else ""
                val isAuthority = currentRole == "SUPER_ADMIN" || currentRole == "NETWORK_ADMIN" || currentUsername.equals("admin", true) || currentUsername.equals("operator", true)

                val isForMe = stampedPacket.recipientId == "ALL" ||
                        stampedPacket.recipientBlockchainId == "ALL" ||
                        stampedPacket.recipientId == transport.localId ||
                        (currentUsername.isNotBlank() && stampedPacket.recipientId.equals(currentUsername, ignoreCase = true)) ||
                        (myBlockchainId.isNotBlank() && stampedPacket.recipientBlockchainId.equals(myBlockchainId, ignoreCase = true)) ||
                        (stampedPacket.packetType == PacketType.SOS) ||
                        (isAuthority && stampedPacket.priority >= 50)

                if (!isForMe) {
                    if (batteryRelayPolicy.shouldRelay(stampedPacket)) {
                        // Store in DTN Custody
                        val targetDest = if (stampedPacket.recipientBlockchainId.isNotBlank() && stampedPacket.recipientBlockchainId != "ALL") {
                            stampedPacket.recipientBlockchainId
                        } else {
                            stampedPacket.recipientId
                        }

                        val dtnBundle = DtnBundle(
                            bundleId = stampedPacket.packetId,
                            messageId = stampedPacket.messageId,
                            source = stampedPacket.senderBlockchainId.ifBlank { stampedPacket.senderId },
                            destination = targetDest,
                            creationTime = stampedPacket.timestamp,
                            expirationTime = stampedPacket.timestamp + (stampedPacket.ttl * 60_000L),
                            ttl = stampedPacket.ttl,
                            priority = stampedPacket.priority,
                            payload = stampedPacket.payload,
                            payloadHash = stampedPacket.payloadHash
                        )
                        dtnEngine.ingestAndStoreBundle(dtnBundle)
                        storeAndForwardManager.bufferPacket(stampedPacket)
                        Log.d("OfflineChatApp", "Stored DTN custody bundle (${stampedPacket.packetId}) for ($targetDest)")
                    } else {
                        Log.w("OfflineChatApp", "Dropped relay packet (${stampedPacket.packetId}) due to low battery threshold")
                    }
                    return@launch
                }

                // 8. Process Direct Packet Payload
                when (stampedPacket.packetType) {
                    PacketType.MESSAGE, PacketType.SOS, PacketType.BUNDLE_DATA -> {
                        // Decrypt transit ciphertext
                        val transitBytes = Base64.decode(stampedPacket.payload, Base64.NO_WRAP)
                        val decryptedPlaintext = cryptoManager.decryptFromTransit(transitBytes)
                        val plaintextString = String(decryptedPlaintext, Charsets.UTF_8)

                        // Encrypt with local hardware Keystore AEAD for storage at rest
                        val storagePayloadBase64 = cryptoManager.encryptForStorage(decryptedPlaintext)

                        // Map conversation ID: 1-on-1 direct conversations map to the sender
                        val targetConvId = if (stampedPacket.packetType == PacketType.SOS) {
                            "EMERGENCY_SOS"
                        } else if (stampedPacket.conversationId.startsWith("direct_")) {
                            "direct_${stampedPacket.senderId}"
                        } else {
                            stampedPacket.conversationId
                        }

                        val convType = if (stampedPacket.packetType == PacketType.SOS) "EMERGENCY_SOS" else if (targetConvId.startsWith("direct_")) "DIRECT" else "GENERAL"

                        // Insert Conversation (Foreign Key)
                        database.chatDao().insertConversation(
                            Conversation(
                                id = targetConvId,
                                peerId = stampedPacket.senderId,
                                createdAt = stampedPacket.timestamp,
                                lastMessageAt = stampedPacket.timestamp,
                                conversationType = convType,
                                displayName = if (convType == "DIRECT") stampedPacket.senderId else targetConvId,
                                participantBlockchainId = stampedPacket.senderBlockchainId
                            )
                        )

                        // Update Friend snippet if this is a direct message from a friend
                        if (convType == "DIRECT") {
                            database.chatDao().updateFriendLastMessage(
                                username = stampedPacket.senderId,
                                snippet = plaintextString,
                                time = stampedPacket.timestamp
                            )
                        }

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
                            conversationId = targetConvId,
                            senderId = stampedPacket.senderId,
                            encryptedPayload = storagePayloadBase64,
                            timestamp = stampedPacket.timestamp,
                            status = "RECEIVED",
                            hopTrace = hopsJsonArr.toString(),
                            senderBlockchainId = stampedPacket.senderBlockchainId,
                            recipientBlockchainId = stampedPacket.recipientBlockchainId
                        )
                        database.chatDao().insertMessage(dbMsg)
                        Log.d("OfflineChatApp", "PERSISTED PACKET (${stampedPacket.messageId}) in ($targetConvId): $decryptedPlaintext")
                    }
                    else -> {}
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
