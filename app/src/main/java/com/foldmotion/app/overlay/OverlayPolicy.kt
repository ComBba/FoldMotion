package com.foldmotion.app.overlay

enum class OverlayAttachResult {
    DISABLED,
    NEEDS_PERMISSION,
    ATTACHED,
}

object OverlayPolicy {
    fun resolve(enabled: Boolean, canDrawOverlays: Boolean): OverlayAttachResult {
        if (!enabled) return OverlayAttachResult.DISABLED
        if (!canDrawOverlays) return OverlayAttachResult.NEEDS_PERMISSION
        return OverlayAttachResult.ATTACHED
    }

    fun shouldDrawPreviewFx(overlayAttached: Boolean): Boolean {
        return !overlayAttached
    }
}
