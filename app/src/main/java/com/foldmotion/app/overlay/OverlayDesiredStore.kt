package com.foldmotion.app.overlay

import android.content.Context
import com.foldmotion.app.hinge.FoldFxParams
import com.foldmotion.app.hinge.FoldStyle

class OverlayDesiredStore(
    context: Context,
) {
    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    init {
        OverlayInput.settings.value = snapshot()
    }

    fun snapshot(): OverlaySettings {
        val styleName = prefs.getString(KEY_STYLE, FoldStyle.DEFAULT.name)
        val style = FoldStyle.entries.find { it.name == styleName } ?: FoldStyle.DEFAULT
        return OverlaySettings(
            enabled = prefs.getBoolean(KEY, false),
            style = style,
            strength = prefs.getFloat(KEY_STRENGTH, FoldFxParams.DEFAULT_STRENGTH),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, false),
        )
    }

    fun isDesired(): Boolean = OverlayInput.settings.value.enabled

    fun setDesired(value: Boolean) {
        write(snapshot().copy(enabled = value))
    }

    fun setStyle(style: FoldStyle) {
        val current = snapshot()
        write(
            current.copy(
                style = style,
                hapticEnabled = if (style == FoldStyle.HAPTIC) true else current.hapticEnabled,
            ),
        )
    }

    fun setStrength(strength: Float) {
        write(snapshot().copy(strength = strength.coerceIn(0.30f, 1f)))
    }

    fun setHapticEnabled(enabled: Boolean) {
        write(snapshot().copy(hapticEnabled = enabled))
    }

    private fun write(settings: OverlaySettings) {
        prefs.edit()
            .putBoolean(KEY, settings.enabled)
            .putString(KEY_STYLE, settings.style.name)
            .putFloat(KEY_STRENGTH, settings.strength)
            .putBoolean(KEY_HAPTIC, settings.hapticEnabled)
            .apply()
        OverlayInput.settings.value = settings
    }

    private companion object {
        const val PREFS = "foldmotion"
        const val KEY = "overlay_desired"
        const val KEY_STYLE = "overlay_style"
        const val KEY_STRENGTH = "overlay_strength"
        const val KEY_HAPTIC = "overlay_haptic"
    }
}
