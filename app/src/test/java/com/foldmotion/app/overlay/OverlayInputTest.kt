package com.foldmotion.app.overlay

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OverlayInputTest {

    @Test
    fun debugPresetOverridesLiveSensor() {
        assertThat(OverlayInput.overlayAngle(debugPresetDegrees = 45f, sensorAngle = 180f))
            .isEqualTo(45f)
    }

    @Test
    fun liveSensorIsUsedWhenPresetIsOff() {
        assertThat(OverlayInput.overlayAngle(debugPresetDegrees = null, sensorAngle = 90f))
            .isEqualTo(90f)
        assertThat(OverlayInput.overlayAngle(debugPresetDegrees = null, sensorAngle = null))
            .isNull()
    }
}
