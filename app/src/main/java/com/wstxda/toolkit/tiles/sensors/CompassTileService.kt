package com.wstxda.toolkit.tiles.sensors

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sensors.CompassManager
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.CompassIconProvider
import com.wstxda.toolkit.ui.label.CompassLabelProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

private val START_FOREGROUND_IMMEDIATELY =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class CompassTileService : BaseTileService() {

    private lateinit var compassLabelProvider: CompassLabelProvider
    private lateinit var compassIconProvider: CompassIconProvider

    override fun onCreate() {
        super.onCreate()
        CompassManager.initialize(this)
        compassLabelProvider = CompassLabelProvider(this)
        compassIconProvider = CompassIconProvider(this)

        getSystemService(android.app.NotificationManager::class.java)?.createNotificationChannel(
            channel()
        )

        if (START_FOREGROUND_IMMEDIATELY) {
            startForegroundCompat(NOTIFICATION_ID, notification())
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        if (qsTile?.state == Tile.STATE_ACTIVE) {
            startCompass()
        }

        combine(CompassManager.isActive, CompassManager.currentDegrees) { _, _ ->
            updateTile()
        }.launchIn(serviceScope)
    }

    override fun onClick() {
        if (!CompassManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        when (qsTile?.state) {
            Tile.STATE_ACTIVE -> updateTileAsInactive()
            else -> updateTileAsActive()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        if (qsTile?.state == Tile.STATE_ACTIVE) {
            stopCompass()
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun updateTile() {
        val isActive = CompassManager.isActive.value
        val degrees = CompassManager.currentDegrees.value
        val state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

        setTileState(
            state = state,
            label = compassLabelProvider.getLabel(isActive, degrees),
            subtitle = compassLabelProvider.getSubtitle(isActive),
            icon = compassIconProvider.getIcon(isActive, degrees)
        )
    }

    private fun updateTileAsActive() {
        val degrees = CompassManager.currentDegrees.value
        setTileState(
            state = Tile.STATE_ACTIVE,
            label = compassLabelProvider.getLabel(true, degrees),
            subtitle = compassLabelProvider.getSubtitle(true),
            icon = compassIconProvider.getIcon(true, degrees)
        )
        startCompass()
    }

    private fun updateTileAsInactive() {
        setTileState(
            state = Tile.STATE_INACTIVE,
            label = compassLabelProvider.getLabel(false, 0f),
            subtitle = compassLabelProvider.getSubtitle(false),
            icon = compassIconProvider.getIcon(false, 0f)
        )
        stopCompassAndRemoveNotification()
    }

    private fun startCompass() {
        try {
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }

            CompassManager.start()

        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                updateTileAsInactive()
            } else {
                throw e
            }
        }
    }

    private fun stopCompass() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }

        CompassManager.pause()
    }

    private fun stopCompassAndRemoveNotification() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        CompassManager.stop()
    }
}