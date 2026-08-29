package com.example.offlinechat.network

import com.example.offlinechat.network.dtn.DtnBundle
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

enum class PacketType {
    MESSAGE,
    ACK,
    STORE_FORWARD_QUERY,
    STORE_FORWARD_RESPONSE,
    ROUTE_ERROR,
    HEARTBEAT,
    SOS,
    FILE_CHUNK,
    LOCATION_SHARE,
    GROUP_UPDATE,
    BUNDLE_DATA,
    BUNDLE_INVENTORY_QUERY,
    BUNDLE_INVENTORY_RESPONSE,
    PARTITION_EPOCH_SYNC,
    CRDT_OPERATION,
    CHAOS_BEACON
}

object PacketPriority {
    const val SOS = 100
    const val IMPORTANT = 50
    const val NORMAL = 10
    const val FILE = 5
}

data class MeshPacket(
    val protocolVersion: Int = 3,
    val packetType: PacketType = PacketType.MESSAGE,
    val packetId: String = UUID.randomUUID().toString(),
    val messageId: String = packetId,
    val conversationId: String = "General Chat",
    val senderId: String,
    val recipientId: String = "ALL", // "ALL" for broadcast or target Node ID
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = 10,               // Time-To-Live hop limit
    val hopCount: Int = 0,
    val priority: Int = PacketPriority.NORMAL,
    val payload: String,             // Base64 encrypted transit ciphertext
    val payloadHash: String = computeHash(payload),
    val hops: List<HopRecord> = emptyList(),
    val batteryLevel: Int = -1,      // 0-100% (-1 = unknown)
    val isCharging: Boolean = false,
    val extraMetadata: Map<String, String> = emptyMap()
) {

    fun toJsonString(): String {
        val json = JSONObject().apply {
            put("version", protocolVersion)
            put("type", packetType.name)
            put("packetId", packetId)
            put("messageId", messageId)
            put("conversationId", conversationId)
            put("senderId", senderId)
            put("recipientId", recipientId)
            put("timestamp", timestamp)
            put("ttl", ttl)
            put("hopCount", hopCount)
            put("priority", priority)
            put("payload", payload)
            put("payloadHash", payloadHash)
            put("batteryLevel", batteryLevel)
            put("isCharging", isCharging)

            // Serialize hops
            val hopsArray = JSONArray()
            hops.forEach { hop ->
                hopsArray.put(JSONObject().apply {
                    put("nodeId", hop.nodeId)
                    put("nodeName", hop.nodeName)
                    put("transport", hop.transport)
                    put("timestamp", hop.timestamp)
                    put("latencyMs", hop.latencyMs)
                })
            }
            put("hops", hopsArray)

            // Extra metadata
            val metaObj = JSONObject()
            extraMetadata.forEach { (k, v) -> metaObj.put(k, v) }
            put("metadata", metaObj)
        }
        return json.toString()
    }

    fun stampedWithHop(
        nodeId: String,
        nodeName: String,
        transport: String,
        currentBattery: Int = -1,
        charging: Boolean = false
    ): MeshPacket {
        val lastTimestamp = hops.lastOrNull()?.timestamp ?: timestamp
        val now = System.currentTimeMillis()
        val latency = (now - lastTimestamp).coerceAtLeast(0L)

        val newHop = HopRecord(
            nodeId = nodeId,
            nodeName = nodeName,
            transport = transport,
            timestamp = now,
            latencyMs = latency
        )

        return this.copy(
            ttl = (ttl - 1).coerceAtLeast(0),
            hopCount = hopCount + 1,
            hops = hops + newHop,
            batteryLevel = if (currentBattery >= 0) currentBattery else this.batteryLevel,
            isCharging = if (currentBattery >= 0) charging else this.isCharging
        )
    }

    companion object {
        fun computeHash(data: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
                hashBytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                ""
            }
        }

        fun fromJsonString(jsonStr: String): MeshPacket? {
            return try {
                val obj = JSONObject(jsonStr)

                // Backward-compatibility fallback for legacy V1 packets
                val typeStr = obj.optString("type", "MESSAGE")
                val packetType = try {
                    PacketType.valueOf(typeStr)
                } catch (e: Exception) {
                    PacketType.MESSAGE
                }

                val hopsList = mutableListOf<HopRecord>()
                val hopsArray = obj.optJSONArray("hops")
                if (hopsArray != null) {
                    for (i in 0 until hopsArray.length()) {
                        val hObj = hopsArray.getJSONObject(i)
                        hopsList.add(
                            HopRecord(
                                nodeId = hObj.optString("nodeId", ""),
                                nodeName = hObj.optString("nodeName", ""),
                                transport = hObj.optString("transport", "BLE"),
                                timestamp = hObj.optLong("timestamp", System.currentTimeMillis()),
                                latencyMs = hObj.optLong("latencyMs", 0L)
                            )
                        )
                    }
                }

                val metadataMap = mutableMapOf<String, String>()
                val metaObj = obj.optJSONObject("metadata")
                if (metaObj != null) {
                    val keys = metaObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        metadataMap[key] = metaObj.optString(key, "")
                    }
                }

                val rawPayload = obj.optString("payload", "")
                val payloadHash = obj.optString("payloadHash", computeHash(rawPayload))

                MeshPacket(
                    protocolVersion = obj.optInt("version", 3),
                    packetType = packetType,
                    packetId = obj.optString("packetId", obj.optString("id", UUID.randomUUID().toString())),
                    messageId = obj.optString("messageId", obj.optString("id", UUID.randomUUID().toString())),
                    conversationId = obj.optString("conversationId", "General Chat"),
                    senderId = obj.optString("senderId", obj.optString("sender", "UnknownNode")),
                    recipientId = obj.optString("recipientId", "ALL"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    ttl = obj.optInt("ttl", 10),
                    hopCount = obj.optInt("hopCount", hopsList.size),
                    priority = obj.optInt("priority", PacketPriority.NORMAL),
                    payload = rawPayload,
                    payloadHash = payloadHash,
                    hops = hopsList,
                    batteryLevel = obj.optInt("batteryLevel", -1),
                    isCharging = obj.optBoolean("isCharging", false),
                    extraMetadata = metadataMap
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
