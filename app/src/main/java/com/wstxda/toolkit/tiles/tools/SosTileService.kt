package com.wstxda.toolkit.tiles.tools

import android.service.quicksettings.Tile
import android.widget.Toast
import com.wstxda.toolkit.R
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.tools.SosModule
import com.wstxda.toolkit.ui.icon.SosIconProvider
import com.wstxda.toolkit.ui.label.SosLabelProvider
import kotlinx.coroutines.flow.Flow

class SosTileService : BaseTileService() {

    private val sosModule by lazy { SosModule.getInstance(applicationContext) }
    private val sosLabelProvider by lazy { SosLabelProvider(applicationContext) }
    private val sosIconProvider by lazy { SosIconProvider(applicationContext) }

    override fun onClick() {
        if (!sosModule.hasFlashHardware()) {
            Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show()
            return
        }

        if (qsTile?.state == Tile.STATE_UNAVAILABLE) return

        sosModule.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(sosModule.isActive, sosModule.isFlashAvailable)
    }

    override fun updateTile() {
        val active = sosModule.isActive.value
        val isHardwareAvailable = sosModule.hasFlashHardware()
        val isSystemAvailable = sosModule.isFlashAvailable.value
        val isFullyAvailable = isHardwareAvailable && isSystemAvailable

        setTileState(
            state = when {
                !isFullyAvailable -> Tile.STATE_UNAVAILABLE
                active -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            },
            label = sosLabelProvider.getLabel(),
            subtitle = sosLabelProvider.getSubtitle(active, isFullyAvailable),
            icon = sosIconProvider.getIcon()
        )
    }
}