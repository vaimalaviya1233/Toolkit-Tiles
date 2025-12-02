package com.wstxda.toolkit.manager.tools

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class SosManager(context: Context) {

    private val flasher = SosFlasher(context)
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    init {
        managerScope.launch {
            flasher.isTorchAvailable.collectLatest { available ->
                if (!available && _isActive.value) {
                    stop()
                }
            }
        }
    }

    fun hasFlash(): Boolean {
        return flasher.hasFlash && flasher.isTorchAvailable.value
    }

    fun toggle() {
        if (_isActive.value) stop() else start()
    }

    private fun start() {
        if (!hasFlash()) return
        _isActive.value = true
        flasher.start()
    }

    private fun stop() {
        _isActive.value = false
        flasher.stop()
    }

    fun cleanup() {
        stop()
        flasher.cleanup()
        managerScope.cancel()
    }
}