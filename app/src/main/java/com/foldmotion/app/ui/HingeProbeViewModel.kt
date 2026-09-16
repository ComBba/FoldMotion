package com.foldmotion.app.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.foldmotion.app.hinge.FoldAngleSmoother
import com.foldmotion.app.hinge.HingeAngleSource
import com.foldmotion.app.hinge.HingeUiState
import com.foldmotion.app.overlay.OverlayDesiredStore
import com.foldmotion.app.overlay.OverlayInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HingeProbeViewModel(
    private val hingeAngleSource: HingeAngleSource,
    private val overlayDesiredStore: OverlayDesiredStore,
) : ViewModel() {
    private val _state = MutableStateFlow(
        HingeUiState(overlayDesired = overlayDesiredStore.isDesired()),
    )
    val state: StateFlow<HingeUiState> = _state
    private var smoother = FoldAngleSmoother.idle(180f)
    private var lastTarget: Float? = null

    init {
        viewModelScope.launch {
            hingeAngleSource.observe().collect { availability ->
                _state.update { it.copy(availability = availability) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                tickSmoother(SystemClock.uptimeMillis())
                delay(16L)
            }
        }
    }

    fun selectPreset(degrees: Float?) {
        OverlayInput.debugPresetDegrees.value = degrees
        _state.update { it.copy(debugPresetDegrees = degrees) }
    }

    fun setOverlayDesired(enabled: Boolean) {
        overlayDesiredStore.setDesired(enabled)
        _state.update { it.copy(overlayDesired = enabled) }
    }

    private fun tickSmoother(nowMs: Long) {
        val target = _state.value.rawAngle ?: return
        if (target != lastTarget) {
            smoother = if (lastTarget == null) {
                FoldAngleSmoother.idle(target)
            } else {
                smoother.retarget(target, nowMs)
            }
            lastTarget = target
        }
        val sample = smoother.sample(nowMs)
        _state.update { it.copy(smoothedAngle = sample) }
    }

    class Factory(
        private val hingeAngleSource: HingeAngleSource,
        private val overlayDesiredStore: OverlayDesiredStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HingeProbeViewModel(hingeAngleSource, overlayDesiredStore) as T
        }
    }
}
