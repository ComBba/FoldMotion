package com.foldmotion.app.hinge

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FoldAngleSmootherTest {

    @Test
    fun startsAtIncomingTarget() {
        val smoother = FoldAngleSmoother.idle(180f)

        assertThat(smoother.sample(nowMs = 0L)).isEqualTo(180f)
    }

    @Test
    fun doesNotJumpImmediatelyWhenTargetChanges() {
        val smoother = FoldAngleSmoother.idle(180f)
            .retarget(90f, nowMs = 1_000L)

        assertThat(smoother.sample(nowMs = 1_000L)).isEqualTo(180f)
        assertThat(smoother.sample(nowMs = 1_140L)).isGreaterThan(90f)
        assertThat(smoother.sample(nowMs = 1_140L)).isLessThan(180f)
    }

    @Test
    fun reachesTargetAtDuration() {
        val smoother = FoldAngleSmoother.idle(180f)
            .retarget(0f, nowMs = 0L, durationMs = 280L)

        assertThat(smoother.sample(nowMs = 280L)).isEqualTo(0f)
        assertThat(smoother.sample(nowMs = 400L)).isEqualTo(0f)
    }

    @Test
    fun retargetMidFlightStartsFromCurrentSample() {
        val first = FoldAngleSmoother.idle(180f)
            .retarget(0f, nowMs = 0L, durationMs = 200L)
        val mid = first.sample(nowMs = 100L)
        val second = first.retarget(90f, nowMs = 100L, durationMs = 200L)

        assertThat(second.sample(nowMs = 100L)).isWithin(0.01f).of(mid)
        assertThat(second.sample(nowMs = 300L)).isEqualTo(90f)
    }

    @Test
    fun isSettledAfterDurationAndWhileIdle() {
        val idle = FoldAngleSmoother.idle(180f)
        assertThat(idle.isSettled(nowMs = 0L)).isTrue()

        val inFlight = idle.retarget(90f, nowMs = 1_000L, durationMs = 280L)
        assertThat(inFlight.isSettled(nowMs = 1_000L)).isFalse()
        assertThat(inFlight.isSettled(nowMs = 1_279L)).isFalse()
        assertThat(inFlight.isSettled(nowMs = 1_280L)).isTrue()
    }
}
