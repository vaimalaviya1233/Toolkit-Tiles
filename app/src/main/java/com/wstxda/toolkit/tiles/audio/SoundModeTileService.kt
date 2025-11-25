package com.wstxda.toolkit.tiles.audio

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.NotificationPolicyPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.audio.SoundModeManager
import com.wstxda.toolkit.ui.icon.SoundModeIconProvider
import com.wstxda.toolkit.ui.label.SoundModeLabelProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SoundModeTileService : BaseTileService() {

    private lateinit var soundModeLabelProvider: SoundModeLabelProvider
    private lateinit var soundModeIconProvider: SoundModeIconProvider

    override fun onCreate() {
        super.onCreate()
        SoundModeManager.init(this)
        soundModeLabelProvider = SoundModeLabelProvider(this)
        soundModeIconProvider = SoundModeIconProvider(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        SoundModeManager.currentMode.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        SoundModeManager.unregisterReceiver(this)
    }

    override fun onClick() {
        if (SoundModeManager.hasPermission()) {
            SoundModeManager.cycleMode()
        } else {
            startActivityAndCollapse(NotificationPolicyPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val mode = SoundModeManager.currentMode.value
        val hasPermission = SoundModeManager.hasPermission()

        setTileState(
            state = if (hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = soundModeLabelProvider.getLabel(mode, hasPermission),
            subtitle = soundModeLabelProvider.getSubtitle(hasPermission),
            icon = soundModeIconProvider.getIcon(mode, hasPermission)
        )
    }
}