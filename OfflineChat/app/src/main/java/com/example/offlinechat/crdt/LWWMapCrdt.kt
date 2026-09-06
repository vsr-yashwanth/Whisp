package com.example.offlinechat.crdt

import android.util.Log
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.CrdtOperationEntity
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import com.example.offlinechat.network.PacketType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class LWWMapCrdt(val documentId: String) {
    private val entries = ConcurrentHashMap<String, CrdtEntry>()

    fun apply(op: CrdtOperation): Boolean {
        val newEntry = CrdtEntry(
            value = op.valueJson,
            lamportClock = op.lamportClock,
            timestamp = op.timestamp,
            actorId = op.actorId,
            isTombstone = (op.type == CrdtOpType.DELETE)
        )

        val existing = entries[op.key]
        if (existing == null || newEntry > existing) {
            entries[op.key] = newEntry
            return true
        }
        return false
    }

    fun get(key: String): String? {
        val entry = entries[key]
        return if (entry != null && !entry.isTombstone) entry.value else null
    }

    fun getAll(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        entries.forEach { (k, entry) ->
            if (!entry.isTombstone) {
                result[k] = entry.value
            }
        }
        return result
    }
}

class CrdtEngine(
    private val localActorId: String,
    private val chatDao: ChatDao? = null,
    private val sendRawPacket: (ByteArray) -> Unit = {}
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val documents = ConcurrentHashMap<String, LWWMapCrdt>()
    private var localLamportClock: Long = 0L

    private val _documentStates = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val documentStates: StateFlow<Map<String, Map<String, String>>> = _documentStates.asStateFlow()

    fun updateField(documentId: String, key: String, value: String) {
        localLamportClock++
        val op = CrdtOperation(
            documentId = documentId,
            actorId = localActorId,
            lamportClock = localLamportClock,
            timestamp = System.currentTimeMillis(),
            type = CrdtOpType.SET,
            key = key,
            valueJson = value
        )
        applyAndBroadcastOperation(op)
    }

    fun deleteField(documentId: String, key: String) {
        localLamportClock++
        val op = CrdtOperation(
            documentId = documentId,
            actorId = localActorId,
            lamportClock = localLamportClock,
            timestamp = System.currentTimeMillis(),
            type = CrdtOpType.DELETE,
            key = key,
            valueJson = ""
        )
        applyAndBroadcastOperation(op)
    }

    fun applyRemoteOperation(opJson: String): Boolean {
        val op = CrdtOperation.fromJsonString(opJson) ?: return false
        localLamportClock = maxOf(localLamportClock, op.lamportClock) + 1

        val doc = documents.computeIfAbsent(op.documentId) { LWWMapCrdt(op.documentId) }
        val modified = doc.apply(op)

        if (modified) {
            persistOperation(op)
            publishStateUpdate()
            Log.d("CrdtEngine", "Merged remote CRDT operation on (${op.documentId}/${op.key}) from (${op.actorId})")
        }
        return modified
    }

    fun getDocument(documentId: String): Map<String, String> {
        return documents[documentId]?.getAll() ?: emptyMap()
    }

    private fun applyAndBroadcastOperation(op: CrdtOperation) {
        val doc = documents.computeIfAbsent(op.documentId) { LWWMapCrdt(op.documentId) }
        doc.apply(op)
        persistOperation(op)
        publishStateUpdate()

        // Broadcast over mesh
        val packet = MeshPacket(
            protocolVersion = 3,
            packetType = PacketType.CRDT_OPERATION,
            senderId = localActorId,
            recipientId = "ALL",
            payload = op.toJsonString(),
            priority = PacketPriority.IMPORTANT
        )
        sendRawPacket(packet.toJsonString().toByteArray(Charsets.UTF_8))
        Log.d("CrdtEngine", "Applied and broadcasted local CRDT op (${op.documentId}/${op.key})")
    }

    private fun publishStateUpdate() {
        val snapshot = mutableMapOf<String, Map<String, String>>()
        documents.forEach { (id, doc) ->
            snapshot[id] = doc.getAll()
        }
        _documentStates.value = snapshot
    }

    private fun persistOperation(op: CrdtOperation) {
        chatDao?.let { dao ->
            scope.launch {
                try {
                    dao.insertCrdtOperation(
                        CrdtOperationEntity(
                            opId = op.opId,
                            documentId = op.documentId,
                            actorId = op.actorId,
                            lamportClock = op.lamportClock,
                            timestamp = op.timestamp,
                            operationType = op.type.name,
                            key = op.key,
                            valueJson = op.valueJson
                        )
                    )
                } catch (e: Exception) {
                    Log.e("CrdtEngine", "Failed to persist CRDT op: ${e.message}", e)
                }
            }
        }
    }
}
