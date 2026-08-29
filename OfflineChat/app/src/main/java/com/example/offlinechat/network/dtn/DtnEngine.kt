package com.example.offlinechat.network.dtn

import android.util.Log
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.DtnBundleEntity
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketType
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class DtnEngine(
    private val chatDao: ChatDao,
    private val sendRawData: (ByteArray) -> Unit,
    private val quotaManager: StorageQuotaManager = StorageQuotaManager(chatDao),
    val inventoryManager: BundleInventoryManager = BundleInventoryManager(chatDao)
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var cleanupJob: Job? = null

    fun start() {
        startPeriodicExpirationCleanup()
    }

    fun stop() {
        cleanupJob?.cancel()
        cleanupJob = null
    }

    suspend fun ingestAndStoreBundle(bundle: DtnBundle): Boolean {
        return try {
            val now = System.currentTimeMillis()
            if (bundle.isExpired(now)) {
                Log.w("DtnEngine", "Dropped incoming bundle (${bundle.bundleId}) because it is expired")
                return false
            }

            val rawJson = bundle.toJsonString()
            val sizeBytes = rawJson.toByteArray(Charsets.UTF_8).size.toLong()

            // Enforce storage quota before insertion
            quotaManager.enforceQuota(sizeBytes)

            val entity = DtnBundleEntity(
                bundleId = bundle.bundleId,
                messageId = bundle.messageId,
                source = bundle.source,
                destination = bundle.destination,
                creationTime = bundle.creationTime,
                expirationTime = bundle.expirationTime,
                ttl = bundle.ttl,
                priority = bundle.priority,
                hopCount = bundle.hopCount,
                replicationCount = bundle.replicationCount,
                maxReplications = bundle.maxReplications,
                payload = bundle.payload,
                payloadHash = bundle.payloadHash,
                custodyState = BundleCustodyState.STORED.name,
                deliveryProbability = bundle.deliveryProbability,
                sizeBytes = sizeBytes,
                rawJson = rawJson
            )

            chatDao.insertDtnBundle(entity)
            Log.d("DtnEngine", "Stored bundle in DTN custody (${bundle.bundleId}) for destination (${bundle.destination})")
            true
        } catch (e: Exception) {
            Log.e("DtnEngine", "Failed to store DTN bundle: ${e.message}", e)
            false
        }
    }

    fun onPeerConnectedOrDiscovered(peerId: String) {
        scope.launch {
            try {
                // Check if we have bundles specifically intended for this peer or broadcast "ALL"
                val bundlesForPeer = chatDao.getDtnBundlesForDestination(peerId)
                if (bundlesForPeer.isNotEmpty()) {
                    Log.d("DtnEngine", "Opportunistically delivering ${bundlesForPeer.size} stored bundles to peer ($peerId)")
                    val now = System.currentTimeMillis()
                    for (b in bundlesForPeer) {
                        if (b.expirationTime <= now) {
                            chatDao.deleteDtnBundle(b.bundleId)
                            continue
                        }

                        // Dispatch as BUNDLE_DATA packet
                        val packet = MeshPacket(
                            protocolVersion = 3,
                            packetType = PacketType.BUNDLE_DATA,
                            packetId = b.bundleId,
                            messageId = b.messageId,
                            senderId = b.source,
                            recipientId = b.destination,
                            timestamp = b.creationTime,
                            ttl = b.ttl,
                            hopCount = b.hopCount + 1,
                            priority = b.priority,
                            payload = b.payload,
                            payloadHash = b.payloadHash
                        )

                        sendRawData(packet.toJsonString().toByteArray(Charsets.UTF_8))
                        chatDao.updateDtnBundleCustodyState(b.bundleId, BundleCustodyState.FORWARDED.name)
                        Log.d("DtnEngine", "Delivered DTN bundle (${b.bundleId}) to ($peerId)")
                    }
                }
            } catch (e: Exception) {
                Log.e("DtnEngine", "Error during opportunistic DTN peer flush: ${e.message}", e)
            }
        }
    }

    private fun startPeriodicExpirationCleanup() {
        cleanupJob = scope.launch {
            while (isActive) {
                try {
                    val now = System.currentTimeMillis()
                    val purged = chatDao.deleteExpiredDtnBundles(now)
                    if (purged > 0) {
                        Log.d("DtnEngine", "DTN Expiration Pruner removed $purged expired bundles")
                    }
                } catch (e: Exception) {
                    Log.e("DtnEngine", "Error pruning expired DTN bundles", e)
                }
                delay(TimeUnit.MINUTES.toMillis(2))
            }
        }
    }
}
