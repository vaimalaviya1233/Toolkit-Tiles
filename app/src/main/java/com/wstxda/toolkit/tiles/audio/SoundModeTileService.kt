package com.wstxda.toolkit.tiles.audio

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.NotificationPolicyPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.audio.SoundMode
import com.wstxda.toolkit.manager.audio.SoundModeManager
import com.wstxda.toolkit.ui.icon.SoundModeIconProvider
import com.wstxda.toolkit.ui.label.SoundModeLabelProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SoundModeTileService : BaseTileService() {

    private val soundModeManager by lazy { SoundModeManager(applicationContext) }
    private val soundModeLabelProvider by lazy { SoundModeLabelProvider(applicationContext) }
    private val soundModeIconProvider by lazy { SoundModeIconProvider(applicationContext) }
    private var listeningJob: Job? = null
    private var currentMode: SoundMode = SoundMode.NORMAL

    override fun onStartListening() {
        super.onStartListening()
        listeningJob = soundModeManager.currentModeFlow
            .onEach { mode ->
                currentMode = mode
                updateTile()
            }
            .launchIn(serviceScope)
    }

    override fun onStopListening() {
        super.onStopListening()
        listeningJob?.cancel()
        listeningJob = null
    }

    override fun onClick() {
        if (soundModeManager.hasPermission()) {
            soundModeManager.cycleMode()
        } else {
            startActivityAndCollapse(NotificationPolicyPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val hasPermission = soundModeManager.hasPermission()
        val mode = if (listeningJob == null) soundModeManager.getCurrentModeInternal() else currentMode

        setTileState(
            state = if (hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = soundModeLabelProvider.getLabel(mode, hasPermission),
            subtitle = soundModeLabelProvider.getSubtitle(hasPermission),
            icon = soundModeIconProvider.getIcon(mode, hasPermission)
        )
    }
}