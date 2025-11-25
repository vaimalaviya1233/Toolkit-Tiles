package com.wstxda.toolkit.tiles.counter

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.counter.CounterAction
import com.wstxda.toolkit.manager.counter.CounterManager
import com.wstxda.toolkit.ui.icon.CounterIconProvider
import com.wstxda.toolkit.ui.label.CounterLabelProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class CounterAddTileService : BaseTileService() {

    private lateinit var counterLabelProvider: CounterLabelProvider
    private lateinit var counterIconProvider: CounterIconProvider

    override fun onCreate() {
        super.onCreate()
        CounterManager.initialize(this)
        counterLabelProvider = CounterLabelProvider(this)
        counterIconProvider = CounterIconProvider(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        combine(CounterManager.count, CounterManager.lastAction) { _, _ ->
            updateTile()
        }.launchIn(serviceScope)
    }

    override fun onClick() {
        CounterManager.increment()
    }

    override fun updateTile() {
        val count = CounterManager.count.value
        val action = CounterManager.lastAction.value
        val isActive = action == CounterAction.ADD

        setTileState(
            state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = counterLabelProvider.getAddLabel(isActive, count),
            subtitle = counterLabelProvider.getAddSubtitle(isActive),
            icon = counterIconProvider.getAddIcon()
        )
    }
}