package com.example.offlinechat.routing

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.offlinechat.network.MeshPacket
import com.example.offlinechat.network.PacketPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryRelayConfig(
    val relayEnabled: Boolean = true,
    val minimumBatteryThreshold: Int = 20 // Percent (e.g. 20%)
)

class BatteryRelayPolicy(private val context: Context) {

    private val _config = MutableStateFlow(BatteryRelayConfig())
    val config: StateFlow<BatteryRelayConfig> = _config.asStateFlow()

    fun updateRelayEnabled(enabled: Boolean) {
        _config.value = _config.value.copy(relayEnabled = enabled)
    }

    fun updateMinimumThreshold(threshold: Int) {
        _config.value = _config.value.copy(minimumBatteryThreshold = threshold.coerceIn(5, 80))
    }

    fun getBatteryLevel(): Int {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                ((level / scale.toFloat()) * 100).toInt()
            } else 100
        } catch (e: Exception) {
            100
        }
    }

    fun isCharging(): Boolean {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Determines whether this node should relay a given packet based on battery policy.
     * SOS packets are ALWAYS relayed regardless of battery level.
     */
    fun shouldRelay(packet: MeshPacket): Boolean {
        // Emergency SOS packets always bypass battery throttling
        if (packet.priority >= PacketPriority.SOS || packet.packetType == com.example.offlinechat.network.PacketType.SOS) {
            return true
        }

        val currentConfig = _config.value
        if (!currentConfig.relayEnabled) {
            return false
        }

        if (isCharging()) {
            return true
        }

        val currentLevel = getBatteryLevel()
        return currentLevel >= currentConfig.minimumBatteryThreshold
    }
}
