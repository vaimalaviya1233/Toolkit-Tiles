package com.wstxda.toolkit.ui.utils

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class Haptics(private val context: Context) {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Vibrator::class.java)
        }!!
    }

    fun tick() {
        perform(getEffectTick())
    }

    fun long(duration: Long, force: Boolean = false) {
        performOneShot(duration, force)
    }

    fun cancel() {
        vibrator.cancel()
    }

    private fun perform(effectId: Int) {
        if (!isAllowed(force = false)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val effect = VibrationEffect.createPredefined(effectId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attrs =
                    VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_TOUCH).build()
                vibrator.vibrate(effect, attrs)
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(effect)
            }
        } else {
            val effect = VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
            @Suppress("DEPRECATION") vibrator.vibrate(effect)
        }
    }

    private fun performOneShot(duration: Long, force: Boolean) {
        if (!isAllowed(force)) return

        val effect = VibrationEffect.createOneShot(
            duration, VibrationEffect.DEFAULT_AMPLITUDE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val usage =
                if (force) VibrationAttributes.USAGE_ALARM else VibrationAttributes.USAGE_TOUCH

            val attrs = VibrationAttributes.Builder().setUsage(usage).build()
            vibrator.vibrate(effect, attrs)
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(effect)
        }
    }

    private fun getEffectTick(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK
        else 2

    private fun isAllowed(force: Boolean): Boolean {
        if (!vibrator.hasVibrator()) return false
        if (force) return true

        val am = context.getSystemService(AudioManager::class.java)
        if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) return false

        val nm = context.getSystemService(NotificationManager::class.java)
        val filter = nm.currentInterruptionFilter
        return filter <= NotificationManager.INTERRUPTION_FILTER_PRIORITY
    }
}