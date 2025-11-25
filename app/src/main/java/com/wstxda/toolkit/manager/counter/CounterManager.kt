package com.wstxda.toolkit.manager.counter

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import androidx.core.content.edit
import com.wstxda.toolkit.tiles.counter.CounterAddTileService
import com.wstxda.toolkit.tiles.counter.CounterRemoveTileService
import com.wstxda.toolkit.tiles.counter.CounterResetTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object CounterManager {

    private const val PREFS_NAME = "counter_prefs"
    private const val KEY_COUNT = "count"
    private const val KEY_ACTION = "last_action"

    private lateinit var appContext: Context

    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private val _lastAction = MutableStateFlow(CounterAction.NONE)
    val lastAction = _lastAction.asStateFlow()

    fun initialize(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext

        val prefs = getPrefs()
        _count.value = prefs.getInt(KEY_COUNT, 0)

        val actionString = prefs.getString(KEY_ACTION, CounterAction.NONE.name)
        _lastAction.value =
            runCatching { CounterAction.valueOf(actionString!!) }.getOrDefault(CounterAction.NONE)
    }

    fun increment() {
        updateState(_count.value + 1, CounterAction.ADD)
        refreshAllTiles()
    }

    fun decrement() {
        updateState(_count.value - 1, CounterAction.REMOVE)
        refreshAllTiles()
    }

    fun reset() {
        updateState(0, CounterAction.RESET)
        refreshAllTiles()
    }

    private fun updateState(newValue: Int, action: CounterAction) {
        _count.value = newValue
        _lastAction.value = action

        getPrefs().edit {
            putInt(KEY_COUNT, newValue)
            putString(KEY_ACTION, action.name)
        }
    }

    private fun refreshAllTiles() {
        if (!::appContext.isInitialized) return

        val classes = listOf(
            CounterAddTileService::class.java,
            CounterRemoveTileService::class.java,
            CounterResetTileService::class.java
        )

        classes.forEach { clazz ->
            TileService.requestListeningState(appContext, ComponentName(appContext, clazz))
        }
    }

    private fun getPrefs() = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}