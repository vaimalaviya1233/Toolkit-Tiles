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

    private val diceRollManager by lazy { DiceRollManager() }
    private val diceRollLabelProvider by lazy { DiceRollLabelProvider(applicationContext) }
    private val diceRollIconProvider by lazy { DiceRollIconProvider(applicationContext) }
    private val diceRollHaptics by lazy { Haptics(applicationContext) }

    private var animationJob: Job? = null
    private var currentRoll: Int? = null
    private var isRolling = false

    override fun onStopListening() {
        super.onStopListening()
        cancelAnimation()
        currentRoll = null
        updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelAnimation()
    }

    private fun cancelAnimation() {
        animationJob?.cancel()
        animationJob = null
        isRolling = false
    }

    override fun onClick() {
        if (isRolling) return
        animationJob?.cancel()
        animationJob = serviceScope.launch {
            isRolling = true
            val finalRoll = diceRollManager.roll()

            for (i in 0 until 12) {
                currentRoll = diceRollManager.roll()
                diceRollHaptics.tick()
                updateTile()

                delay(60L + (i * 30))
            }

            currentRoll = finalRoll
            isRolling = false
            diceRollHaptics.tick()
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