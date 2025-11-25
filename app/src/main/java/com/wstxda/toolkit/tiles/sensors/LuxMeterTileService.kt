package com.wstxda.toolkit.tiles.sensors

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.sensors.LuxMeterManager
import com.wstxda.toolkit.services.foreground.NOTIFICATION_ID
import com.wstxda.toolkit.services.foreground.channel
import com.wstxda.toolkit.services.foreground.notification
import com.wstxda.toolkit.services.foreground.startForegroundCompat
import com.wstxda.toolkit.ui.icon.LuxMeterIconProvider
import com.wstxda.toolkit.ui.label.LuxMeterLabelProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

private val START_FOREGROUND_IMMEDIATELY =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.UPSIDE_DOWN_CAKE
private val CAN_ONLY_START_FOREGROUND_ON_CLICK =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

class LuxMeterTileService : BaseTileService() {

    private lateinit var luxMeterLabelProvider: LuxMeterLabelProvider
    private lateinit var luxMeterIconProvider: LuxMeterIconProvider

    override fun onCreate() {
        super.onCreate()
        LuxMeterManager.initialize(this)
        luxMeterLabelProvider = LuxMeterLabelProvider(this)
        luxMeterIconProvider = LuxMeterIconProvider(this)

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
            LuxMeterManager.setForceActive(true)
            LuxMeterManager.resume()
            startLux()
        } else {
            LuxMeterManager.setForceActive(false)
        }

        combine(LuxMeterManager.isActive, LuxMeterManager.lux) { _, _ ->
            updateTile()
        }.launchIn(serviceScope)
    }

    override fun onClick() {
        if (!LuxMeterManager.isSupported(this)) {
            Toast.makeText(this, R.string.not_supported, Toast.LENGTH_LONG).show()
            return
        }

        val isActive = qsTile?.state == Tile.STATE_ACTIVE

        if (isActive) {
            LuxMeterManager.stop()
            stopLuxAndRemoveNotification()
            qsTile.state = Tile.STATE_INACTIVE
            updateTile()
        } else {
            try {
                if (!START_FOREGROUND_IMMEDIATELY) {
                    startForegroundCompat(NOTIFICATION_ID, notification())
                }
                LuxMeterManager.start()
                qsTile.state = Tile.STATE_ACTIVE
                updateTile()

            } catch (e: Exception) {
                if (CAN_ONLY_START_FOREGROUND_ON_CLICK && e is ForegroundServiceStartNotAllowedException) {
                    LuxMeterManager.stop()
                    qsTile.state = Tile.STATE_INACTIVE
                    updateTile()
                } else {
                    throw e
                }
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        if (qsTile?.state == Tile.STATE_ACTIVE) {
            LuxMeterManager.pause()

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
        val isActive = LuxMeterManager.isActive.value
        val lux = LuxMeterManager.lux.value

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = luxMeterLabelProvider.getLabel(isActive, lux),
            subtitle = luxMeterLabelProvider.getSubtitle(isActive),
            icon = luxMeterIconProvider.getIcon(isActive)
        )
    }

    private fun startLux() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            try {
                startForegroundCompat(NOTIFICATION_ID, notification())
            } catch (_: Exception) {
            }
        }
    }

    private fun stopLuxAndRemoveNotification() {
        if (!START_FOREGROUND_IMMEDIATELY) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }
}