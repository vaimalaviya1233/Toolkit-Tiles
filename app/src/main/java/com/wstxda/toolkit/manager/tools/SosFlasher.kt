package com.wstxda.toolkit.manager.tools

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SosFlasher(context: Context) {

    private val haptics = Haptics(context)
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn = _isTorchOn.asStateFlow()

    private val _torchAvailable = MutableStateFlow(true)
    val isTorchAvailable = _torchAvailable.asStateFlow()

    private val cameraId: String? = try {
        cameraManager.cameraIdList.find { id ->
            val c = cameraManager.getCameraCharacteristics(id)
            c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true && c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    } catch (_: Exception) {
        null
    }

    val hasFlash: Boolean get() = cameraId != null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var job: Job? = null
    private val unit = 200L

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(id: String, enabled: Boolean) {
            if (id == cameraId) {
                _isTorchOn.value = enabled
                _torchAvailable.value = true
            }
        }

        override fun onTorchModeUnavailable(id: String) {
            if (id == cameraId) {
                _torchAvailable.value = false
            }
        }
    }

    init {
        cameraId?.let {
            cameraManager.registerTorchCallback(torchCallback, null)
        }
    }

    val isRunning: Boolean get() = job?.isActive == true

    fun start() {
        if (!hasFlash || !_torchAvailable.value || _isTorchOn.value || isRunning) return

        job?.cancel()
        job = scope.launch {
            while (isActive) {
                delay(unit * 2)
                sendS()
                delay(unit * 2)
                sendO()
                delay(unit * 2)
                sendS()
                delay(unit * 4)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        setTorch(false)
        haptics.cancel()
    }

    private suspend fun sendS() = repeat(3) { dot() }
    private suspend fun sendO() = repeat(3) { dash() }

    private suspend fun dot() {
        blink(unit)
        delay(unit)
    }

    private suspend fun dash() {
        blink(unit * 3)
        delay(unit)
    }

    private suspend fun blink(duration: Long) {
        setTorch(true)
        haptics.long(duration)
        delay(duration)
        setTorch(false)
    }

    private fun setTorch(enabled: Boolean) {
        try {
            cameraId?.let { cameraManager.setTorchMode(it, enabled) }
        } catch (_: Exception) {
            _torchAvailable.value = false
        }
    }
}