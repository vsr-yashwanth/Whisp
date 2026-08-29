package com.example.offlinechat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.Message
import com.example.offlinechat.network.ConnectionState
import com.example.offlinechat.network.HopRecord
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
                val transitBase64 = android.util.Base64.encodeToString(transitEncrypted, android.util.Base64.NO_WRAP)

                val hopsArr = try { JSONArray(msg.hopTrace) } catch (e: Exception) { JSONArray() }

                val json = JSONObject().apply {
                    put("version", 1)
                    put("type", "MESSAGE")
                    put("messageId", msg.id)
                    put("conversationId", currentConversationId)
                    put("senderId", msg.senderId)
                    put("timestamp", msg.timestamp)
                    put("payload", transitBase64)
                    put("hops", hopsArr)
                }
                transport.sendData(json.toString().toByteArray())
                chatDao.updateMessageStatus(msg.id, "SENT")
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send pending message", e)
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val msgId = UUID.randomUUID().toString()
                val timestamp = System.currentTimeMillis()
                
                // 1. Encrypt for local storage at rest (Android Keystore AES-256-GCM)
                val storagePayloadBase64 = cryptoManager.encryptForStorage(text.toByteArray())
                
                // 2. Encrypt for network transit (AES-256-GCM)
                val transitPayload = cryptoManager.encryptForTransit(text.toByteArray())
                val transitPayloadBase64 = android.util.Base64.encodeToString(transitPayload, android.util.Base64.NO_WRAP)
                
                val initialHop = JSONObject().apply {
                    put("nodeId", "Node-${android.os.Build.MODEL.replace(" ", "")}")
                    put("nodeName", "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                    put("transport", "ORIGIN")
                    put("timestamp", timestamp)
                    put("latencyMs", 0L)
                }
                val hopsArray = JSONArray().apply { put(initialHop) }

                // Ensure parent conversation exists
                chatDao.insertConversation(
                    com.example.offlinechat.data.Conversation(
                        id = currentConversationId,
                        peerId = currentConversationId,
                        createdAt = timestamp,
                        lastMessageAt = timestamp
                    )
                )

                // 3. Save to local SQLite Room DB with initial hop
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

                // 4. Broadcast network packet across transport
                val json = JSONObject().apply {
                    put("version", 1)
                    put("type", "MESSAGE")
                    put("messageId", msgId)
                    put("conversationId", currentConversationId)
                    put("senderId", "User-${android.os.Build.MODEL.take(6)}")
                    put("timestamp", timestamp)
                    put("payload", transitPayloadBase64)
                    put("hops", hopsArray)
                }
                transport.sendData(json.toString().toByteArray())
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Failed to send message", e)
            }
        }
    }
    
    fun sendKeyExchange() {
        val pubKey = cryptoManager.generateSessionPublicKey()
        val json = JSONObject().apply {
            put("version", 1)
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
