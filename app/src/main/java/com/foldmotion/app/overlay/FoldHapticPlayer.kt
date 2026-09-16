package com.foldmotion.app.overlay

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

class FoldHapticPlayer(
    context: Context,
) {
    private val vibrator = context.applicationContext.getSystemService(Vibrator::class.java)

    fun pulse() {
        if (vibrator == null || !vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(DURATION_MS, AMPLITUDE))
    }

    private companion object {
        const val DURATION_MS = 18L
        const val AMPLITUDE = 48
    }
}
