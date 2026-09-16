package com.foldmotion.app.overlay

import com.foldmotion.app.hinge.FoldFxParams

object OverlayTickPolicy {
    const val ANIMATING_DELAY_MS = 16L
    const val IDLE_DELAY_MS = 250L

    fun delayMs(settled: Boolean): Long {
        return if (settled) IDLE_DELAY_MS else ANIMATING_DELAY_MS
    }

    fun shouldPublish(previous: FoldFxParams?, next: FoldFxParams): Boolean {
        return previous != next
    }
}
