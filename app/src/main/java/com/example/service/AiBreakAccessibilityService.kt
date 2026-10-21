package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class AiBreakAccessibilityService : AccessibilityService() {

    companion object {
        private val _isServiceEnabled = MutableStateFlow(false)
        val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()

        private val _lastDetectedPackage = MutableStateFlow<String?>(null)
        val lastDetectedPackage: StateFlow<String?> = _lastDetectedPackage.asStateFlow()

        var instance: AiBreakAccessibilityService? = null
            private set

        fun performScrollUpGesture(): Boolean {
            val service = instance ?: return false
            return service.dispatchHumanScrollUp()
        }

        fun performDoubleTapLike(): Boolean {
            val service = instance ?: return false
            return service.dispatchDoubleTapLike()
        }

        fun performSingleTap(xPercent: Float = 0.5f, yPercent: Float = 0.5f): Boolean {
            val service = instance ?: return false
            return service.dispatchSingleTap(xPercent, yPercent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isServiceEnabled.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString()
        if (!pkg.isNullOrBlank() && pkg != packageName) {
            _lastDetectedPackage.value = pkg
        }
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by system
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _isServiceEnabled.value = false
    }

    /**
     * Dispatches a realistic human-like upward swipe gesture with slight organic jitter and curve
     */
    private fun dispatchHumanScrollUp(): Boolean {
        val metrics: DisplayMetrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()

        // Human thumb swipe geometry: slight right-to-left organic curve
        val startXJitter = Random.nextFloat() * 40f - 20f
        val startX = (width * 0.52f) + startXJitter
        val startY = height * (0.74f + Random.nextFloat() * 0.05f)

        val controlX = (width * 0.46f) + (Random.nextFloat() * 20f - 10f)
        val controlY = height * 0.50f

        val endX = (width * 0.49f) + (Random.nextFloat() * 30f - 15f)
        val endY = height * (0.24f + Random.nextFloat() * 0.05f)

        val path = Path().apply {
            moveTo(startX, startY)
            quadTo(controlX, controlY, endX, endY)
        }

        val duration = Random.nextLong(280, 380)
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        return dispatchGesture(gesture, null, null)
    }

    /**
     * Dispatches double tap in the center of the viewport to like reels on Instagram / Shorts / TikTok
     */
    private fun dispatchDoubleTapLike(): Boolean {
        val metrics: DisplayMetrics = resources.displayMetrics
        val centerX = metrics.widthPixels * 0.5f
        val centerY = metrics.heightPixels * 0.5f

        val path1 = Path().apply { moveTo(centerX, centerY) }
        val path2 = Path().apply { moveTo(centerX + 2f, centerY + 2f) }

        val stroke1 = GestureDescription.StrokeDescription(path1, 0, 60)
        val stroke2 = GestureDescription.StrokeDescription(path2, 160, 60)

        val gesture = GestureDescription.Builder()
            .addStroke(stroke1)
            .addStroke(stroke2)
            .build()

        return dispatchGesture(gesture, null, null)
    }

    private fun dispatchSingleTap(xPercent: Float, yPercent: Float): Boolean {
        val metrics: DisplayMetrics = resources.displayMetrics
        val tapX = metrics.widthPixels * xPercent.coerceIn(0.05f, 0.95f)
        val tapY = metrics.heightPixels * yPercent.coerceIn(0.05f, 0.95f)

        val path = Path().apply { moveTo(tapX, tapY) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()

        return dispatchGesture(gesture, null, null)
    }
}
