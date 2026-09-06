package com.example.offlinechat.sdk

import com.example.offlinechat.OfflineChatApp
import com.example.offlinechat.network.Peer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class WhispEvent {
    data class PeerDiscovered(val peer: Peer) : WhispEvent()
    data class PeerLost(val peerId: String) : WhispEvent()
    data class RouteChanged(val destinationId: String, val nextHop: String, val latencyMs: Long) : WhispEvent()
    data class MessageDelivered(val messageId: String, val hopsCount: Int) : WhispEvent()
    data class NetworkPartitionDetected(val epoch: Long, val remainingNodes: Int) : WhispEvent()
    data class NetworkPartitionHealed(val epoch: Long, val totalNodes: Int) : WhispEvent()
}

class WhispClient private constructor() {

    private val _events = MutableSharedFlow<WhispEvent>(extraBufferCapacity = 100)
    val events: SharedFlow<WhispEvent> = _events.asSharedFlow()

    fun getLocalNodeId(): String {
        return OfflineChatApp.instance.transport.localId
    }

    fun getDiscoveredPeers(): StateFlow<List<Peer>> {
        return OfflineChatApp.instance.transport.discoveredPeers
    }

    fun sendMessage(destinationNodeId: String, payload: String, priority: Int = 10) {
        val app = OfflineChatApp.instance
        val rawEncrypted = app.cryptoManager.encryptForTransit(payload.toByteArray(Charsets.UTF_8))
        val packet = com.example.offlinechat.network.MeshPacket(
            protocolVersion = 3,
            senderId = getLocalNodeId(),
            recipientId = destinationNodeId,
            payload = android.util.Base64.encodeToString(rawEncrypted, android.util.Base64.NO_WRAP),
            priority = priority
        )
        app.transport.sendData(packet.toJsonString().toByteArray(Charsets.UTF_8))
    }

    fun updateSharedCrdtState(documentId: String, key: String, valueJson: String) {
        // Broadcast CRDT operation
        val op = com.example.offlinechat.crdt.CrdtOperation(
            documentId = documentId,
            actorId = getLocalNodeId(),
            lamportClock = System.currentTimeMillis(),
            type = com.example.offlinechat.crdt.CrdtOpType.SET,
            key = key,
            valueJson = valueJson
        )
        val packet = com.example.offlinechat.network.MeshPacket(
            protocolVersion = 3,
            packetType = com.example.offlinechat.network.PacketType.CRDT_OPERATION,
            senderId = getLocalNodeId(),
            recipientId = "ALL",
            payload = op.toJsonString()
        )
        OfflineChatApp.instance.transport.sendData(packet.toJsonString().toByteArray(Charsets.UTF_8))
    }

    fun emitEvent(event: WhispEvent) {
        _events.tryEmit(event)
    }

    companion object {
        val instance: WhispClient by lazy { WhispClient() }
    }
}
