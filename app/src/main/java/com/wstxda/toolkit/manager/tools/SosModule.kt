package com.wstxda.toolkit.manager.tools

import android.content.Context

object SosModule {
    @Volatile
    private var instance: SosManager? = null

    fun getInstance(context: Context): SosManager {
        return instance ?: synchronized(this) {
            instance ?: SosManager(context.applicationContext).also { instance = it }
        }
    }
}