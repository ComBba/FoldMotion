package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CoverPowerPolicyTest {

    @Test
    fun staysOffWhileFullyOpen() {
        assertThat(CoverPowerPolicy.shouldPowerCover(0f, currentlyOn = false)).isFalse()
    }

    @Test
    fun turnsOnOnceFoldStarts() {
        assertThat(CoverPowerPolicy.shouldPowerCover(0.20f, currentlyOn = false)).isTrue()
        assertThat(CoverPowerPolicy.shouldPowerCover(0.19f, currentlyOn = false)).isFalse()
    }

    @Test
    fun hysteresisKeepsCoverOnUntilNearlyOpen() {
        assertThat(CoverPowerPolicy.shouldPowerCover(0.10f, currentlyOn = true)).isTrue()
        assertThat(CoverPowerPolicy.shouldPowerCover(0.07f, currentlyOn = true)).isFalse()
    }
}
