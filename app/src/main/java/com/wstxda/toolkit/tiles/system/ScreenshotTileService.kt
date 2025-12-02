package com.wstxda.toolkit.tiles.system

import android.service.quicksettings.Tile
import com.wstxda.toolkit.activity.AccessibilityPermissionActivity
import com.wstxda.toolkit.activity.ScreenshotActivity
import com.wstxda.toolkit.base.BaseTileService
import com.wstxda.toolkit.manager.system.ScreenshotManager
import com.wstxda.toolkit.ui.icon.ScreenshotIconProvider
import com.wstxda.toolkit.ui.label.ScreenshotLabelProvider

class ScreenshotTileService : BaseTileService() {

    private val screenshotManager by lazy { ScreenshotManager(applicationContext) }
    private val screenshotLabelProvider by lazy { ScreenshotLabelProvider(applicationContext) }
    private val screenshotIconProvider by lazy { ScreenshotIconProvider(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        if (screenshotManager.isPermissionGranted()) {
            startActivityAndCollapse(ScreenshotActivity::class.java)
        } else {
            startActivityAndCollapse(AccessibilityPermissionActivity::class.java)
        }
    }

    override fun updateTile() {
        val hasPermission = screenshotManager.isPermissionGranted()

        setTileState(
            state = Tile.STATE_INACTIVE,
            label = screenshotLabelProvider.getLabel(),
            subtitle = screenshotLabelProvider.getSubtitle(hasPermission),
            icon = screenshotIconProvider.getIcon()
        )
    }
}