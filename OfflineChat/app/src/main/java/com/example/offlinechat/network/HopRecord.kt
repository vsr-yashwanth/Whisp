package com.example.offlinechat.network

import kotlinx.serialization.Serializable

@Serializable
data class HopRecord(
    val nodeId: String,
    val nodeName: String,
    val transport: String, // "BLE_MESH", "WIFI_DIRECT", "LOCAL_BRIDGE", "GLOBAL_RELAY", "WEB_GATEWAY"
    val timestamp: Long,
    val latencyMs: Long = 0L
)
