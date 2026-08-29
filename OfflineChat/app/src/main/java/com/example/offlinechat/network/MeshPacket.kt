package com.example.offlinechat.network

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
    GROUP_UPDATE
}

object PacketPriority {
    const val SOS = 100
    const val IMPORTANT = 50
    const val NORMAL = 10
    const val FILE = 5
}

data class MeshPacket(
    val protocolVersion: Int = 2,
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

            val hopsArr = JSONArray()
            hops.forEach { hop ->
                val hObj = JSONObject().apply {
                    put("nodeId", hop.nodeId)
                    put("nodeName", hop.nodeName)
                    put("transport", hop.transport)
                    put("timestamp", hop.timestamp)
                    put("latencyMs", hop.latencyMs)
                }
                hopsArr.put(hObj)
            }
            put("hops", hopsArr)

            if (extraMetadata.isNotEmpty()) {
                val metaObj = JSONObject()
                extraMetadata.forEach { (k, v) -> metaObj.put(k, v) }
                put("extraMetadata", metaObj)
            }
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
        val now = System.currentTimeMillis()
        val prevTimestamp = hops.lastOrNull()?.timestamp ?: timestamp
        val latency = (now - prevTimestamp).coerceAtLeast(0L)

        val newHop = HopRecord(
            nodeId = nodeId,
            nodeName = nodeName,
            transport = transport,
            timestamp = now,
            latencyMs = latency
        )

        return this.copy(
            ttl = (this.ttl - 1).coerceAtLeast(0),
            hopCount = this.hopCount + 1,
            hops = this.hops + newHop,
            batteryLevel = if (currentBattery >= 0) currentBattery else this.batteryLevel,
            isCharging = charging || this.isCharging
        )
    }

    companion object {
        fun computeHash(content: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val bytes = digest.digest(content.toByteArray(Charsets.UTF_8))
                bytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                content.hashCode().toString()
            }
        }

        fun fromJsonString(jsonStr: String): MeshPacket? {
            return try {
                val json = JSONObject(jsonStr)

                // Backward-compatible type resolution
                val typeStr = json.optString("type", "MESSAGE")
                val packetType = try {
                    PacketType.valueOf(typeStr)
                } catch (e: Exception) {
                    PacketType.MESSAGE
                }

                val version = json.optInt("version", 1)
                val msgId = json.optString("messageId", UUID.randomUUID().toString())
                val packetId = json.optString("packetId", msgId)
                val convId = json.optString("conversationId", "General Chat")
                val sender = json.optString("senderId", "UnknownNode")
                val recipient = json.optString("recipientId", "ALL")
                val ts = json.optLong("timestamp", System.currentTimeMillis())
                val ttl = json.optInt("ttl", 10)
                val hopsCount = json.optInt("hopCount", 0)
                val prio = json.optInt("priority", PacketPriority.NORMAL)
                val payloadStr = json.optString("payload", "")
                val hash = json.optString("payloadHash", computeHash(payloadStr))
                val battery = json.optInt("batteryLevel", -1)
                val charging = json.optBoolean("isCharging", false)

                val hopsList = mutableListOf<HopRecord>()
                val hopsArr = json.optJSONArray("hops")
                if (hopsArr != null) {
                    for (i in 0 until hopsArr.length()) {
                        val hObj = hopsArr.optJSONObject(i) ?: continue
                        hopsList.add(
                            HopRecord(
                                nodeId = hObj.optString("nodeId"),
                                nodeName = hObj.optString("nodeName"),
                                transport = hObj.optString("transport", "ORIGIN"),
                                timestamp = hObj.optLong("timestamp", ts),
                                latencyMs = hObj.optLong("latencyMs", 0L)
                            )
                        )
                    }
                }

                val metaMap = mutableMapOf<String, String>()
                val metaObj = json.optJSONObject("extraMetadata")
                if (metaObj != null) {
                    val keys = metaObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        metaMap[k] = metaObj.optString(k)
                    }
                }

                MeshPacket(
                    protocolVersion = version,
                    packetType = packetType,
                    packetId = packetId,
                    messageId = msgId,
                    conversationId = convId,
                    senderId = sender,
                    recipientId = recipient,
                    timestamp = ts,
                    ttl = ttl,
                    hopCount = hopsCount,
                    priority = prio,
                    payload = payloadStr,
                    payloadHash = hash,
                    hops = hopsList,
                    batteryLevel = battery,
                    isCharging = charging,
                    extraMetadata = metaMap
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
