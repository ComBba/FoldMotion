package com.foldmotion.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foldmotion.app.cover.CoverScreenSession
import com.foldmotion.app.hinge.SensorHingeAngleSource
import com.foldmotion.app.overlay.FoldOverlayService
import com.foldmotion.app.overlay.OverlayDesiredStore
import com.foldmotion.app.overlay.OverlayPolicy
import com.foldmotion.app.ui.HingeProbeRoute
import com.foldmotion.app.ui.HingeProbeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val overlayStore by lazy { OverlayDesiredStore(this) }
    private val viewModel: HingeProbeViewModel by viewModels {
        HingeProbeViewModel.Factory(
            SensorHingeAngleSource(applicationContext),
            overlayStore,
        )
    }
    private var coverSession: CoverScreenSession? = null
    private var overlayAttached by mutableStateOf(false)
    private var canDrawOverlays by mutableStateOf(false)

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* FGS notification still posts; this is best-effort. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        coverSession = CoverScreenSession(this)
        canDrawOverlays = Settings.canDrawOverlays(this)
        overlayAttached = FoldOverlayService.isRunning.value
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    coverSession?.setFoldProgress(state.progress ?: 0f)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                FoldOverlayService.isRunning.collect { running ->
                    overlayAttached = running
                    val desired = overlayStore.isDesired()
                    if (viewModel.state.value.overlayDesired != desired) {
                        viewModel.setOverlayDesired(desired)
                    }
                }
            }
        }
        setContent {
            MaterialTheme {
                HingeProbeRoute(
                    viewModel = viewModel,
                    overlayAttached = overlayAttached,
                    canDrawOverlays = canDrawOverlays,
                    onToggleOverlay = ::onToggleOverlay,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        canDrawOverlays = Settings.canDrawOverlays(this)
        syncDesiredFromStore()
        overlayAttached = FoldOverlayService.isRunning.value
        reconcileService()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            coverSession?.onHostFocused()
        }
    }

    override fun onDestroy() {
        coverSession?.release()
        coverSession = null
        super.onDestroy()
    }

    private fun onToggleOverlay(enabled: Boolean) {
        viewModel.setOverlayDesired(enabled)
        if (!enabled) {
            FoldOverlayService.stop(this)
            return
        }
        requestNotificationPermission()
        if (!Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
            return
        }
        FoldOverlayService.startFromUser(this)
    }

    private fun syncDesiredFromStore() {
        val desired = overlayStore.isDesired()
        if (viewModel.state.value.overlayDesired != desired) {
            viewModel.setOverlayDesired(desired)
        }
    }

    private fun reconcileService() {
        val desired = overlayStore.isDesired()
        val shouldRun = OverlayPolicy.shouldRunForegroundService(desired, canDrawOverlays)
        val running = FoldOverlayService.isRunning.value
        when {
            shouldRun && !running -> FoldOverlayService.startFromUser(this)
            !shouldRun && running -> FoldOverlayService.stop(this)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
