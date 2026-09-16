package com.foldmotion.app.hinge

sealed interface HingeAvailability {
    data object Unknown : HingeAvailability
    data object Missing : HingeAvailability
    data class Present(val angleDegrees: Float?) : HingeAvailability
}

data class HingeUiState(
    val availability: HingeAvailability = HingeAvailability.Unknown,
    val debugPresetDegrees: Float? = null,
    val smoothedAngle: Float? = null,
    val overlayDesired: Boolean = false,
) {
    val rawAngle: Float?
        get() = debugPresetDegrees
            ?: (availability as? HingeAvailability.Present)?.angleDegrees

    val displayedAngle: Float?
        get() = smoothedAngle ?: rawAngle

    val progress: Float?
        get() = displayedAngle?.let(FoldProgress::fromAngle)

    val fx: FoldFxParams
        get() = FoldFxParams.fromProgress(progress ?: 0f)

    val sensorLabel: String
        get() = when (availability) {
            HingeAvailability.Unknown -> "UNKNOWN"
            HingeAvailability.Missing -> "MISSING"
            is HingeAvailability.Present -> "PRESENT"
        }

    companion object {
        val DEBUG_PRESETS: List<Float> = listOf(180f, 90f, 45f, 0f)
    }
}
