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

    private lateinit var sosLabelProvider: SosLabelProvider
    private lateinit var sosIconProvider: SosIconProvider
    private lateinit var sosHaptics: Haptics

    override fun onCreate() {
        super.onCreate()
        SosManager.init(this)
        sosLabelProvider = SosLabelProvider(this)
        sosIconProvider = SosIconProvider(this)
        sosHaptics = Haptics(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        SosManager.isActive.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onClick() {
        sosHaptics.tick()

        if (!SosManager.hasFlash()) {
            Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_SHORT).show()
            return
        }

        SosManager.toggle()
    }

    override fun updateTile() {
        val active = SosManager.isActive.value
        val available = SosManager.hasFlash()

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