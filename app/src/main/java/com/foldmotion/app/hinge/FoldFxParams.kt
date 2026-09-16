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

        fun compose(
            progress: Float,
            style: FoldStyle = FoldStyle.DEFAULT,
            strength: Float = DEFAULT_STRENGTH,
        ): FoldFxParams {
            val base = fromProgress(progress)
            val styled = when (style) {
                FoldStyle.FLUID -> base
                FoldStyle.HINGE_SHADOW -> base.copy(
                    dim = base.dim * 0.55f,
                    hingeShadow = (base.hingeShadow * 1.35f).coerceAtMost(1f),
                    vignette = base.vignette * 0.45f,
                    blur = base.blur * 0.40f,
                    blackout = base.blackout * 0.35f,
                    scale = 1f - (1f - base.scale) * 0.35f,
                    rotationY = base.rotationY * 0.35f,
                    contentAlpha = 1f - (1f - base.contentAlpha) * 0.40f,
                )
                FoldStyle.FADE -> base.copy(
                    dim = (base.dim * 1.35f).coerceAtMost(0.75f),
                    hingeShadow = base.hingeShadow * 0.25f,
                    vignette = (base.vignette * 1.15f).coerceAtMost(1f),
                    blur = base.blur * 0.50f,
                    blackout = (base.blackout * 1.40f).coerceAtMost(1f),
                    scale = 1f - (1f - base.scale) * 0.20f,
                    rotationY = base.rotationY * 0.15f,
                    contentAlpha = 1f - (1f - base.contentAlpha) * 0.85f,
                )
                FoldStyle.HAPTIC -> base.copy(
                    dim = base.dim * 0.40f,
                    hingeShadow = base.hingeShadow * 0.50f,
                    vignette = base.vignette * 0.35f,
                    blur = base.blur * 0.25f,
                    blackout = base.blackout * 0.30f,
                    scale = 1f - (1f - base.scale) * 0.25f,
                    rotationY = base.rotationY * 0.20f,
                    contentAlpha = 1f - (1f - base.contentAlpha) * 0.30f,
                )
            }
            return styled.withStrength(strength)
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

        const val DEFAULT_STRENGTH = 0.70f

        private fun FoldFxParams.withStrength(strength: Float): FoldFxParams {
            val s = strength.coerceIn(0.30f, 1f)
            return copy(
                dim = dim * s,
                hingeShadow = hingeShadow * s,
                vignette = vignette * s,
                blur = blur * s,
                blackout = blackout * s,
                scale = 1f - (1f - scale) * s,
                rotationY = rotationY * s,
                contentAlpha = 1f - (1f - contentAlpha) * s,
            )
        }

        private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
            val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
            return t * t * (3f - 2f * t)
        }
    }
}
