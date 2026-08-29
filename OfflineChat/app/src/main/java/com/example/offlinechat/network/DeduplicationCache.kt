package com.example.offlinechat.network

import java.util.Collections
import java.util.LinkedHashMap
import java.util.concurrent.TimeUnit

class DeduplicationCache(
    private val maxCapacity: Int = 5000,
    private val ttlMillis: Long = TimeUnit.MINUTES.toMillis(15)
) {

    private data class CacheEntry(
        val timestamp: Long,
        val payloadHash: String?
    )

    // Thread-safe LRU Map
    private val lruMap: MutableMap<String, CacheEntry> = Collections.synchronizedMap(
        object : LinkedHashMap<String, CacheEntry>(maxCapacity, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
                return size > maxCapacity
            }
        }
    )

    // Secondary index for payload hashes
    private val hashIndex: MutableMap<String, String> = Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(maxCapacity, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > maxCapacity
            }
        }
    )

    /**
     * Checks whether the packet ID or payload hash has already been processed within the TTL window.
     * If not seen, records it and returns false.
     * If already seen, returns true (is duplicate).
     */
    @Synchronized
    fun isDuplicateOrRecord(packetId: String, payloadHash: String? = null): Boolean {
        if (packetId.isBlank()) return false
        val now = System.currentTimeMillis()

        // 1. Check primary packetId cache
        val existingEntry = lruMap[packetId]
        if (existingEntry != null) {
            if (now - existingEntry.timestamp < ttlMillis) {
                return true // Duplicate within TTL
            } else {
                lruMap.remove(packetId)
            }
        }

        // 2. Check secondary payloadHash cache (for multi-route identical payloads)
        if (!payloadHash.isNullOrBlank()) {
            val existingId = hashIndex[payloadHash]
            if (existingId != null && existingId != packetId) {
                val linkedEntry = lruMap[existingId]
                if (linkedEntry != null && now - linkedEntry.timestamp < ttlMillis) {
                    return true // Duplicate payload received via different packetId
                }
            }
        }

        // 3. Record new entry
        lruMap[packetId] = CacheEntry(now, payloadHash)
        if (!payloadHash.isNullOrBlank()) {
            hashIndex[payloadHash] = packetId
        }

        return false
    }

    @Synchronized
    fun contains(packetId: String): Boolean {
        val entry = lruMap[packetId] ?: return false
        val now = System.currentTimeMillis()
        if (now - entry.timestamp >= ttlMillis) {
            lruMap.remove(packetId)
            return false
        }
        return true
    }

    @Synchronized
    fun purgeExpired() {
        val now = System.currentTimeMillis()
        val it = lruMap.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (now - entry.value.timestamp >= ttlMillis) {
                entry.value.payloadHash?.let { hashIndex.remove(it) }
                it.remove()
            }
        }
    }

    @Synchronized
    fun size(): Int {
        return lruMap.size
    }

    @Synchronized
    fun clear() {
        lruMap.clearproxy()
        hashIndex.clear()
    }

    private fun MutableMap<String, CacheEntry>.clearproxy() {
        this.clear()
    }
}
