package com.foldmotion.app.hinge

data class FoldAngleSmoother(
    private val from: Float,
    private val target: Float,
    private val startedAtMs: Long,
    private val durationMs: Long,
) {
    fun retarget(
        target: Float,
        nowMs: Long,
        durationMs: Long = this.durationMs,
    ): FoldAngleSmoother {
        if (target == this.target && nowMs >= startedAtMs + this.durationMs) {
            return copy(from = target, target = target, startedAtMs = nowMs, durationMs = durationMs)
        }
        return FoldAngleSmoother(
            from = sample(nowMs),
            target = target,
            startedAtMs = nowMs,
            durationMs = durationMs,
        )
    }

    fun sample(nowMs: Long): Float {
        if (durationMs <= 0L || from == target) return target
        val elapsed = (nowMs - startedAtMs).coerceAtLeast(0L)
        if (elapsed >= durationMs) return target
        val t = easeInOut(elapsed.toFloat() / durationMs.toFloat())
        return from + (target - from) * t
    }

    fun isSettled(nowMs: Long): Boolean {
        return from == target || durationMs <= 0L || nowMs >= startedAtMs + durationMs
    }

    companion object {
        const val DEFAULT_DURATION_MS = 280L

        fun idle(angle: Float, durationMs: Long = DEFAULT_DURATION_MS): FoldAngleSmoother {
            return FoldAngleSmoother(
                from = angle,
                target = angle,
                startedAtMs = 0L,
                durationMs = durationMs,
            )
        }

        private fun easeInOut(t: Float): Float {
            return if (t < 0.5f) {
                2f * t * t
            } else {
                val u = -2f * t + 2f
                1f - (u * u) / 2f
            }
        }
    }
}
