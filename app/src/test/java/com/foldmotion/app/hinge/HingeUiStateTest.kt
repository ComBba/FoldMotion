package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HingeUiStateTest {

    @Test
    fun livePresentUsesSensorAngle() {
        val state = HingeUiState(
            availability = HingeAvailability.Present(137.4f),
        )

        assertThat(state.displayedAngle).isEqualTo(137.4f)
        assertThat(state.progress).isWithin(0.0001f).of(FoldProgress.fromAngle(137.4f))
        assertThat(state.sensorLabel).isEqualTo("PRESENT")
    }

    @Test
    fun debugPresetOverridesLiveAngle() {
        val state = HingeUiState(
            availability = HingeAvailability.Present(137.4f),
            debugPresetDegrees = 45f,
        )

        assertThat(state.displayedAngle).isEqualTo(45f)
        assertThat(state.progress).isEqualTo(0.75f)
    }

    @Test
    fun missingSensorHasNoAngleUntilPreset() {
        val live = HingeUiState(availability = HingeAvailability.Missing)
        assertThat(live.displayedAngle).isNull()
        assertThat(live.progress).isNull()
        assertThat(live.sensorLabel).isEqualTo("MISSING")

        val preset = live.copy(debugPresetDegrees = 90f)
        assertThat(preset.displayedAngle).isEqualTo(90f)
        assertThat(preset.progress).isEqualTo(0.5f)
    }

    @Test
    fun smoothedAngleWinsOverRaw() {
        val state = HingeUiState(
            availability = HingeAvailability.Present(180f),
            smoothedAngle = 135f,
        )

        assertThat(state.rawAngle).isEqualTo(180f)
        assertThat(state.displayedAngle).isEqualTo(135f)
    }

    @Test
    fun fxFollowsSmoothedProgress() {
        val state = HingeUiState(
            availability = HingeAvailability.Present(180f),
            smoothedAngle = 90f,
        )

        assertThat(state.fx).isEqualTo(
            FoldFxParams.compose(0.5f, FoldStyle.FLUID, FoldFxParams.DEFAULT_STRENGTH),
        )
        assertThat(state.fx.hingeShadow).isGreaterThan(0f)
    }

    @Test
    fun presentWithoutSampleStaysWaiting() {
        val state = HingeUiState(availability = HingeAvailability.Present(null))
        assertThat(state.displayedAngle).isNull()
        assertThat(state.sensorLabel).isEqualTo("PRESENT")
    }
}
