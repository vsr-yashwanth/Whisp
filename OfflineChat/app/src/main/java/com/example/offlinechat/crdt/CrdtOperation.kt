package com.example.offlinechat.crdt

import org.json.JSONObject
import java.util.UUID

enum class CrdtOpType {
    SET,
    DELETE
}

data class CrdtOperation(
    val opId: String = UUID.randomUUID().toString(),
    val documentId: String,
    val actorId: String,
    val lamportClock: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val type: CrdtOpType = CrdtOpType.SET,
    val key: String,
    val valueJson: String = ""
) {
    fun toJsonString(): String {
        return JSONObject().apply {
            put("opId", opId)
            put("documentId", documentId)
            put("actorId", actorId)
            put("lamportClock", lamportClock)
            put("timestamp", timestamp)
            put("type", type.name)
            put("key", key)
            put("valueJson", valueJson)
        }.toString()
    }

    companion object {
        fun fromJsonString(jsonStr: String): CrdtOperation? {
            return try {
                val obj = JSONObject(jsonStr)
                CrdtOperation(
                    opId = obj.getString("opId"),
                    documentId = obj.getString("documentId"),
                    actorId = obj.getString("actorId"),
                    lamportClock = obj.getLong("lamportClock"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    type = try { CrdtOpType.valueOf(obj.getString("type")) } catch (e: Exception) { CrdtOpType.SET },
                    key = obj.getString("key"),
                    valueJson = obj.optString("valueJson", "")
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class CrdtEntry(
    val value: String,
    val lamportClock: Long,
    val timestamp: Long,
    val actorId: String,
    val isTombstone: Boolean = false
) : Comparable<CrdtEntry> {
    override fun compareTo(other: CrdtEntry): Int {
        // 1. Compare Lamport clock
        val clockDiff = this.lamportClock.compareTo(other.lamportClock)
        if (clockDiff != 0) return clockDiff

        // 2. Compare physical timestamp
        val timeDiff = this.timestamp.compareTo(other.timestamp)
        if (timeDiff != 0) return timeDiff

        // 3. Deterministic tie-breaker on Actor ID
        return this.actorId.compareTo(other.actorId)
    }
}
