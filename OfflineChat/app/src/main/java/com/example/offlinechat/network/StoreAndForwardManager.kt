package com.example.offlinechat.network

import android.util.Log
import com.example.offlinechat.data.BufferedPacket
import com.example.offlinechat.data.ChatDao
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class StoreAndForwardManager(
    private val chatDao: ChatDao,
    private val sendFunction: (ByteArray) -> Unit,
    private val defaultTtlMillis: Long = TimeUnit.HOURS.toMillis(24)
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cleanupJob: Job? = null

    fun start() {
        startPeriodicCleanup()
    }

    fun stop() {
        cleanupJob?.cancel()
        cleanupJob = null
    }

    fun bufferPacket(packet: MeshPacket) {
        scope.launch {
            try {
                val now = System.currentTimeMillis()
                val ttlMillis = (packet.ttl * 60_000L).coerceAtLeast(defaultTtlMillis)
                val expiresAt = now + ttlMillis

                val buffered = BufferedPacket(
                    packetId = packet.packetId,
                    messageId = packet.messageId,
                    recipientId = packet.recipientId,
                    conversationId = packet.conversationId,
                    priority = packet.priority,
                    ttl = packet.ttl,
                    createdAt = now,
                    expiresAt = expiresAt,
                    retryCount = 0,
                    rawJsonPayload = packet.toJsonString()
                )
                chatDao.insertBufferedPacket(buffered)
                Log.d("StoreAndForward", "Buffered packet (${packet.packetId}) for recipient (${packet.recipientId})")
            } catch (e: Exception) {
                Log.e("StoreAndForward", "Failed to buffer packet: ${e.message}", e)
            }
        }
    }

    fun onPeerConnectedOrDiscovered(peerId: String) {
        scope.launch {
            try {
                val packets = chatDao.getBufferedPacketsForRecipient(peerId)
                if (packets.isNotEmpty()) {
                    Log.d("StoreAndForward", "Flushing ${packets.size} buffered packets to newly reachable peer ($peerId)")
                    val now = System.currentTimeMillis()
                    for (buffered in packets) {
                        if (buffered.expiresAt <= now) {
                            chatDao.deleteBufferedPacket(buffered.packetId)
                            continue
                        }

                        // Send buffered packet
                        try {
                            sendFunction(buffered.rawJsonPayload.toByteArray(Charsets.UTF_8))
                            chatDao.deleteBufferedPacket(buffered.packetId)
                            Log.d("StoreAndForward", "Delivered buffered packet (${buffered.packetId}) to ($peerId)")
                        } catch (e: Exception) {
                            chatDao.incrementRetryCount(buffered.packetId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StoreAndForward", "Error flushing buffered packets: ${e.message}", e)
            }
        }
    }

    private fun startPeriodicCleanup() {
        cleanupJob = scope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val deleted = chatDao.deleteExpiredBufferedPackets(now)
                    if (deleted > 0) {
                        Log.d("StoreAndForward", "Purged $deleted expired buffered packets")
                    }
                } catch (e: Exception) {
                    Log.e("StoreAndForward", "Error purging expired packets", e)
                }
                delay(TimeUnit.MINUTES.toMillis(2))
            }
        }
    }
}
