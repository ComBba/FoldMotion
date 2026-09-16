package com.foldmotion.app.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.foldmotion.app.MainActivity
import com.foldmotion.app.R
import com.foldmotion.app.hinge.FoldAngleSmoother
import com.foldmotion.app.hinge.FoldFxParams
import com.foldmotion.app.hinge.HingeAvailability
import com.foldmotion.app.hinge.SensorHingeAngleSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class FoldOverlayService : Service() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + job)
    private lateinit var overlay: FoldOverlayWindow
    private lateinit var desiredStore: OverlayDesiredStore
    private var engineJob: Job? = null
    private var smoother = FoldAngleSmoother.idle(180f)
    private var lastTarget: Float? = null
    private var lastFx: FoldFxParams? = null
    private var latestAngle: Float? = null
    private val wake = MutableSharedFlow<Unit>()

    override fun onCreate() {
        super.onCreate()
        overlay = FoldOverlayWindow(this)
        desiredStore = OverlayDesiredStore(this)
        ensureChannel()
        _isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            desiredStore.setDesired(false)
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        startInForeground()
        overlay.attach()
        if (engineJob == null) {
            startEngine()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        engineJob?.cancel()
        engineJob = null
        scope.cancel()
        overlay.detach()
        _isRunning.value = false
        super.onDestroy()
    }

    private fun startEngine() {
        val source = SensorHingeAngleSource(this)
        engineJob = scope.launch {
            launch {
                source.observe().collect { availability ->
                    latestAngle = (availability as? HingeAvailability.Present)?.angleDegrees
                    wake.tryEmit(Unit)
                }
            }
            launch {
                OverlayInput.debugPresetDegrees.collect { wake.tryEmit(Unit) }
            }
            while (isActive) {
                val now = SystemClock.uptimeMillis()
                val target = OverlayInput.overlayAngle(
                    OverlayInput.debugPresetDegrees.value,
                    latestAngle,
                )
                if (target != null) {
                    if (target != lastTarget) {
                        smoother = if (lastTarget == null) {
                            FoldAngleSmoother.idle(target)
                        } else {
                            smoother.retarget(target, now)
                        }
                        lastTarget = target
                    }
                    val fx = FoldFxParams.fromAngle(smoother.sample(now))
                    if (OverlayTickPolicy.shouldPublish(lastFx, fx)) {
                        overlay.setParams(fx)
                        lastFx = fx
                    }
                }
                if (smoother.isSettled(now)) {
                    withTimeoutOrNull(OverlayTickPolicy.IDLE_DELAY_MS) {
                        wake.first()
                    }
                } else {
                    delay(OverlayTickPolicy.ANIMATING_DELAY_MS)
                }
            }
        }
    }

    private fun startInForeground() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.overlay_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            this,
            1,
            Intent(this, FoldOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle(getString(R.string.overlay_notification_title))
            .setContentText(getString(R.string.overlay_notification_text))
            .setOngoing(true)
            .setContentIntent(openApp)
            .addAction(
                0,
                getString(R.string.overlay_notification_stop),
                stop,
            )
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    companion object {
        const val ACTION_STOP = "com.foldmotion.app.STOP_OVERLAY"
        private const val CHANNEL_ID = "fold_overlay"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "FoldMotion"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning

        fun startFromUser(context: Context) {
            val app = context.applicationContext
            try {
                ContextCompat.startForegroundService(
                    app,
                    Intent(app, FoldOverlayService::class.java),
                )
            } catch (error: Exception) {
                Log.e(TAG, "startForegroundService blocked", error)
            }
        }

        fun stop(context: Context) {
            val app = context.applicationContext
            app.stopService(Intent(app, FoldOverlayService::class.java))
        }
    }
}
