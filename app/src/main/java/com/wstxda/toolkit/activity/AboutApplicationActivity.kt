package com.wstxda.toolkit.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wstxda.toolkit.component.AboutApplicationBottomSheetDialog

class AboutApplicationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdgeNoContrast()
        applySystemBarInsets(window.decorView)

        if (savedInstanceState == null) {
            AboutApplicationBottomSheetDialog().show(supportFragmentManager, "about_app")
        }
    }

    private fun enableEdgeToEdgeNoContrast() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
            )
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun applySystemBarInsets(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, 0, bars.right, bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    @Suppress("DEPRECATION")
    override fun onPause() {
        super.onPause()
        if (isFinishing) overridePendingTransition(0, 0)
    }
}