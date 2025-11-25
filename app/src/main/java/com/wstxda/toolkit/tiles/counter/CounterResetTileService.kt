package com.wstxda.toolkit.tiles.counter

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.counter.CounterManager
import com.wstxda.toolkit.ui.icon.CounterIconProvider
import com.wstxda.toolkit.ui.label.CounterLabelProvider
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class CounterResetTileService : BaseTileService() {

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
        CounterManager.count.onEach { updateTile() }.launchIn(serviceScope)
    }

    override fun onClick() {
        CounterManager.reset()
    }

    override fun updateTile() {
        setTileState(
            state = Tile.STATE_INACTIVE,
            label = counterLabelProvider.getResetLabel(),
            subtitle = counterLabelProvider.getResetSubtitle(),
            icon = counterIconProvider.getResetIcon()
        )
    }
}