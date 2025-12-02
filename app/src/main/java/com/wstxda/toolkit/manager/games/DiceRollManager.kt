package com.wstxda.toolkit.manager.games

import kotlin.random.Random

class DiceRollManager() {

    fun roll(): Int {
        return Random.nextInt(1, 7)
    }
}