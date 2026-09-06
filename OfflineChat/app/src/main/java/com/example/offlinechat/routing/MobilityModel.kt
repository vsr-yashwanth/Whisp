package com.example.offlinechat.routing

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

enum class MovementState {
    STATIONARY,
    WALKING,
    RUNNING,
    VEHICLE,
    UNKNOWN
}

class MobilityClassifier(private val context: Context?) : SensorEventListener {

    private val _currentMovementState = MutableStateFlow(MovementState.STATIONARY)
    val currentMovementState: StateFlow<MovementState> = _currentMovementState.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var accelSensor: Sensor? = null
    private var lastMagnitude = 9.8f

    fun start() {
        if (context == null) return
        try {
            sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
            accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            accelSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } catch (e: Exception) {
            _currentMovementState.value = MovementState.STATIONARY
        }
    }

    fun stop() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = Math.abs(magnitude - lastMagnitude)
            lastMagnitude = magnitude

            val newState = when {
                delta < 0.3f -> MovementState.STATIONARY
                delta in 0.3f..2.5f -> MovementState.WALKING
                delta in 2.5f..6.0f -> MovementState.RUNNING
                delta > 6.0f -> MovementState.VEHICLE
                else -> MovementState.UNKNOWN
            }
            _currentMovementState.value = newState
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
