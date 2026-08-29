package com.example.offlinechat.network.dtn

import android.util.Log
import com.example.offlinechat.data.ChatDao
import com.example.offlinechat.data.DtnBundleEntity
import org.json.JSONArray
import org.json.JSONObject

class BundleInventoryManager(
    private val chatDao: ChatDao
) {
    suspend fun createInventorySummary(destinationFilter: String? = null): String {
        val now = System.currentTimeMillis()
        val activeBundles = chatDao.getActiveDtnBundles(now)
        val filtered = if (destinationFilter != null) {
            activeBundles.filter { it.destination == destinationFilter || it.destination == "ALL" }
        } else {
            activeBundles
        }

        val jsonArray = JSONArray()
        filtered.forEach { b ->
            jsonArray.put(JSONObject().apply {
                put("id", b.bundleId)
                put("hash", b.payloadHash)
                put("dest", b.destination)
                put("prio", b.priority)
                put("exp", b.expirationTime)
            })
        }
        return jsonArray.toString()
    }

    suspend fun computeMissingBundleIds(remoteInventoryJson: String): List<String> {
        val missingIds = mutableListOf<String>()
        try {
            val remoteArr = JSONArray(remoteInventoryJson)
            val now = System.currentTimeMillis()
            val localBundles = chatDao.getActiveDtnBundles(now).map { it.bundleId }.toSet()

            for (i in 0 until remoteArr.length()) {
                val item = remoteArr.getJSONObject(i)
                val bundleId = item.getString("id")
                val exp = item.optLong("exp", 0L)
                if (exp > now && !localBundles.contains(bundleId)) {
                    missingIds.add(bundleId)
                }
            }
        } catch (e: Exception) {
            Log.e("BundleInventory", "Error parsing remote inventory: ${e.message}", e)
        }
        return missingIds
    }
}
