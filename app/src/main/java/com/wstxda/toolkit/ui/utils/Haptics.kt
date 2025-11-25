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

    fun long(duration: Long) {
        performOneShot(duration)
    }

    fun cancel() {
        vibrator.cancel()
    }

    private fun perform(effectId: Int) {
        if (!isAllowed()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val effect = VibrationEffect.createPredefined(effectId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val attrs =
                    VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build()

                vibrator.vibrate(effect, attrs)
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(effect)
            }

        } else {
            val effect = VibrationEffect.createOneShot(
                12, VibrationEffect.DEFAULT_AMPLITUDE
            )
            @Suppress("DEPRECATION") vibrator.vibrate(effect)
        }
    }

    private fun performOneShot(duration: Long) {
        if (!isAllowed()) return

        val effect = VibrationEffect.createOneShot(
            duration, VibrationEffect.DEFAULT_AMPLITUDE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val attrs =
                VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_ALARM).build()
            vibrator.vibrate(effect, attrs)
        } else {
            @Suppress("DEPRECATION") vibrator.vibrate(effect)
        }
    }

    private fun getEffectTick(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) VibrationEffect.EFFECT_TICK
        else 2

    private fun isAllowed(): Boolean {
        if (!vibrator.hasVibrator()) return false


        val am = context.getSystemService(AudioManager::class.java)
        if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) return false

        val nm = context.getSystemService(NotificationManager::class.java)
        val filter = nm.currentInterruptionFilter
        return filter <= NotificationManager.INTERRUPTION_FILTER_PRIORITY
    }
}