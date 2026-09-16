package com.foldmotion.app.hinge

object FoldHapticPolicy {
    fun shouldPulse(
        previousAngle: Float?,
        nextAngle: Float,
        hapticEnabled: Boolean,
    ): Boolean {
        if (!hapticEnabled) return false
        val previous = previousAngle ?: return false
        return bucket(previous) != bucket(nextAngle)
    }

    internal fun bucket(angle: Float): Int {
        return when {
            angle > 135f -> 180
            angle > 45f -> 90
            else -> 0
        }
    }
}
