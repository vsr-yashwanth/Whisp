package com.example.offlinechat

import android.app.Application
import android.util.Base64
import android.util.Log
import com.example.offlinechat.data.ChatDatabase
import com.example.offlinechat.data.Conversation
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.HybridMeshTransport
import com.example.offlinechat.network.WebServerManager
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

    private val appScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = ChatDatabase.getDatabase(this)
        cryptoManager = CryptoManager(this)
        transport = HybridMeshTransport(this)
        webServerManager = WebServerManager(this, database.chatDao(), transport, cryptoManager)

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
                val json = JSONObject(String(data))
                val originNodeId = json.optJSONArray("hops")?.optJSONObject(0)?.optString("nodeId") ?: ""
                if (originNodeId == transport.localId) {
                    return@launch // Suppress loopback of own packets
                }

                if (json.optString("type") == "MESSAGE") {
                    val transitPayloadBase64 = json.getString("payload")
                    val msgId = json.getString("messageId")
                    val senderId = json.getString("senderId")
                    val conversationId = json.optString("conversationId", "General Chat")
                    val timestamp = json.optLong("timestamp", System.currentTimeMillis())

                    // Stamp current node hop
                    val hopsArray = json.optJSONArray("hops") ?: JSONArray()
                    val prevTimestamp = if (hopsArray.length() > 0) {
                        hopsArray.getJSONObject(hopsArray.length() - 1).optLong("timestamp", timestamp)
                    } else timestamp
                    val now = System.currentTimeMillis()
                    val latency = (now - prevTimestamp).coerceAtLeast(0L)
                    val currentHop = JSONObject().apply {
                        put("nodeId", transport.localId)
                        put("nodeName", transport.localName)
                        put("transport", transportType)
                        put("timestamp", now)
                        put("latencyMs", latency)
                    }
                    hopsArray.put(currentHop)

                    // 1. Decrypt transit ciphertext
                    val transitBytes = Base64.decode(transitPayloadBase64, Base64.NO_WRAP)
                    val decryptedPlaintext = cryptoManager.decryptFromTransit(transitBytes)

                    // 2. Encrypt with local hardware Keystore AEAD for storage at rest
                    val storagePayloadBase64 = cryptoManager.encryptForStorage(decryptedPlaintext)

                    // 3. Insert Conversation (Foreign Key)
                    database.chatDao().insertConversation(
                        Conversation(
                            id = conversationId,
                            peerId = senderId,
                            createdAt = timestamp,
                            lastMessageAt = timestamp
                        )
                    )

                    // 4. Insert Message into SQLite DB
                    val dbMsg = Message(
                        id = msgId,
                        conversationId = conversationId,
                        senderId = senderId,
                        encryptedPayload = storagePayloadBase64,
                        timestamp = timestamp,
                        status = "RECEIVED",
                        hopTrace = hopsArray.toString()
                    )
                    database.chatDao().insertMessage(dbMsg)
                    Log.d("OfflineChatApp", "SUCCESSFULLY PERSISTED PACKET ($msgId) in ($conversationId): $decryptedPlaintext")
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
