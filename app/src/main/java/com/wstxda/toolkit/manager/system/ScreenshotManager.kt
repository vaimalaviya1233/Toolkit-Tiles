package com.wstxda.toolkit.manager.system

import android.content.Context
import com.wstxda.toolkit.permissions.PermissionManager

object ScreenshotManager {

    fun isPermissionGranted(context: Context): Boolean {
        return PermissionManager.isAccessibilityServiceEnabled(context)
    }
}