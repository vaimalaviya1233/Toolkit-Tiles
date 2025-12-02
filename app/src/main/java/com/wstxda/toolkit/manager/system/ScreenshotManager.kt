package com.wstxda.toolkit.manager.system

import android.content.Context
import com.wstxda.toolkit.permissions.PermissionManager

class ScreenshotManager(context: Context) {

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)

    fun isPermissionGranted(): Boolean {
        return permissionManager.isAccessibilityServiceEnabled()
    }
}