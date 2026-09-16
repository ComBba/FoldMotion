package com.foldmotion.app.hinge

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SensorHingeAngleSource(
    context: Context,
) : HingeAngleSource {
    private val sensorManager = context.getSystemService(SensorManager::class.java)
    private val hingeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE)

    override fun observe(): Flow<HingeAvailability> = callbackFlow {
        Log.d(TAG, "hingeSensor = $hingeSensor")
        if (hingeSensor == null) {
            trySend(HingeAvailability.Missing)
            awaitClose { }
            return@callbackFlow
        }

        trySend(HingeAvailability.Present(null))
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val angle = event.values[0]
                Log.d(TAG, "angle = $angle")
                trySend(HingeAvailability.Present(angle))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            hingeSensor,
            SensorManager.SENSOR_DELAY_GAME,
        )
        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private companion object {
        const val TAG = "FoldMotion"
    }
}
