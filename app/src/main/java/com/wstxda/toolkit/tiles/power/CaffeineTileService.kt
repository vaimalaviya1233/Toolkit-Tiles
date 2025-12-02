package com.wstxda.toolkit.tiles.power

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.WriteSettingsPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.power.CaffeineManager
import com.wstxda.toolkit.manager.power.CaffeineState
import com.wstxda.toolkit.ui.icon.CaffeineIconProvider
import com.wstxda.toolkit.ui.label.CaffeineLabelProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CaffeineTileService : BaseTileService() {

    private val caffeineManager by lazy { CaffeineManager(applicationContext) }
    private val caffeineLabelProvider by lazy { CaffeineLabelProvider(applicationContext) }
    private val caffeineIconProvider by lazy { CaffeineIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        caffeineManager.synchronizeState()

        caffeineManager.currentState.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        caffeineManager.cleanup()
    }

    override fun onClick() {
        if (caffeineManager.isPermissionGranted()) {
            caffeineManager.cycleState()
        } else {
            startActivityAndCollapse(WriteSettingsPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val state = caffeineManager.currentState.value
        val hasPermission = caffeineManager.isPermissionGranted()

        setTileState(
            state = if (state != CaffeineState.Off && hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = caffeineLabelProvider.getLabel(state, hasPermission),
            subtitle = caffeineLabelProvider.getSubtitle(state, hasPermission),
            icon = caffeineIconProvider.getIcon(state)
        )
    }
}