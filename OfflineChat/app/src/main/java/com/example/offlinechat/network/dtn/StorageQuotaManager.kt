package com.example.offlinechat.network.dtn

import android.util.Log
import com.example.offlinechat.data.ChatDao
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class StorageQuotaManager(
    private val chatDao: ChatDao,
    private val maxQuotaBytes: Long = 500L * 1024L * 1024L // 500 MB default
) {
    val totalStorageBytes: Flow<Long> = chatDao.getTotalDtnStorageBytes()

    suspend fun enforceQuota(newBundleSizeBytes: Long): Boolean {
        try {
            // Check if adding this bundle exceeds quota
            val currentBytes = chatDao.getActiveDtnBundles(System.currentTimeMillis()).sumOf { it.sizeBytes }
            var neededBytes = (currentBytes + newBundleSizeBytes) - maxQuotaBytes

            if (neededBytes <= 0) {
                return true // Quota is fine
            }

            Log.w("StorageQuotaManager", "DTN Storage quota exceeded. Evicting low-priority bundles to free $neededBytes bytes")

            // Evict lowest priority bundles
            val candidates = chatDao.getEvictionCandidates(limit = 50)
            for (candidate in candidates) {
                chatDao.deleteDtnBundle(candidate.bundleId)
                neededBytes -= candidate.sizeBytes
                Log.d("StorageQuotaManager", "Evicted bundle (${candidate.bundleId}) to satisfy quota")
                if (neededBytes <= 0) break
            }

            return neededBytes <= 0
        } catch (e: Exception) {
            Log.e("StorageQuotaManager", "Error enforcing quota: ${e.message}", e)
            return true
        }
    }
}
