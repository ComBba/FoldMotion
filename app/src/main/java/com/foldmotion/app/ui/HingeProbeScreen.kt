package com.foldmotion.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foldmotion.app.hinge.HingeUiState
import com.foldmotion.app.overlay.OverlayPolicy

@Composable
fun HingeProbeRoute(
    viewModel: HingeProbeViewModel,
    overlayAttached: Boolean,
    canDrawOverlays: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HingeProbeScreen(
        state = state,
        overlayAttached = overlayAttached,
        canDrawOverlays = canDrawOverlays,
        onSelectPreset = viewModel::selectPreset,
        onToggleOverlay = onToggleOverlay,
        modifier = modifier,
    )
}

@Composable
fun HingeProbeScreen(
    state: HingeUiState,
    overlayAttached: Boolean,
    canDrawOverlays: Boolean,
    onSelectPreset: (Float?) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fx = state.fx
    Box(modifier = modifier.fillMaxSize()) {
        FakeHomeGrid(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    cameraDistance = 18f * density
                    scaleX = fx.scale
                    scaleY = fx.scale
                    rotationY = fx.rotationY
                    alpha = fx.contentAlpha
                },
        )
        if (OverlayPolicy.shouldDrawPreviewFx(overlayAttached)) {
            FoldFxOverlay(params = fx)
        }
        if (fx.coverReveal > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = fx.coverReveal },
                contentAlignment = Alignment.Center,
            ) {
                CoverHomeScreen(
                    modifier = Modifier
                        .padding(horizontal = 72.dp, vertical = 36.dp)
                        .clip(RoundedCornerShape(36.dp)),
                )
            }
        }
        DebugHud(
            state = state,
            overlayAttached = overlayAttached,
            canDrawOverlays = canDrawOverlays,
            onSelectPreset = onSelectPreset,
            onToggleOverlay = onToggleOverlay,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@Composable
private fun DebugHud(
    state: HingeUiState,
    overlayAttached: Boolean,
    canDrawOverlays: Boolean,
    onSelectPreset: (Float?) -> Unit,
    onToggleOverlay: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = Color(0xE6121828),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "FoldMotion M3",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = formatHinge(state.displayedAngle),
                modifier = Modifier.testTag("hinge_angle"),
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "SENSOR ${state.sensorLabel}  RAW ${formatShort(state.rawAngle)}  P ${state.progress?.let { "%.2f".format(it) } ?: "--"}",
                color = Color.White.copy(alpha = 0.78f),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = overlayStatus(overlayAttached, canDrawOverlays, state.overlayDesired),
                modifier = Modifier.testTag("overlay_status"),
                color = Color.White.copy(alpha = 0.78f),
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Switch(
                    checked = state.overlayDesired,
                    onCheckedChange = onToggleOverlay,
                )
                Text(
                    text = "상주 Overlay",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.debugPresetDegrees == null,
                    onClick = { onSelectPreset(null) },
                    label = { Text("Live") },
                )
                HingeUiState.DEBUG_PRESETS.forEach { degrees ->
                    FilterChip(
                        selected = state.debugPresetDegrees == degrees,
                        onClick = { onSelectPreset(degrees) },
                        label = { Text("${degrees.toInt()}°") },
                    )
                }
            }
        }
    }
}

private fun overlayStatus(
    overlayAttached: Boolean,
    canDrawOverlays: Boolean,
    overlayDesired: Boolean,
): String {
    return when {
        overlayAttached -> "FGS ON  TOUCH-THROUGH"
        overlayDesired && !canDrawOverlays -> "OVERLAY 권한 필요"
        overlayDesired -> "OVERLAY 대기"
        else -> "OVERLAY OFF"
    }
}

private fun formatHinge(angle: Float?): String {
    return if (angle == null) {
        "HINGE: --.-°"
    } else {
        "HINGE: %.1f°".format(angle)
    }
}

private fun formatShort(angle: Float?): String {
    return angle?.let { "%.0f°".format(it) } ?: "--"
}
