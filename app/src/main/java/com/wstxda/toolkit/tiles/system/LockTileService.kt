package com.wstxda.toolkit.tiles.system

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.system.LockManager
import com.wstxda.toolkit.ui.icon.LockIconProvider
import com.wstxda.toolkit.ui.label.LockLabelProvider

class LockTileService : BaseTileService() {

    private lateinit var labelProvider: LockLabelProvider
    private lateinit var iconProvider: LockIconProvider

    override fun onCreate() {
        super.onCreate()
        labelProvider = LockLabelProvider(this)
        iconProvider = LockIconProvider(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        if (LockManager.isPermissionGranted(this)) {
            LockManager.lockScreen(this)
        } else {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val hasPermission = LockManager.isPermissionGranted(this)

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = labelProvider.getLabel(),
            subtitle = labelProvider.getSubtitle(hasPermission),
            icon = iconProvider.getIcon()
        )
    }
}