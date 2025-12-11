package com.wstxda.toolkit.manager.mediaoutput

import android.content.Context
import android.content.Intent
import android.media.MediaRouter2
import android.os.Build

object AudioPanelDispatcher {

    fun triggerOutputSwitching(context: Context): Boolean {
        val executionPaths = sequenceOf(
            { performIntentLaunch(context, Intent("miui.intent.action.ACTIVITY_MIPLAY_DETAIL")) },

            {
                performIntentLaunch(
                    context, Intent().setClassName(
                        "miui.systemui.plugin", "miui.systemui.miplay.MiPlayDetailActivity"
                    )
                )
            },

            {
                performIntentLaunch(
                    context, Intent().setClassName(
                        "com.samsung.android.mdx.quickboard",
                        "com.samsung.android.mdx.quickboard.view.MediaActivity"
                    )
                )
            },

            { invokeNativeSystemRouter(context) },

            {
                performIntentLaunch(
                    context,
                    Intent("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG").setPackage("com.android.systemui")
                )
            },

            {
                performIntentLaunch(
                    context, Intent("com.android.settings.panel.action.MEDIA_OUTPUT")
                )
            })

        return executionPaths.any { attempt -> attempt() }
    }

    private fun performIntentLaunch(ctx: Context, targetIntent: Intent): Boolean {
        return try {
            targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(targetIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun invokeNativeSystemRouter(ctx: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return try {
                MediaRouter2.getInstance(ctx).showSystemOutputSwitcher()
                true
            } catch (_: Exception) {
                false
            }
        }
        return false
    }
}