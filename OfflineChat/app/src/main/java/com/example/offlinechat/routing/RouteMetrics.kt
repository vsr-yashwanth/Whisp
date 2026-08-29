package com.example.offlinechat.routing

data class RouteMetrics(
    val hopCount: Int = 1,
    val averageLatencyMs: Long = 20L,
    val packetLossRate: Float = 0.0f,      // 0.0 (0%) to 1.0 (100%)
    val batteryLevel: Int = 100,           // 0 to 100% (-1 = unknown)
    val isCharging: Boolean = false,
    val transportType: String = "BLE_MESH",// BLE_MESH, WIFI_DIRECT, LOCAL_BRIDGE, GLOBAL_RELAY
    val connectionUptimeMs: Long = 60_000L,
    val recentDeliverySuccessRate: Float = 1.0f
)

data class RouteCandidate(
    val destinationNodeId: String,
    val nextHopNodeId: String,
    val nextHopName: String,
    val viaTransport: String,
    val metrics: RouteMetrics,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class RoutingConfig(
    val wHop: Float = 10.0f,
    val wLatency: Float = 0.1f,            // 100ms = 10 points
    val wPacketLoss: Float = 80.0f,        // 10% loss = 8 points
    val wLowBattery: Float = 50.0f,        // Low battery (<20%) = 50 points penalty
    val wCriticalBattery: Float = 300.0f,  // Critical battery (<10%) = 300 points penalty
    val wInstability: Float = 25.0f,
    val maxRouteAgeMillis: Long = 180_000L // 3 minutes expiration
)
