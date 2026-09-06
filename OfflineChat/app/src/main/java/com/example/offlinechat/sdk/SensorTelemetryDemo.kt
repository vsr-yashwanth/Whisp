package com.example.offlinechat.sdk

import kotlinx.coroutines.*
import org.json.JSONObject

class SensorTelemetryDemo(
    private val sensorId: String = "SENSOR-NODE-ALPHA",
    private val client: WhispClient = WhispClient.instance
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var telemetryJob: Job? = null

    fun startPeriodicTelemetry(targetSinkNodeId: String = "ALL", intervalMillis: Long = 10_000L) {
        telemetryJob?.cancel()
        telemetryJob = scope.launch {
            var readingCounter = 0
            while (isActive) {
                readingCounter++
                val temperature = 24.0 + (Math.random() * 8.0)
                val payloadObj = JSONObject().apply {
                    put("sensorId", sensorId)
                    put("counter", readingCounter)
                    put("temperatureCelsius", "%.1f".format(temperature))
                    put("timestamp", System.currentTimeMillis())
                    put("status", "OPERATIONAL")
                }

                try {
                    client.sendMessage(
                        destinationNodeId = targetSinkNodeId,
                        payload = payloadObj.toString(),
                        priority = 5 // File/Telemetry priority
                    )
                } catch (e: Exception) {
                    // Ignore offline
                }

                delay(intervalMillis)
            }
        }
    }

    fun stop() {
        telemetryJob?.cancel()
        telemetryJob = null
    }
}
