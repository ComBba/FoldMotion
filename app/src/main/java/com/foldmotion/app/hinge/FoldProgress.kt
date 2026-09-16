package com.foldmotion.app.hinge

object FoldProgress {
    const val OPEN_DEGREES = 180f
    const val CLOSED_DEGREES = 0f

    fun fromAngle(angleDegrees: Float): Float {
        val clamped = angleDegrees.coerceIn(CLOSED_DEGREES, OPEN_DEGREES)
        return 1f - (clamped / OPEN_DEGREES)
    }
}
