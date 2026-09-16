package com.foldmotion.app.overlay

import android.content.Context

class OverlayDesiredStore(
    context: Context,
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isDesired(): Boolean = prefs.getBoolean(KEY, false)

    fun setDesired(value: Boolean) {
        prefs.edit().putBoolean(KEY, value).apply()
    }

    private companion object {
        const val PREFS = "foldmotion"
        const val KEY = "overlay_desired"
    }
}
