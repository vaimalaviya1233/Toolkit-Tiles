package com.wstxda.toolkit.tiles.games

import android.service.quicksettings.Tile
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.games.DiceRollManager
import com.wstxda.toolkit.ui.icon.DiceRollIconProvider
import com.wstxda.toolkit.ui.label.DiceRollLabelProvider
import com.wstxda.toolkit.ui.utils.Haptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiceRollTileService : BaseTileService() {

    private lateinit var diceRollLabelProvider: DiceRollLabelProvider
    private lateinit var diceRollIconProvider: DiceRollIconProvider
    private lateinit var haptics: Haptics
    private var animationJob: Job? = null
    private var currentRoll: Int? = null
    private var isRolling = false

    override fun onCreate() {
        super.onCreate()
        diceRollLabelProvider = DiceRollLabelProvider(this)
        diceRollIconProvider = DiceRollIconProvider(this)
        haptics = Haptics(this)
    }

    override fun onStopListening() {
        super.onStopListening()
        animationJob?.cancel()
        currentRoll = null
        isRolling = false
        updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        animationJob?.cancel()
    }

    override fun onClick() {
        if (isRolling) return

        animationJob?.cancel()
        animationJob = serviceScope.launch {
            isRolling = true
            val finalRoll = DiceRollManager.roll()

            for (i in 0 until 12) {
                val roll = DiceRollManager.roll()

                currentRoll = roll
                haptics.tick()
                updateTile()

                delay(60L + (i * 30))
            }

            currentRoll = finalRoll
            isRolling = false
            haptics.tick()
            updateTile()
        }
    }

    override fun updateTile() {
        setTileState(
            state = if (currentRoll != null) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE,
            label = diceRollLabelProvider.getLabel(currentRoll),
            subtitle = diceRollLabelProvider.getSubtitle(isRolling),
            icon = diceRollIconProvider.getIcon(currentRoll)
        )
    }
}