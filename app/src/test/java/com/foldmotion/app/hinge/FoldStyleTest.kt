package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoldStyleTest {

    @Test
    fun fluidCompressesTowardCenterMoreThanHingeShadow() {
        val fluid = FoldFxParams.compose(0.5f, FoldStyle.FLUID, strength = 1f)
        val shadow = FoldFxParams.compose(0.5f, FoldStyle.HINGE_SHADOW, strength = 1f)

        assertThat(fluid.scale).isLessThan(shadow.scale)
        assertThat(shadow.hingeShadow).isGreaterThan(fluid.hingeShadow)
    }

    @Test
    fun fadePrefersDimOverHingeBand() {
        val fade = FoldFxParams.compose(0.5f, FoldStyle.FADE, strength = 1f)
        val fluid = FoldFxParams.compose(0.5f, FoldStyle.FLUID, strength = 1f)

        assertThat(fade.dim).isGreaterThan(fluid.dim)
        assertThat(fade.hingeShadow).isLessThan(fluid.hingeShadow)
        assertThat(fade.scale).isGreaterThan(fluid.scale)
    }

    @Test
    fun hapticStyleKeepsVisualsQuieterThanFluid() {
        val haptic = FoldFxParams.compose(0.5f, FoldStyle.HAPTIC, strength = 1f)
        val fluid = FoldFxParams.compose(0.5f, FoldStyle.FLUID, strength = 1f)

        assertThat(haptic.dim).isLessThan(fluid.dim)
        assertThat(haptic.vignette).isLessThan(fluid.vignette)
        assertThat(haptic.scale).isGreaterThan(fluid.scale)
    }

    @Test
    fun mediumStrengthIsWeakerThanFull() {
        val full = FoldFxParams.compose(0.5f, FoldStyle.FLUID, strength = 1f)
        val medium = FoldFxParams.compose(0.5f, FoldStyle.FLUID, strength = FoldFxParams.DEFAULT_STRENGTH)

        assertThat(medium.dim).isLessThan(full.dim)
        assertThat(medium.hingeShadow).isLessThan(full.hingeShadow)
        assertThat(medium.scale).isGreaterThan(full.scale)
        assertThat(medium.coverReveal).isEqualTo(full.coverReveal)
    }

    @Test
    fun defaultStyleIsFluidAtMediumStrength() {
        assertThat(FoldStyle.DEFAULT).isEqualTo(FoldStyle.FLUID)
        assertThat(FoldFxParams.DEFAULT_STRENGTH).isWithin(0.001f).of(0.70f)
    }
}
