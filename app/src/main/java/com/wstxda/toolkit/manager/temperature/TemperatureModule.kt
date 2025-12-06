package com.wstxda.toolkit.manager.temperature

import android.content.Context

object TemperatureModule {
    @Volatile
    private var instance: TemperatureManager? = null

    fun getInstance(context: Context): TemperatureManager {
        return instance ?: synchronized(this) {
            instance ?: TemperatureManager(context.applicationContext).also { instance = it }
        }
    }
}