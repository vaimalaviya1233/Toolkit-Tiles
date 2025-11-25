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

    private lateinit var caffeineLabelProvider: CaffeineLabelProvider
    private lateinit var caffeineIconProvider: CaffeineIconProvider

    override fun onCreate() {
        super.onCreate()
        caffeineLabelProvider = CaffeineLabelProvider(this)
        caffeineIconProvider = CaffeineIconProvider(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        CaffeineManager.synchronizeState(this)

        CaffeineManager.currentState.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onClick() {
        if (CaffeineManager.isPermissionGranted(this)) {
            CaffeineManager.cycleState(this)
        } else {
            startActivityAndCollapse(WriteSettingsPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val state = CaffeineManager.currentState.value
        val hasPermission = CaffeineManager.isPermissionGranted(this)

        setTileState(
            state = if (state != CaffeineState.Off && hasPermission) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = caffeineLabelProvider.getLabel(state, hasPermission),
            subtitle = caffeineLabelProvider.getSubtitle(state, hasPermission),
            icon = caffeineIconProvider.getIcon(state)
        )
    }
}