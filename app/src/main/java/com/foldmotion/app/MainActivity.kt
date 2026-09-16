package com.foldmotion.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foldmotion.app.cover.CoverScreenSession
import com.foldmotion.app.hinge.HingeUiState
import com.foldmotion.app.hinge.SensorHingeAngleSource
import com.foldmotion.app.overlay.FoldOverlayWindow
import com.foldmotion.app.overlay.OverlayAttachResult
import com.foldmotion.app.overlay.OverlayPolicy
import com.foldmotion.app.ui.HingeProbeRoute
import com.foldmotion.app.ui.HingeProbeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: HingeProbeViewModel by viewModels {
        HingeProbeViewModel.Factory(SensorHingeAngleSource(applicationContext))
    }
    private var coverSession: CoverScreenSession? = null
    private lateinit var overlayWindow: FoldOverlayWindow
    private var overlayAttached by mutableStateOf(false)
    private var canDrawOverlays by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        overlayWindow = FoldOverlayWindow(this)
        coverSession = CoverScreenSession(this)
        canDrawOverlays = Settings.canDrawOverlays(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    coverSession?.setFoldProgress(state.progress ?: 0f)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.state.collect(::syncOverlay)
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
        syncOverlay(viewModel.state.value)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            coverSession?.onHostFocused()
        }
    }

    override fun onDestroy() {
        overlayWindow.detach()
        overlayAttached = false
        coverSession?.release()
        coverSession = null
        super.onDestroy()
    }

    private fun onToggleOverlay(enabled: Boolean) {
        viewModel.setOverlayDesired(enabled)
        if (enabled && !Settings.canDrawOverlays(this)) {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"),
                ),
            )
        }
    }

    private fun syncOverlay(state: HingeUiState) {
        canDrawOverlays = Settings.canDrawOverlays(this)
        when (OverlayPolicy.resolve(state.overlayDesired, canDrawOverlays)) {
            OverlayAttachResult.ATTACHED -> {
                overlayWindow.attach()
                overlayWindow.setParams(state.fx)
                overlayAttached = overlayWindow.isAttached
            }
            OverlayAttachResult.NEEDS_PERMISSION,
            OverlayAttachResult.DISABLED,
            -> {
                overlayWindow.detach()
                overlayAttached = false
            }
        }
    }
}
