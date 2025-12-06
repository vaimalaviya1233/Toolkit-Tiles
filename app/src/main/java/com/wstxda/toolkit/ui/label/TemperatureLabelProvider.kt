package com.wstxda.toolkit.ui.label

import android.content.Context
import com.wstxda.toolkit.R
import java.util.Locale

class TemperatureLabelProvider(private val context: Context) {

    fun getLabel(isActive: Boolean, temp: Float): CharSequence {
        return if (isActive) {
            String.format(Locale.US, context.getString(R.string.temperature_tile_format), temp)
        } else {
            context.getString(R.string.temperature_tile)
        }
    }

    fun getSubtitle(isActive: Boolean): CharSequence? {
        return if (isActive) {
            context.getString(R.string.temperature_tile_source)
        } else {
            context.getString(R.string.tile_off)
        }
    }
}