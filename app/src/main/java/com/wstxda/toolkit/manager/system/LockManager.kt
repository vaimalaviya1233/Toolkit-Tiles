package com.wstxda.toolkit.manager.system

import android.content.Context
import android.content.Intent
import android.os.Build
import com.wstxda.toolkit.permissions.PermissionManager
import com.wstxda.toolkit.services.accessibility.TileAccessibilityAction
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService

class LockManager(context: Context) {

    private val appContext = context.applicationContext
    private val permissionManager = PermissionManager(appContext)

    fun isPermissionGranted(): Boolean {
        return permissionManager.isAccessibilityServiceEnabled()
    }

    fun lockScreen() {
        if (!isPermissionGranted()) return

        val intent = Intent(appContext, TileAccessibilityService::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                putExtra(TileAccessibilityService.ACTION_KEY, TileAccessibilityAction.LOCK_SCREEN)
            }
        }

        appContext.startService(intent)
    }
}