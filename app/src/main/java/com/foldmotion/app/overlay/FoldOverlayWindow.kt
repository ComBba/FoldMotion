package com.foldmotion.app.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.foldmotion.app.R
import com.foldmotion.app.hinge.FoldFxParams
import com.foldmotion.app.ui.FoldFxOverlay
import kotlinx.coroutines.flow.MutableStateFlow

class FoldOverlayWindow(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val windowManager = appContext.getSystemService(WindowManager::class.java)
    private val paramsState = MutableStateFlow(FoldFxParams.fromProgress(0f))
    private var host: OverlayComposeHost? = null

    val isAttached: Boolean
        get() = host != null

    fun attach() {
        if (host != null) return
        val next = OverlayComposeHost(appContext, paramsState)
        windowManager.addView(next.view, overlayLayoutParams())
        host = next
    }

    fun detach() {
        val current = host ?: return
        host = null
        runCatching { windowManager.removeViewImmediate(current.view) }
        current.destroy()
    }

    fun setParams(params: FoldFxParams) {
        paramsState.value = params
    }

    private fun overlayLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            title = OVERLAY_TITLE
            alpha = maxTouchThroughAlpha(appContext)
        }
    }

    companion object {
        const val OVERLAY_TITLE = "FoldMotionOverlay"

        internal fun maxTouchThroughAlpha(context: Context): Float {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 1f
            return context.getSystemService(InputManager::class.java)
                .maximumObscuringOpacityForTouch
        }
    }
}

private class OverlayComposeHost(
    context: Context,
    paramsState: MutableStateFlow<FoldFxParams>,
) : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore
        get() = store
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    val view: ComposeView

    init {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        val themed = ContextThemeWrapper(context, R.style.Theme_FoldMotion)
        view = ComposeView(themed).apply {
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            setViewTreeLifecycleOwner(this@OverlayComposeHost)
            setViewTreeViewModelStoreOwner(this@OverlayComposeHost)
            setViewTreeSavedStateRegistryOwner(this@OverlayComposeHost)
            setContent {
                val params by paramsState.collectAsState()
                FoldFxOverlay(params = params)
            }
        }
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        if (lifecycleRegistry.currentState != Lifecycle.State.DESTROYED) {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
        store.clear()
    }
}
