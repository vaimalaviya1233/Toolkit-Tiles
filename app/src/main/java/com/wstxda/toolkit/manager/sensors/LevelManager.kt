package com.wstxda.toolkit.manager.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Display
import androidx.core.content.getSystemService
import com.wstxda.toolkit.services.sensors.Mode
import com.wstxda.toolkit.services.sensors.Orientation
import com.wstxda.toolkit.services.sensors.getOrientation
import com.wstxda.toolkit.services.sensors.getTilt
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

object LevelManager : SensorEventListener {

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    private val _orientation = MutableStateFlow(Orientation(0f, 0f, 0f, Mode.Dot))
    val orientation = _orientation.asStateFlow()

    private val _degrees = MutableStateFlow(0)
    val degrees = _degrees.asStateFlow()

    private var appContext: Context? = null

    private val sensorManager: SensorManager?
        get() = appContext?.getSystemService()

    private val rotationSensor: Sensor?
        get() = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val display: Display?
        get() = appContext?.getSystemService(android.hardware.display.DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)

    private val haptics: Haptics?
        get() = appContext?.let { Haptics(it) }

    private var isSensorRegistered = false
    private var lastHapticFeedback = 0L

    fun initialize(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
        }
    }

    fun isSupported(context: Context): Boolean {
        val sm = context.getSystemService<SensorManager>()
        return sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null
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
        val sensor = rotationSensor ?: return
        if (!isSensorRegistered) {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
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

        val rotation = display?.rotation ?: 0

        val orient = getOrientation(event, rotation)
        _orientation.value = orient

        val deg = when (orient.mode) {
            Mode.Line -> orient.balance.roundToInt()
            Mode.Dot -> getTilt(orient.pitch, orient.roll)
        }

        _degrees.value = deg

        if (deg == 0) vibrateOnZero()
    }

    private fun vibrateOnZero() {
        val now = System.currentTimeMillis()
        if (now - lastHapticFeedback > 500) {
            haptics?.tick()
            lastHapticFeedback = now
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}