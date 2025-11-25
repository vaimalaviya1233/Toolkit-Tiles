package com.wstxda.toolkit.manager.system

import android.content.Context
import android.content.Intent
import android.os.Build
import com.wstxda.toolkit.permissions.PermissionManager
import com.wstxda.toolkit.services.accessibility.TileAccessibilityAction
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService

object LockManager {

    fun isPermissionGranted(context: Context): Boolean {
        return PermissionManager.isAccessibilityServiceEnabled(context)
    }

    fun lockScreen(context: Context) {
        if (!isPermissionGranted(context)) return

        val intent = Intent(context, TileAccessibilityService::class.java).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                putExtra(TileAccessibilityService.ACTION_KEY, TileAccessibilityAction.LOCK_SCREEN)
            }
        }
        context.startService(intent)
    }
}