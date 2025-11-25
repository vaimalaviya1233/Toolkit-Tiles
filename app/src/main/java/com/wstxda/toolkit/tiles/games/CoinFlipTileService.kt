package com.wstxda.toolkit.tiles.games

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.games.CoinFlipManager
import com.wstxda.toolkit.manager.games.CoinFlipSide
import com.wstxda.toolkit.ui.icon.CoinFlipIconProvider
import com.wstxda.toolkit.ui.label.CoinFlipLabelProvider

class CoinFlipTileService : BaseTileService() {

    private lateinit var coinFlipLabelProvider: CoinFlipLabelProvider
    private lateinit var coinFlipIconProvider: CoinFlipIconProvider
    private var lastFlip: CoinFlipSide? = null

    override fun onCreate() {
        super.onCreate()
        coinFlipLabelProvider = CoinFlipLabelProvider(this)
        coinFlipIconProvider = CoinFlipIconProvider(this)
    }

    override fun onStopListening() {
        super.onStopListening()
        lastFlip = null
        CoinFlipManager.reset()
        updateTile()
    }

    override fun onClick() {
        lastFlip = CoinFlipManager.flip()
        updateTile()
    }

    override fun updateTile() {
        val heads = CoinFlipManager.headsCount
        val tails = CoinFlipManager.tailsCount

        setTileState(
            state = if (lastFlip != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = coinFlipLabelProvider.getLabel(lastFlip),
            subtitle = coinFlipLabelProvider.getSubtitle(lastFlip, heads, tails),
            icon = coinFlipIconProvider.getIcon(lastFlip)
        )
    }
}