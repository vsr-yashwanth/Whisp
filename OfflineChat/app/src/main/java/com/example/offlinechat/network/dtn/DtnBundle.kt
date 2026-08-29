package com.example.offlinechat.network.dtn

import org.json.JSONObject
import java.security.MessageDigest

enum class BundleCustodyState {
    RECEIVED,
    STORED,
    FORWARDING,
    FORWARDED,
    DELIVERED,
    EXPIRED,
    DROPPED
}

data class DtnBundle(
    val bundleId: String,
    val messageId: String,
    val source: String,
    val destination: String,
    val creationTime: Long = System.currentTimeMillis(),
    val expirationTime: Long,
    val ttl: Int, // TTL in minutes
    val priority: Int = 10,
    val hopCount: Int = 0,
    val replicationCount: Int = 0,
    val maxReplications: Int = 3,
    val payload: String, // Encrypted transit ciphertext (Base64)
    val payloadHash: String = computeSha256(payload),
    val custodyState: BundleCustodyState = BundleCustodyState.RECEIVED,
    val deliveryProbability: Float = 0.5f,
    val routingMetadata: String = "{}",
    val securityMetadata: String = "{}"
) {
    fun toJsonString(): String {
        return JSONObject().apply {
            put("bundleId", bundleId)
            put("messageId", messageId)
            put("source", source)
            put("destination", destination)
            put("creationTime", creationTime)
            put("expirationTime", expirationTime)
            put("ttl", ttl)
            put("priority", priority)
            put("hopCount", hopCount)
            put("replicationCount", replicationCount)
            put("maxReplications", maxReplications)
            put("payload", payload)
            put("payloadHash", payloadHash)
            put("custodyState", custodyState.name)
            put("deliveryProbability", deliveryProbability.toDouble())
            put("routingMetadata", routingMetadata)
            put("securityMetadata", securityMetadata)
        }.toString()
    }

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        return now >= expirationTime
    }

    fun canReplicate(): Boolean {
        return replicationCount < maxReplications
    }

    companion object {
        fun computeSha256(data: String): String {
            return try {
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(data.toByteArray(Charsets.UTF_8))
                hashBytes.joinToString("") { "%02x".format(it) }
            } catch (e: Exception) {
                ""
            }
        }

        fun fromJsonString(jsonStr: String): DtnBundle? {
            return try {
                val obj = JSONObject(jsonStr)
                DtnBundle(
                    bundleId = obj.getString("bundleId"),
                    messageId = obj.optString("messageId", obj.getString("bundleId")),
                    source = obj.getString("source"),
                    destination = obj.getString("destination"),
                    creationTime = obj.optLong("creationTime", System.currentTimeMillis()),
                    expirationTime = obj.getLong("expirationTime"),
                    ttl = obj.optInt("ttl", 1440),
                    priority = obj.optInt("priority", 10),
                    hopCount = obj.optInt("hopCount", 0),
                    replicationCount = obj.optInt("replicationCount", 0),
                    maxReplications = obj.optInt("maxReplications", 3),
                    payload = obj.getString("payload"),
                    payloadHash = obj.optString("payloadHash", computeSha256(obj.getString("payload"))),
                    custodyState = try {
                        BundleCustodyState.valueOf(obj.optString("custodyState", "RECEIVED"))
                    } catch (e: Exception) {
                        BundleCustodyState.RECEIVED
                    },
                    deliveryProbability = obj.optDouble("deliveryProbability", 0.5).toFloat(),
                    routingMetadata = obj.optString("routingMetadata", "{}"),
                    securityMetadata = obj.optString("securityMetadata", "{}")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
