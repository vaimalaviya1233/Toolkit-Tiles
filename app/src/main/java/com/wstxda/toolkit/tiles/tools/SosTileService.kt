package com.wstxda.toolkit.tiles.tools

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.tools.SosManager
import com.wstxda.toolkit.ui.icon.SosIconProvider
import com.wstxda.toolkit.ui.label.SosLabelProvider
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SosTileService : BaseTileService() {

    private val sosManager by lazy { SosManager(applicationContext) }
    private val sosLabelProvider by lazy { SosLabelProvider(applicationContext) }
    private val sosIconProvider by lazy { SosIconProvider(applicationContext) }
    private val sosHaptics by lazy { Haptics(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        sosManager.isActive.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        sosManager.cleanup()
    }

    override fun onClick() {
        sosHaptics.tick()

        if (!sosManager.hasFlash()) {
            Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show()
            return
        }

        sosManager.toggle()
    }

    override fun updateTile() {
        val active = sosManager.isActive.value
        val available = sosManager.hasFlash()

        setTileState(
            state = when {
                !available -> Tile.STATE_UNAVAILABLE
                active -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = sosLabelProvider.getLabel(),
            subtitle = sosLabelProvider.getSubtitle(active, available),
            icon = sosIconProvider.getIcon()
        )
    }
}