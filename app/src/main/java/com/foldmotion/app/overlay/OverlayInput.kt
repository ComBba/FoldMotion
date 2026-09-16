package com.foldmotion.app.overlay

import kotlinx.coroutines.flow.MutableStateFlow

object OverlayInput {
    val debugPresetDegrees = MutableStateFlow<Float?>(null)

    fun overlayAngle(debugPresetDegrees: Float?, sensorAngle: Float?): Float? {
        return debugPresetDegrees ?: sensorAngle
    }
}
