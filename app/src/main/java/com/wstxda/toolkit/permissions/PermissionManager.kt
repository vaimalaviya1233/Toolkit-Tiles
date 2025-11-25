package com.wstxda.toolkit.permissions

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import com.wstxda.toolkit.services.accessibility.TileAccessibilityService

object PermissionManager {

    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, TileAccessibilityService::class.java)

        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }
}