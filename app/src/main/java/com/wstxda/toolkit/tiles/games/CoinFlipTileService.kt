package com.wstxda.toolkit.tiles.games

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.games.CoinFlipManager
import com.wstxda.toolkit.manager.games.CoinFlipSide
import com.wstxda.toolkit.ui.icon.CoinFlipIconProvider
import com.wstxda.toolkit.ui.label.CoinFlipLabelProvider

class CoinFlipTileService : BaseTileService() {

    private val coinFlipManager by lazy { CoinFlipManager() }
    private val coinFlipLabelProvider by lazy { CoinFlipLabelProvider(applicationContext) }
    private val coinFlipIconProvider by lazy { CoinFlipIconProvider(applicationContext) }

    private var lastFlip: CoinFlipSide? = null

    override fun onStopListening() {
        super.onStopListening()
        lastFlip = null
        coinFlipManager.reset()
        updateTile()
    }

    override fun onClick() {
        lastFlip = coinFlipManager.flip()
        updateTile()
    }

    override fun updateTile() {
        val heads = coinFlipManager.headsCount
        val tails = coinFlipManager.tailsCount

        setTileState(
            state = if (lastFlip != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = coinFlipLabelProvider.getLabel(lastFlip),
            subtitle = coinFlipLabelProvider.getSubtitle(lastFlip, heads, tails),
            icon = coinFlipIconProvider.getIcon(lastFlip)
        )
    }
}