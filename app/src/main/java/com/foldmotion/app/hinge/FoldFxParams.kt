package com.foldmotion.app.hinge

data class FoldFxParams(
    val dim: Float,
    val hingeShadow: Float,
    val vignette: Float,
    val blur: Float,
    val blackout: Float,
    val coverReveal: Float,
    val scale: Float,
    val rotationY: Float,
    val contentAlpha: Float,
) {
    companion object {
        fun fromAngle(angleDegrees: Float): FoldFxParams {
            return fromProgress(FoldProgress.fromAngle(angleDegrees))
        }

        fun fromProgress(progress: Float): FoldFxParams {
            val t = progress.coerceIn(0f, 1f)
            return FoldFxParams(
                dim = smoothstep(0.25f, 0.80f, t) * 0.50f,
                hingeShadow = smoothstep(0.08f, 0.55f, t),
                vignette = smoothstep(0.35f, 0.85f, t),
                blur = smoothstep(0.40f, 0.90f, t) * 0.65f,
                blackout = smoothstep(0.70f, 0.92f, t) * (1f - smoothstep(0.82f, 1.00f, t)),
                coverReveal = smoothstep(0.82f, 1.00f, t),
                scale = 1f - t * 0.08f,
                rotationY = t * 8f,
                contentAlpha = 1f - t * 0.45f,
            )
        }

        private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
            val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return t * t * (3f - 2f * t)
        }
    }
}
