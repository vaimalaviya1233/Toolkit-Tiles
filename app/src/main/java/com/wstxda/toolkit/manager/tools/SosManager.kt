package com.wstxda.toolkit.manager.tools

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SosManager {

    private var flasher: SosFlasher? = null

    private val _isActive = MutableStateFlow(false)
    val isActive = _isActive.asStateFlow()

    fun init(context: Context) {
        if (flasher == null) {
            flasher = SosFlasher(context)
        }
    }

    fun hasFlash(): Boolean {
        val f = flasher ?: return false
        return f.hasFlash && f.isTorchAvailable.value && !f.isTorchOn.value
    }

    fun toggle() {
        if (_isActive.value) stop() else start()
    }

    private fun start() {
        if (!hasFlash()) return
        flasher?.start()
        _isActive.value = true
    }

    private fun stop() {
        flasher?.stop()
        _isActive.value = false
    }
}