package com.wstxda.toolkit.manager.audio

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SoundModeManager {

    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager

    private val _currentMode = MutableStateFlow(SoundMode.NORMAL)
    val currentMode = _currentMode.asStateFlow()

    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                _currentMode.value = getCurrentModeInternal()
            }
        }
    }

    fun init(context: Context) {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        _currentMode.value = getCurrentModeInternal()

        context.registerReceiver(
            ringerModeReceiver,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else 0
        )
    }

    fun hasPermission(): Boolean {
        return if (::notificationManager.isInitialized) {
            notificationManager.isNotificationPolicyAccessGranted
        } else false
    }

    fun cycleMode() {
        if (!hasPermission()) return

        val newMode = when (_currentMode.value) {
            SoundMode.NORMAL -> SoundMode.VIBRATE
            SoundMode.VIBRATE -> SoundMode.SILENT
            SoundMode.SILENT -> SoundMode.NORMAL
        }
        audioManager.ringerMode = newMode.ringerMode
    }

    private fun getCurrentModeInternal(): SoundMode {
        if (!::audioManager.isInitialized) return SoundMode.NORMAL
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> SoundMode.VIBRATE
            AudioManager.RINGER_MODE_SILENT -> SoundMode.SILENT
            else -> SoundMode.NORMAL
        }
    }

    fun unregisterReceiver(context: Context) {
        try {
            context.unregisterReceiver(ringerModeReceiver)
        } catch (_: IllegalArgumentException) {
        }
    }
}