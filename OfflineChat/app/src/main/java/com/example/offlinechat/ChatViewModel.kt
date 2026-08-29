package com.example.offlinechat

import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.Conversation
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.ConnectionState
import com.example.offlinechat.network.HopRecord
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import com.example.offlinechat.network.PeerTransport
import com.example.offlinechat.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromMe: Boolean,
    val timestamp: Long,
    val status: String = "SENT",
    val hopTrace: List<HopRecord> = emptyList()
)

class ChatViewModel(
    private val transport: PeerTransport,
    private val cryptoManager: CryptoManager,
    private val chatDao: ChatDao,
    private val currentConversationId: String
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    init {
        // Flush pending messages on reconnection
        viewModelScope.launch(Dispatchers.IO) {
            transport.connectionState.collectLatest { state ->
                if (state == ConnectionState.CONNECTED) {
                    flushPendingMessages()
                }
            }
        }

        // Load messages from DB to UI in real-time
        viewModelScope.launch {
            chatDao.getMessagesForConversation(currentConversationId).collect { dbMessages ->
                val uiMessages = dbMessages.map { dbMsg ->
                    val text = cryptoManager.decryptFromStorage(dbMsg.encryptedPayload)
                    val hops = parseHopTrace(dbMsg.hopTrace)
                    ChatMessage(
                        id = dbMsg.id,
                        text = text,
                        isFromMe = dbMsg.senderId == "me",
                        timestamp = dbMsg.timestamp,
                        status = dbMsg.status,
                        hopTrace = hops
                    )
                }
                _messages.value = uiMessages
            }
        }
    }

    private fun parseHopTrace(jsonStr: String?): List<HopRecord> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<HopRecord>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    HopRecord(
                        nodeId = obj.optString("nodeId"),
                        nodeName = obj.optString("nodeName"),
                        transport = obj.optString("transport"),
                        timestamp = obj.optLong("timestamp"),
                        latencyMs = obj.optLong("latencyMs")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun flushPendingMessages() {
        val pendingMessages = chatDao.getPendingMessages(currentConversationId)
        for (msg in pendingMessages) {
            try {
                val plaintext = cryptoManager.decryptFromStorage(msg.encryptedPayload)
                val transitEncrypted = cryptoManager.encryptForTransit(plaintext.toByteArray())
                val transitBase64 = Base64.encodeToString(transitEncrypted, Base64.NO_WRAP)

                val hopsList = parseHopTrace(msg.hopTrace)

                val packet = MeshPacket(
                    protocolVersion = 4,
                    packetType = PacketType.MESSAGE,
                    packetId = UUID.randomUUID().toString(),
                    messageId = msg.id,
                    conversationId = currentConversationId,
                    senderId = msg.senderId,
                    recipientId = if (currentConversationId.startsWith("Node-") || currentConversationId.startsWith("User-")) currentConversationId else "ALL",
                    timestamp = msg.timestamp,
                    ttl = 10,
                    priority = PacketPriority.NORMAL,
                    payload = transitBase64,
                    hops = hopsList
                )
                val signedPacket = cryptoManager.signPacketEnvelope(packet)
                transport.sendData(signedPacket.toJsonString().toByteArray())
                chatDao.updateMessageStatus(msg.id, "SENT")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send pending message", e)
            }
        }
    }

    fun sendMessage(text: String, isEmergency: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val msgId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                // 1. Encrypt for local storage at rest (Android Keystore AES-256-GCM)
                val storagePayloadBase64 = cryptoManager.encryptForStorage(text.toByteArray())
                
                // 2. Encrypt for network transit (AES-256-GCM)
                val transitPayload = cryptoManager.encryptForTransit(text.toByteArray())
                val transitPayloadBase64 = Base64.encodeToString(transitPayload, Base64.NO_WRAP)
                
                val initialHop = HopRecord(
                    nodeId = "Node-${Build.MODEL.replace(" ", "")}",
                    nodeName = "${Build.MANUFACTURER} ${Build.MODEL}",
                    transport = "ORIGIN",
                    timestamp = timestamp,
                    latencyMs = 0L
                )

                // Ensure parent conversation exists
                chatDao.insertConversation(
                    Conversation(
                        id = currentConversationId,
                        peerId = currentConversationId,
                        createdAt = timestamp,
                        lastMessageAt = timestamp
                    )
                )

                // 3. Save to local SQLite Room DB with initial hop
                val hopsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("nodeId", initialHop.nodeId)
                        put("nodeName", initialHop.nodeName)
                        put("transport", initialHop.transport)
                        put("timestamp", initialHop.timestamp)
                        put("latencyMs", initialHop.latencyMs)
                    })
                }

                val dbMsg = Message(
                    id = msgId,
                    conversationId = currentConversationId,
                    senderId = "me",
                    encryptedPayload = storagePayloadBase64,
                    timestamp = timestamp,
                    status = "SENT",
                    hopTrace = hopsArray.toString()
                )
                chatDao.insertMessage(dbMsg)

                // 4. Construct versioned MeshPacket protocol instance
                val packet = MeshPacket(
                    protocolVersion = 4,
                    packetType = if (isEmergency) PacketType.SOS else PacketType.MESSAGE,
                    packetId = UUID.randomUUID().toString(),
                    messageId = msgId,
                    conversationId = currentConversationId,
                    senderId = "User-${Build.MODEL.take(6)}",
                    recipientId = if (currentConversationId.startsWith("Node-") || currentConversationId.startsWith("User-")) currentConversationId else "ALL",
                    timestamp = timestamp,
                    ttl = 10,
                    priority = if (isEmergency) PacketPriority.SOS else PacketPriority.NORMAL,
                    payload = transitPayloadBase64,
                    hops = listOf(initialHop)
                )

                val signedPacket = cryptoManager.signPacketEnvelope(packet)

                // Broadcast network packet across transport
                transport.sendData(signedPacket.toJsonString().toByteArray())
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }
    
    fun sendKeyExchange() {
        val pubKey = cryptoManager.generateSessionPublicKey()
        val json = JSONObject().apply {
            put("version", 2)
            put("type", "KEY_EXCHANGE")
            put("publicKey", pubKey)
        }
        transport.sendData(json.toString().toByteArray())
    }

    class Factory(
        private val transport: PeerTransport,
        private val cryptoManager: CryptoManager,
        private val chatDao: ChatDao,
        private val conversationId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(transport, cryptoManager, chatDao, conversationId) as T
        }
    }
}
