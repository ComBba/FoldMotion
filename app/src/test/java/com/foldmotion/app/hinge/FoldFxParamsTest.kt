package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoldFxParamsTest {

    @Test
    fun openHasNoOverlay() {
        val fx = FoldFxParams.fromProgress(0f)

        assertThat(fx.dim).isEqualTo(0f)
        assertThat(fx.hingeShadow).isEqualTo(0f)
        assertThat(fx.vignette).isEqualTo(0f)
        assertThat(fx.blur).isEqualTo(0f)
        assertThat(fx.blackout).isEqualTo(0f)
        assertThat(fx.coverReveal).isEqualTo(0f)
        assertThat(fx.scale).isEqualTo(1f)
        assertThat(fx.rotationY).isEqualTo(0f)
        assertThat(fx.contentAlpha).isEqualTo(1f)
    }

    @Test
    fun closedRevealsCoverInsteadOfStayingBlack() {
        val fx = FoldFxParams.fromProgress(1f)

        assertThat(fx.coverReveal).isEqualTo(1f)
        assertThat(fx.blackout).isLessThan(0.2f)
        assertThat(fx.scale).isLessThan(1f)
        assertThat(fx.contentAlpha).isLessThan(1f)
    }

    @Test
    fun halfOpenStartsDimAndShadowButNotBlackout() {
        val fx = FoldFxParams.fromAngle(90f)

        assertThat(fx.dim).isGreaterThan(0f)
        assertThat(fx.hingeShadow).isGreaterThan(0.4f)
        assertThat(fx.vignette).isGreaterThan(0f)
        assertThat(fx.blackout).isLessThan(0.15f)
    }

    @Test
    fun fortyFiveDegreesDeepensBlackout() {
        val open = FoldFxParams.fromAngle(90f)
        val closing = FoldFxParams.fromAngle(45f)

        assertThat(closing.blackout).isGreaterThan(open.blackout)
        assertThat(closing.dim).isGreaterThan(open.dim)
        assertThat(closing.scale).isLessThan(open.scale)
    }

    @Test
    fun clampsProgressOutsideUnitRange() {
        assertThat(FoldFxParams.fromProgress(-1f)).isEqualTo(FoldFxParams.fromProgress(0f))
        assertThat(FoldFxParams.fromProgress(2f)).isEqualTo(FoldFxParams.fromProgress(1f))
    }
}
