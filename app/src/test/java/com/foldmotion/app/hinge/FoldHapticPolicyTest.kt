package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoldHapticPolicyTest {

    @Test
    fun staysQuietWhenHapticDisabled() {
        assertThat(
            FoldHapticPolicy.shouldPulse(
                previousAngle = 180f,
                nextAngle = 90f,
                hapticEnabled = false,
            ),
        ).isFalse()
    }

    @Test
    fun pulsesWhenCrossingDiscreteHingeBuckets() {
        assertThat(
            FoldHapticPolicy.shouldPulse(
                previousAngle = 180f,
                nextAngle = 90f,
                hapticEnabled = true,
            ),
        ).isTrue()
        assertThat(
            FoldHapticPolicy.shouldPulse(
                previousAngle = 90f,
                nextAngle = 0f,
                hapticEnabled = true,
            ),
        ).isTrue()
    }

    @Test
    fun doesNotPulseForSameBucketOrMissingPrevious() {
        assertThat(
            FoldHapticPolicy.shouldPulse(
                previousAngle = 90f,
                nextAngle = 92f,
                hapticEnabled = true,
            ),
        ).isFalse()
        assertThat(
            FoldHapticPolicy.shouldPulse(
                previousAngle = null,
                nextAngle = 90f,
                hapticEnabled = true,
            ),
        ).isFalse()
    }
}
