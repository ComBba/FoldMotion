package com.foldmotion.app.overlay

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class OverlayPolicyTest {

    @Test
    fun staysOffUntilUserEnables() {
        assertThat(
            OverlayPolicy.resolve(enabled = false, canDrawOverlays = true),
        ).isEqualTo(OverlayAttachResult.DISABLED)
    }

    @Test
    fun asksForPermissionWhenEnabledWithoutGrant() {
        assertThat(
            OverlayPolicy.resolve(enabled = true, canDrawOverlays = false),
        ).isEqualTo(OverlayAttachResult.NEEDS_PERMISSION)
    }

    @Test
    fun attachesWhenEnabledAndGranted() {
        assertThat(
            OverlayPolicy.resolve(enabled = true, canDrawOverlays = true),
        ).isEqualTo(OverlayAttachResult.ATTACHED)
    }

    @Test
    fun previewFxIsSkippedWhileSystemOverlayIsAttached() {
        assertThat(OverlayPolicy.shouldDrawPreviewFx(overlayAttached = true)).isFalse()
        assertThat(OverlayPolicy.shouldDrawPreviewFx(overlayAttached = false)).isTrue()
    }

    @Test
    fun foregroundServiceFollowsAttachPolicy() {
        assertThat(OverlayPolicy.shouldRunForegroundService(enabled = true, canDrawOverlays = true)).isTrue()
        assertThat(OverlayPolicy.shouldRunForegroundService(enabled = true, canDrawOverlays = false)).isFalse()
        assertThat(OverlayPolicy.shouldRunForegroundService(enabled = false, canDrawOverlays = true)).isFalse()
    }
}
