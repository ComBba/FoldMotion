package com.foldmotion.app.overlay

import com.foldmotion.app.hinge.FoldFxParams
import com.foldmotion.app.hinge.FoldStyle

data class OverlaySettings(
    val enabled: Boolean = false,
    val style: FoldStyle = FoldStyle.DEFAULT,
    val strength: Float = FoldFxParams.DEFAULT_STRENGTH,
    val hapticEnabled: Boolean = false,
) {
    companion object {
        val STRENGTH_PRESETS: List<Pair<String, Float>> = listOf(
            "약" to 0.45f,
            "중" to FoldFxParams.DEFAULT_STRENGTH,
            "강" to 0.90f,
        )
    }
}
