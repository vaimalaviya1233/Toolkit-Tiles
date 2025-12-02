package com.wstxda.toolkit.manager.games

import kotlin.random.Random

class CoinFlipManager() {

    var headsCount = 0
        private set
    var tailsCount = 0
        private set

    fun flip(): CoinFlipSide {
        val side = if (Random.nextBoolean()) CoinFlipSide.HEADS else CoinFlipSide.TAILS
        if (side == CoinFlipSide.HEADS) {
            headsCount++
        } else {
            tailsCount++
        }
        return side
    }

    fun reset() {
        headsCount = 0
        tailsCount = 0
    }
}