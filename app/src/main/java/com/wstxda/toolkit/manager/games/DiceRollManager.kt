package com.wstxda.toolkit.manager.games

import kotlin.random.Random

object DiceRollManager {

    fun roll(): Int {
        return Random.nextInt(1, 7)
    }
}