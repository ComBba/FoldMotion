package com.foldmotion.app.overlay

import com.foldmotion.app.hinge.FoldFxParams
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OverlayTickPolicyTest {

    @Test
    fun animatesAtDisplayRefreshWhileFolding() {
        assertThat(OverlayTickPolicy.delayMs(settled = false)).isEqualTo(16L)
    }

    @Test
    fun throttlesWhenHingeIsIdle() {
        assertThat(OverlayTickPolicy.delayMs(settled = true)).isEqualTo(250L)
    }

    @Test
    fun skipsPublishWhenFxUnchanged() {
        val fx = FoldFxParams.fromProgress(0.5f)
        assertThat(OverlayTickPolicy.shouldPublish(previous = fx, next = fx)).isFalse()
        assertThat(OverlayTickPolicy.shouldPublish(previous = null, next = fx)).isTrue()
        assertThat(
            OverlayTickPolicy.shouldPublish(
                previous = FoldFxParams.fromProgress(0f),
                next = fx,
            ),
        ).isTrue()
    }
}
