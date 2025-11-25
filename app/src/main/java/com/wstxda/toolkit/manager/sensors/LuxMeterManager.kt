package com.wstxda.toolkit.manager.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.sensors.getLux
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object LuxMeterManager : SensorEventListener {

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private val _lux = MutableStateFlow(0)
    val lux = _lux.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var isSensorRegistered = false

    fun initialize(context: Context) {
        if (sensorManager != null) return

        sensorManager = context.getSystemService<SensorManager>()
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    fun isSupported(context: Context): Boolean {
        val sm = context.getSystemService<SensorManager>()
        return sm?.getDefaultSensor(Sensor.TYPE_LIGHT) != null
    }

    fun start() {
        _isActive.value = true
        registerSensor()
    }

    fun stop() {
        _isActive.value = false
        unregisterSensor()
    }

    fun resume() {
        if (_isActive.value) registerSensor()
    }

    fun pause() {
        unregisterSensor()
    }

    fun setForceActive(active: Boolean) {
        _isActive.value = active
    }

    private fun registerSensor() {
        if (lightSensor != null && !isSensorRegistered) {
            sensorManager?.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI)
            isSensorRegistered = true
        }
    }

    private fun unregisterSensor() {
        if (isSensorRegistered) {
            sensorManager?.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        _lux.value = event.getLux()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}