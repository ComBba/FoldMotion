package com.foldmotion.app.hinge

enum class FoldStyle {
    FLUID,
    HINGE_SHADOW,
    FADE,
    HAPTIC,
    ;

    companion object {
        val DEFAULT: FoldStyle = FLUID
    }
}
