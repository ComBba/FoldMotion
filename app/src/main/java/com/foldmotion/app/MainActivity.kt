package com.foldmotion.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foldmotion.app.cover.CoverScreenSession
import com.foldmotion.app.hinge.SensorHingeAngleSource
import com.foldmotion.app.ui.HingeProbeRoute
import com.foldmotion.app.ui.HingeProbeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: HingeProbeViewModel by viewModels {
        HingeProbeViewModel.Factory(SensorHingeAngleSource(applicationContext))
    }
    private var coverSession: CoverScreenSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        coverSession = CoverScreenSession(this)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    coverSession?.setFoldProgress(state.progress ?: 0f)
                }
            }
        }
        setContent {
            MaterialTheme {
                HingeProbeRoute(viewModel = viewModel)
            }
        }
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
}
