package com.foldmotion.app.cover

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.foldmotion.app.hinge.CoverPowerPolicy
import com.foldmotion.app.ui.CoverHomeScreen
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.Executor
import org.lsposed.hiddenapibypass.HiddenApiBypass

class CoverScreenSession(
    private val activity: ComponentActivity,
) : DisplayManager.DisplayListener {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executor { command -> handler.post(command) }
    private val displayManager = activity.getSystemService(DisplayManager::class.java)
    private val deviceStates = DeviceStateInvoker(activity, executor) {
        handler.post {
            coverRequested = false
            if (CoverPowerPolicy.shouldPowerCover(lastProgress, currentlyOn = false) &&
                cancelRetries < 3
            ) {
                cancelRetries += 1
                Log.d(TAG, "retry cover request after cancel ($cancelRetries)")
                requestCoverDisplay()
            }
        }
    }
    private var presentation: Presentation? = null
    private var coverRequested = false
    private var lastProgress = 0f
    private var cancelRetries = 0

    init {
        displayManager.registerDisplayListener(this, handler)
    }

    fun setFoldProgress(progress: Float) {
        lastProgress = progress
        val wantCover = CoverPowerPolicy.shouldPowerCover(progress, coverRequested)
        if (wantCover) {
            requestCoverDisplay()
            showPresentationIfPossible()
        } else {
            cancelRetries = 0
            releaseCover()
        }
    }

    fun onHostFocused() {
        if (CoverPowerPolicy.shouldPowerCover(lastProgress, coverRequested)) {
            requestCoverDisplay()
            showPresentationIfPossible()
        }
    }

    fun release() {
        displayManager.unregisterDisplayListener(this)
        releaseCover()
    }

    override fun onDisplayAdded(displayId: Int) {
        logDisplays("added:$displayId")
        if (CoverPowerPolicy.shouldPowerCover(lastProgress, currentlyOn = true)) {
            showPresentationIfPossible()
        }
    }

    override fun onDisplayRemoved(displayId: Int) {
        if (presentation?.display?.displayId == displayId) {
            dismissPresentation()
        }
    }

    override fun onDisplayChanged(displayId: Int) {
        logDisplays("changed:$displayId")
        if (CoverPowerPolicy.shouldPowerCover(lastProgress, currentlyOn = coverRequested)) {
            showPresentationIfPossible()
        }
    }

    private fun requestCoverDisplay() {
        if (coverRequested) return
        coverRequested = deviceStates.requestConcurrentOuter()
        if (coverRequested) {
            listOf(80L, 250L, 600L, 1400L).forEach { delayMs ->
                handler.postDelayed({ showPresentationIfPossible() }, delayMs)
            }
        }
    }

    private fun releaseCover() {
        dismissPresentation()
        if (!coverRequested) return
        deviceStates.cancel()
        coverRequested = false
    }

    private fun showPresentationIfPossible() {
        val display = findCoverDisplay() ?: return
        if (presentation?.display?.displayId == display.displayId) return
        dismissPresentation()
        try {
            Log.d(TAG, "showing cover presentation on display=${display.displayId} ${display.mode.physicalWidth}x${display.mode.physicalHeight}")
            val next = CoverPresentation(activity, display)
            next.show()
            presentation = next
            Log.d(TAG, "cover presentation shown on display=${display.displayId}")
        } catch (error: Throwable) {
            Log.w(TAG, "cover presentation failed", error)
        }
    }

    private fun dismissPresentation() {
        presentation?.dismiss()
        presentation = null
    }

    private fun findCoverDisplay(): Display? {
        return displayManager.displays
            .filter { it.state == Display.STATE_ON }
            .filter { it.displayId != Display.DEFAULT_DISPLAY }
            .filter(::isCoverPanel)
            .maxByOrNull { it.mode.physicalHeight * it.mode.physicalWidth }
    }

    private fun isCoverPanel(display: Display): Boolean {
        val width = display.mode.physicalWidth
        val height = display.mode.physicalHeight
        val shortSide = minOf(width, height)
        val longSide = maxOf(width, height)
        return shortSide in 1000..1400 && longSide in 1800..2200
    }

    private fun logDisplays(reason: String) {
        displayManager.displays.forEach { display ->
            Log.d(
                TAG,
                "display[$reason] id=${display.displayId} state=${display.state} " +
                    "${display.mode.physicalWidth}x${display.mode.physicalHeight}",
            )
        }
    }

    private companion object {
        const val TAG = "FoldMotion"
    }
}

internal class DeviceStateInvoker(
    context: Context,
    private val executor: Executor,
    private val onRequestCanceled: () -> Unit,
) {
    private val service: Any? = resolveService(context)

    fun requestConcurrentOuter(): Boolean {
        val manager = service
        if (manager == null) {
            Log.w(TAG, "device_state service missing")
            return false
        }
        val targetState = findConcurrentState(manager)
        if (targetState == null) {
            Log.w(TAG, "no concurrent cover device state")
            return false
        }
        Log.d(TAG, "requesting $targetState")
        return try {
            if (invokeRequestState(manager, targetState)) {
                Log.d(TAG, "requested concurrent cover state")
                true
            } else {
                Log.w(TAG, "no matching requestState overload for $targetState")
                false
            }
        } catch (error: Throwable) {
            Log.w(TAG, "cannot request cover device state", error)
            false
        }
    }

    fun cancel() {
        val manager = service ?: return
        try {
            methodsNamed(manager.javaClass, "cancelStateRequest")
                .firstOrNull { it.parameterTypes.isEmpty() }
                ?.invoke(manager)
            Log.d(TAG, "released concurrent cover state")
        } catch (error: Throwable) {
            Log.w(TAG, "cannot cancel cover device state", error)
        }
    }

    private fun invokeRequestState(manager: Any, targetState: Any): Boolean {
        dumpMethods(manager.javaClass, "requestState", "cancelStateRequest", "getSupportedDeviceStates")
        val requestClass = loadClass("android.hardware.devicestate.DeviceStateRequest")
        if (requestClass != null) {
            dumpMethods(requestClass, "newBuilder", "build")
        }
        val overloads = methodsNamed(manager.javaClass, "requestState")
        overloads.forEach { method ->
            Log.d(TAG, "requestState(${method.parameterTypes.joinToString { it.name }})")
        }
        for (method in overloads) {
            val args = bindRequestArgs(method, targetState, requestClass) ?: continue
            try {
                method.isAccessible = true
                method.invoke(manager, *args)
                Log.d(TAG, "invoked requestState(${method.parameterTypes.joinToString { it.simpleName }})")
                return true
            } catch (error: Throwable) {
                val cause = error.cause ?: error
                Log.w(TAG, "requestState(${method.parameterTypes.joinToString { it.simpleName }}) failed", cause)
            }
        }
        return false
    }

    private fun bindRequestArgs(
        method: java.lang.reflect.Method,
        targetState: Any,
        requestClass: Class<*>?,
    ): Array<Any?>? {
        val params = method.parameterTypes
        if (params.isEmpty()) return null
        val args = arrayOfNulls<Any>(params.size)
        for (index in params.indices) {
            val param = params[index]
            args[index] = when {
                param.isInstance(targetState) -> targetState
                param == Executor::class.java -> executor
                param.isInterface -> callbackProxy(param)
                requestClass != null && param.isAssignableFrom(requestClass) ->
                    buildRequest(requestClass, targetState) ?: return null
                param == Integer.TYPE || param == Integer::class.java -> identifierOf(targetState) ?: return null
                else -> return null
            }
        }
        return args
    }

    private fun buildRequest(requestClass: Class<*>, targetState: Any): Any? {
        val builders = methodsNamed(requestClass, "newBuilder")
        builders.forEach { method ->
            Log.d(TAG, "newBuilder(${method.parameterTypes.joinToString { it.simpleName }})")
        }
        for (method in builders) {
            val params = method.parameterTypes
            val args: Array<Any> = when {
                params.size == 1 && params[0].isInstance(targetState) -> arrayOf(targetState)
                params.size == 1 && (params[0] == Integer.TYPE || params[0] == Integer::class.java) -> {
                    val identifier = identifierOf(targetState) ?: continue
                    arrayOf(identifier)
                }
                else -> continue
            }
            try {
                method.isAccessible = true
                val builder = method.invoke(null, *args) ?: continue
                val request = methodsNamed(builder.javaClass, "build")
                    .firstOrNull { it.parameterTypes.isEmpty() }
                    ?.invoke(builder)
                if (request != null) {
                    Log.d(TAG, "built DeviceStateRequest via newBuilder(${params.joinToString { it.simpleName }})")
                    return request
                }
            } catch (error: Throwable) {
                Log.w(TAG, "newBuilder failed", error.cause ?: error)
            }
        }
        return null
    }

    private fun callbackProxy(callbackClass: Class<*>): Any {
        val handler = InvocationHandler { proxy, method, args ->
            when (method.name) {
                "equals" -> proxy === args?.getOrNull(0)
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "FoldMotionDeviceStateCallback"
                else -> {
                    Log.d(TAG, "ds-callback ${method.name}")
                    if (method.name.contains("Cancel", ignoreCase = true)) {
                        onRequestCanceled()
                    }
                    null
                }
            }
        }
        return Proxy.newProxyInstance(callbackClass.classLoader, arrayOf(callbackClass), handler)
    }

    private fun findConcurrentState(manager: Any): Any? {
        val states = invokeList(manager, "getSupportedDeviceStates")
            ?: invokeList(manager, "getSupportedStates")
            ?: return null
        states.forEach { Log.d(TAG, "supported $it") }
        return states.firstOrNull { state ->
            state != null && state.toString().contains("CONCURRENT_INNER", ignoreCase = true)
        } ?: states.firstOrNull { state ->
            state != null && state.toString().contains("CONCURRENT_OUTER", ignoreCase = true)
        }
    }

    private fun invokeList(manager: Any, methodName: String): List<*>? {
        return try {
            val method = methodsNamed(manager.javaClass, methodName)
                .firstOrNull { it.parameterTypes.isEmpty() }
                ?: manager.javaClass.getMethod(methodName)
            when (val result = method.invoke(manager)) {
                is List<*> -> result
                is IntArray -> result.map { it }
                is Array<*> -> result.toList()
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun identifierOf(state: Any): Int? {
        for (name in listOf("getIdentifier", "getId")) {
            try {
                val method = state.javaClass.methods.firstOrNull {
                    it.name == name && it.parameterTypes.isEmpty()
                } ?: continue
                return (method.invoke(state) as Number).toInt()
            } catch (_: Throwable) {
            }
        }
        return Regex("identifier=(\\d+)").find(state.toString())?.groupValues?.get(1)?.toInt()
    }

    private companion object {
        const val TAG = "FoldMotion"
        const val SERVICE_NAME = "device_state"

        fun resolveService(context: Context): Any? {
            context.getSystemService(SERVICE_NAME)?.let { return it }
            return try {
                val managerClass = Class.forName("android.hardware.devicestate.DeviceStateManager")
                Context::class.java.getMethod("getSystemService", Class::class.java)
                    .invoke(context, managerClass)
            } catch (_: Throwable) {
                null
            }
        }

        fun loadClass(name: String): Class<*>? {
            return try {
                Class.forName(name)
            } catch (_: Throwable) {
                null
            }
        }

        fun methodsNamed(type: Class<*>, name: String): List<Method> {
            val hidden = try {
                HiddenApiBypass.getDeclaredMethods(type)
                    .filterIsInstance<Method>()
                    .filter { it.name == name }
            } catch (_: Throwable) {
                emptyList()
            }
            if (hidden.isNotEmpty()) return hidden
            return generateSequence(type) { it.superclass }
                .flatMap { it.declaredMethods.asSequence() + it.methods.asSequence() }
                .filter { it.name == name }
                .distinct()
                .toList()
        }

        fun dumpMethods(type: Class<*>, vararg names: String) {
            names.forEach { name ->
                methodsNamed(type, name).forEach { method ->
                    Log.d(
                        TAG,
                        "${type.simpleName}.${method.name}(${method.parameterTypes.joinToString { it.simpleName }})",
                    )
                }
            }
        }
    }
}

private class CoverPresentation(
    private val host: ComponentActivity,
    display: Display,
) : Presentation(host, display) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
        )
        val themed = android.view.ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_NoActionBar,
        )
        val view = ComposeView(themed)
        view.setViewTreeLifecycleOwner(host)
        view.setViewTreeViewModelStoreOwner(host)
        view.setViewTreeSavedStateRegistryOwner(host)
        view.setContent { CoverHomeScreen() }
        setContentView(view)
    }
}
