package com.example.offlinechat.network

import android.util.Log
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.NetworkEpochEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest

data class PartitionStatus(
    val currentEpoch: Long = 1L,
    val activeMemberCount: Int = 1,
    val isPartitioned: Boolean = false,
    val lastPartitionTime: Long = 0L,
    val lastReconciledTime: Long = 0L,
    val reconciliationStatus: String = "SYNCHRONIZED"
)

class PartitionManager(
    private val localNodeId: String,
    private val chatDao: ChatDao? = null,
    private val sendRawPacket: (ByteArray) -> Unit = {}
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _partitionStatus = MutableStateFlow(PartitionStatus())
    val partitionStatus: StateFlow<PartitionStatus> = _partitionStatus.asStateFlow()

    private var previousPeerCount = 0
    private var currentEpochCounter = 1L

    fun onPeerTopologyUpdated(currentPeers: List<Peer>) {
        val currentCount = currentPeers.size
        val now = System.currentTimeMillis()

        // 1. Detect Partition Split: Sudden drop in peers (>=2 dropped from a multi-peer mesh)
        if (previousPeerCount >= 3 && currentCount <= previousPeerCount / 2) {
            currentEpochCounter++
            Log.w("PartitionManager", "[WARNING] Network partition detected! Peers dropped from $previousPeerCount to $currentCount (New Epoch: $currentEpochCounter)")
            _partitionStatus.value = _partitionStatus.value.copy(
                currentEpoch = currentEpochCounter,
                activeMemberCount = currentCount + 1,
                isPartitioned = true,
                lastPartitionTime = now,
                reconciliationStatus = "PARTITION_ISOLATED"
            )
            persistEpoch(currentEpochCounter, currentCount + 1, 2)
        }
        // 2. Detect Partition Merge: New peers reappear after a partition
        else if (_partitionStatus.value.isPartitioned && currentCount > previousPeerCount) {
            currentEpochCounter++
            Log.i("PartitionManager", "[INFO] Network Partition Reconnected! Healing and reconciling with $currentCount peers (Epoch: $currentEpochCounter)")
            _partitionStatus.value = _partitionStatus.value.copy(
                currentEpoch = currentEpochCounter,
                activeMemberCount = currentCount + 1,
                isPartitioned = false,
                lastReconciledTime = now,
                reconciliationStatus = "RECONCILIATION_COMPLETE"
            )
            persistEpoch(currentEpochCounter, currentCount + 1, 1)
            broadcastEpochSync(currentEpochCounter, currentPeers)
        } else {
            _partitionStatus.value = _partitionStatus.value.copy(
                activeMemberCount = currentCount + 1
            )
        }

        previousPeerCount = currentCount
    }

    fun handleIncomingEpochSync(syncJson: String) {
        try {
            val json = JSONObject(syncJson)
            val remoteEpoch = json.getLong("epoch")
            val remoteNodeId = json.getString("senderId")
            Log.d("PartitionManager", "Received Epoch Sync ($remoteEpoch) from ($remoteNodeId)")

            if (remoteEpoch > currentEpochCounter) {
                currentEpochCounter = remoteEpoch
                _partitionStatus.value = _partitionStatus.value.copy(
                    currentEpoch = remoteEpoch,
                    reconciliationStatus = "STATE_RECONCILED"
                )
            }
        } catch (e: Exception) {
            Log.e("PartitionManager", "Error handling epoch sync: ${e.message}", e)
        }
    }

    private fun broadcastEpochSync(epoch: Long, peers: List<Peer>) {
        val payloadObj = JSONObject().apply {
            put("epoch", epoch)
            put("senderId", localNodeId)
            put("timestamp", System.currentTimeMillis())
            put("memberCount", peers.size + 1)
        }

        val packet = MeshPacket(
            protocolVersion = 3,
            packetType = PacketType.PARTITION_EPOCH_SYNC,
            senderId = localNodeId,
            recipientId = "ALL",
            payload = payloadObj.toString(),
            priority = PacketPriority.IMPORTANT
        )

        sendRawPacket(packet.toJsonString().toByteArray(Charsets.UTF_8))
    }

    private fun persistEpoch(epoch: Long, members: Int, partitions: Int) {
        chatDao?.let { dao ->
            scope.launch {
                try {
                    dao.insertNetworkEpoch(
                        NetworkEpochEntity(
                            epochNumber = epoch,
                            timestamp = System.currentTimeMillis(),
                            detectedPartitionCount = partitions,
                            knownMemberCount = members,
                            stateHash = "epoch-$epoch-$members"
                        )
                    )
                } catch (e: Exception) {
                    Log.e("PartitionManager", "Failed to persist epoch: ${e.message}", e)
                }
            }
        }
    }
}
