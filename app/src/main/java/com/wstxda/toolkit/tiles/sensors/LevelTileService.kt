package com.wstxda.toolkit.tiles.sensors

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sensors.LevelManager
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.LevelIconProvider
import com.wstxda.toolkit.ui.label.LevelLabelProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val START_FOREGROUND_IMMEDIATELY =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class LevelTileService : BaseTileService() {

    private lateinit var levelLabelProvider: LevelLabelProvider
    private lateinit var levelIconProvider: LevelIconProvider

    override fun onCreate() {
        super.onCreate()
        LevelManager.initialize(this)
        levelLabelProvider = LevelLabelProvider(this)
        levelIconProvider = LevelIconProvider(this)

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
            LevelManager.setForceActive(true)
            LevelManager.resume()
            startLevel()
        } else {
            LevelManager.setForceActive(false)
        }

        combine(
            LevelManager.isActive, LevelManager.degrees, LevelManager.orientation
        ) { active, deg, orient ->
            Triple(active, deg, orient)
        }.onEach { (active, degrees, orientation) ->
            updateTile(active, degrees, orientation)
        }.launchIn(serviceScope)
    }

    override fun onClick() {
        if (!LevelManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        val active = qsTile?.state == Tile.STATE_ACTIVE
        if (active) {
            updateTileAsInactive()
        } else {
            updateTileAsActive()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        if (qsTile?.state == Tile.STATE_ACTIVE) {
            LevelManager.pause()
            if (!START_FOREGROUND_IMMEDIATELY) {
                stopForeground(STOP_FOREGROUND_DETACH)
            }
        }
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun updateTile() {
        val isActive = LevelManager.isActive.value
        val degrees = LevelManager.degrees.value
        val orient = LevelManager.orientation.value
        updateTile(isActive, degrees, orient)
    }

    private fun updateTile(
        isActive: Boolean,
        degrees: Int,
        orientation: com.wstxda.toolkit.services.sensors.Orientation
    ) {
        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = levelLabelProvider.getLabel(isActive, degrees),
            subtitle = levelLabelProvider.getSubtitle(isActive),
            icon = levelIconProvider.getIcon(isActive, degrees, orientation)
        )
    }

    private fun updateTileAsActive() {
        try {
            if (!START_FOREGROUND_IMMEDIATELY) {
                startForegroundCompat(NOTIFICATION_ID, notification())
            }

            LevelManager.start()
            qsTile.state = Tile.STATE_ACTIVE
            updateTile()

        } catch (e: Exception) {
            if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                LevelManager.stop()
                qsTile.state = Tile.STATE_INACTIVE
                updateTile()
            } else {
                throw e
            }
        }
    }

    private fun updateTileAsInactive() {
        qsTile.state = Tile.STATE_INACTIVE
        updateTile()
        stopLevelAndRemoveNotification()
        LevelManager.stop()
    }

    private fun startLevel() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            try {
                startForegroundCompat(NOTIFICATION_ID, notification())
            } catch (_: Exception) {
            }
        }
    }

    private fun stopLevelAndRemoveNotification() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }
}