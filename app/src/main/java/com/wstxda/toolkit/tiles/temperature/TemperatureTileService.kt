package com.wstxda.toolkit.tiles.temperature

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.temperature.TemperatureManager
import com.wstxda.toolkit.manager.temperature.TemperatureModule
import com.wstxda.toolkit.ui.icon.TemperatureIconProvider
import com.wstxda.toolkit.ui.label.TemperatureLabelProvider
import kotlinx.coroutines.flow.Flow

class TemperatureTileService : BaseTileService() {

    private val temperatureManager: TemperatureManager by lazy { TemperatureModule.getInstance(applicationContext) }
    private val temperatureLabelProvider by lazy { TemperatureLabelProvider(applicationContext) }
    private val temperatureIconProvider by lazy { TemperatureIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        temperatureManager.setListening(true)
    }

    override fun onStopListening() {
        super.onStopListening()
        temperatureManager.setListening(false)
    }

    override fun onClick() {
        temperatureManager.toggle()
    }

    override fun flowsToCollect(): List<Flow<*>> {
        return listOf(
            temperatureManager.isActive, temperatureManager.temperature
        )
    }

    override fun updateTile() {
        val isActive = temperatureManager.isActive.value
        val temp = temperatureManager.temperature.value

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = temperatureLabelProvider.getLabel(isActive, temp),
            subtitle = temperatureLabelProvider.getSubtitle(isActive),
            icon = temperatureIconProvider.getIcon(isActive)
        )
    }
}