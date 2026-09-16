package com.foldmotion.app.hinge

object CoverPowerPolicy {
    const val ON_AT = 0.20f
    const val OFF_AT = 0.08f

    fun shouldPowerCover(progress: Float, currentlyOn: Boolean): Boolean {
        return if (currentlyOn) {
            progress >= OFF_AT
        } else {
            progress >= ON_AT
        }
    }
}
