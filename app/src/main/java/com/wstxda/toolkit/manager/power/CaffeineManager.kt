package com.wstxda.toolkit.manager.power

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.core.content.edit
import com.wstxda.toolkit.tiles.power.CaffeineTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object CaffeineManager {

    private const val PREF_NAME = "caffeine_prefs"
    private const val PREF_KEY_ORIGINAL = "original_timeout"
    private const val PREF_KEY_EXPECTED = "expected_timeout"
    private const val DEFAULT_TIMEOUT = 60000

    private val stateCycle = listOf(
        CaffeineState.Off,
        CaffeineState.FiveMinutes,
        CaffeineState.TenMinutes,
        CaffeineState.ThirtyMinutes,
        CaffeineState.OneHour,
        CaffeineState.Infinite
    )

    private val _currentState = MutableStateFlow<CaffeineState>(CaffeineState.Off)
    val currentState = _currentState.asStateFlow()

    private var isReceiverRegistered = false
    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                forceReset(context)
            }
        }
    }

    fun synchronizeState(context: Context) {
        val prefs = getPrefs(context)
        val expectedTimeout = prefs.getInt(PREF_KEY_EXPECTED, -1)

        if (expectedTimeout != -1) {
            val systemTimeout = getSystemTimeout(context)
            if (systemTimeout != expectedTimeout) {
                forceReset(context)
            } else {
                val restoredState =
                    stateCycle.find { it.timeout == expectedTimeout } ?: CaffeineState.Off
                _currentState.value = restoredState
                toggleReceiver(context, true)
            }
        } else {
            _currentState.value = CaffeineState.Off
            toggleReceiver(context, false)
        }
    }

    fun isPermissionGranted(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun cycleState(context: Context) {
        if (!isPermissionGranted(context)) return

        val currentIndex = stateCycle.indexOf(_currentState.value)
        val nextIndex = (currentIndex + 1) % stateCycle.size
        val nextState = stateCycle[nextIndex]

        applyState(context, nextState)
    }

    private fun applyState(context: Context, newState: CaffeineState) {
        if (newState == CaffeineState.Off) {
            restoreOriginalTimeout(context)
        } else {
            if (_currentState.value == CaffeineState.Off) {
                saveOriginalTimeout(context)
            }

            if (setSystemTimeout(context, newState.timeout)) {
                _currentState.value = newState
                getPrefs(context).edit { putInt(PREF_KEY_EXPECTED, newState.timeout) }
                toggleReceiver(context, true)
            } else {
                forceReset(context)
            }
        }
        requestTileUpdate(context)
    }

    private fun saveOriginalTimeout(context: Context) {
        val current = getSystemTimeout(context)
        if (stateCycle.any { it.timeout == current && it != CaffeineState.Off }) {
            return
        }
        getPrefs(context).edit { putInt(PREF_KEY_ORIGINAL, current) }
    }

    private fun restoreOriginalTimeout(context: Context) {
        val original = getPrefs(context).getInt(PREF_KEY_ORIGINAL, DEFAULT_TIMEOUT)
        setSystemTimeout(context, original)
        forceReset(context)
    }

    private fun forceReset(context: Context) {
        _currentState.value = CaffeineState.Off
        getPrefs(context).edit {
            remove(PREF_KEY_EXPECTED)
        }
        toggleReceiver(context, false)
        requestTileUpdate(context)
    }

    private fun toggleReceiver(context: Context, enable: Boolean) {
        if (enable && !isReceiverRegistered) {
            context.applicationContext.registerReceiver(
                screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF)
            )
            isReceiverRegistered = true
        } else if (!enable && isReceiverRegistered) {
            try {
                context.applicationContext.unregisterReceiver(screenOffReceiver)
            } catch (_: Exception) {
            }
            isReceiverRegistered = false
        }
    }

    private fun getSystemTimeout(context: Context): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT)
        } catch (_: Exception) {
            DEFAULT_TIMEOUT
        }
    }

    private fun setSystemTimeout(context: Context, timeout: Int): Boolean {
        return try {
            Settings.System.putInt(
                context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, timeout
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun requestTileUpdate(context: Context) {
        TileService.requestListeningState(
            context, ComponentName(context, CaffeineTileService::class.java)
        )
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}