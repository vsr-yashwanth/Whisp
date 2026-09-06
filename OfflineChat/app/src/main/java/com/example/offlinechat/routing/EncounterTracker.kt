package com.example.offlinechat.routing

import android.util.Log
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.PeerEncounterEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class LocalEncounterRecord(
    val peerId: String,
    var firstSeen: Long = System.currentTimeMillis(),
    var lastSeen: Long = System.currentTimeMillis(),
    var encounterCount: Int = 1,
    var intervalsSumSeconds: Long = 0L
)

class EncounterTracker(
    private val chatDao: ChatDao? = null
) {
    private val memoryEncounters = ConcurrentHashMap<String, LocalEncounterRecord>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun recordEncounter(peerId: String, transport: String = "BLE_MESH") {
        val now = System.currentTimeMillis()
        val record = memoryEncounters.computeIfAbsent(peerId) {
            LocalEncounterRecord(peerId = peerId, firstSeen = now, lastSeen = now)
        }

        synchronized(record) {
            val intervalSec = ((now - record.lastSeen) / 1000L).coerceAtLeast(0L)
            if (intervalSec > 30) { // Distinct encounter if more than 30s elapsed
                record.encounterCount++
                record.intervalsSumSeconds += intervalSec
            }
            record.lastSeen = now

            val avgInterval = if (record.encounterCount > 1) {
                record.intervalsSumSeconds / (record.encounterCount - 1)
            } else {
                3600L // Default 1 hr
            }

            chatDao?.let { dao ->
                scope.launch {
                    try {
                        dao.insertOrUpdatePeerEncounter(
                            PeerEncounterEntity(
                                peerId = peerId,
                                firstSeen = record.firstSeen,
                                lastSeen = record.lastSeen,
                                encounterCount = record.encounterCount,
                                averageIntervalSeconds = avgInterval,
                                lastTransport = transport,
                                estimatedStability = getEncounterProbability(peerId)
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("EncounterTracker", "Failed to persist peer encounter: ${e.message}", e)
                    }
                }
            }
        }
    }

    fun getEncounterProbability(destinationNodeId: String): Float {
        val record = memoryEncounters[destinationNodeId] ?: return 0.1f
        synchronized(record) {
            val now = System.currentTimeMillis()
            val timeSinceLastSeenSec = ((now - record.lastSeen) / 1000L).coerceAtLeast(0L)

            // High probability if encountered frequently and recently
            val recencyFactor = (1.0 - (timeSinceLastSeenSec / 7200.0)).coerceIn(0.1, 1.0)
            val frequencyFactor = (record.encounterCount / 10.0).coerceIn(0.1, 1.0)

            return ((recencyFactor * 0.6) + (frequencyFactor * 0.4)).toFloat()
        }
    }
}
