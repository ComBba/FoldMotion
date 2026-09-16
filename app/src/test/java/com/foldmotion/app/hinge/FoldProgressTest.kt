package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoldProgressTest {

    @Test
    fun fullyOpenMapsToZero() {
        assertThat(FoldProgress.fromAngle(180f)).isEqualTo(0f)
    }

    @Test
    fun fullyClosedMapsToOne() {
        assertThat(FoldProgress.fromAngle(0f)).isEqualTo(1f)
    }

    @Test
    fun halfwayMapsToHalf() {
        assertThat(FoldProgress.fromAngle(90f)).isEqualTo(0.5f)
    }

    @Test
    fun clampsAboveOpen() {
        assertThat(FoldProgress.fromAngle(200f)).isEqualTo(0f)
    }

    @Test
    fun clampsBelowClosed() {
        assertThat(FoldProgress.fromAngle(-10f)).isEqualTo(1f)
    }
}
