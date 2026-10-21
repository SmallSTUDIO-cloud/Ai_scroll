package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RealDeviceConfig(
    val isEnabled: Boolean = false,
    val targetPackage: String = "com.instagram.android",
    val targetAppName: String = "Instagram",
    val maxDurationMinutes: Int = 5,
    val scrollCooldownSec: Int = 6,
    val isSessionActive: Boolean = false,
    val scrollCount: Int = 0
)

class RealDeviceController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var scrollLoopJob: Job? = null

    private val _config = MutableStateFlow(RealDeviceConfig())
    val config: StateFlow<RealDeviceConfig> = _config.asStateFlow()

    fun updateTargetApp(packageName: String, appName: String) {
        _config.value = _config.value.copy(
            targetPackage = packageName,
            targetAppName = appName
        )
    }

    fun updateDuration(minutes: Int) {
        _config.value = _config.value.copy(maxDurationMinutes = minutes.coerceIn(1, 15))
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            intent != null
        } catch (_: Exception) {
            false
        }
    }

    fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open Accessibility settings directly.", Toast.LENGTH_SHORT).show()
        }
    }

    fun startSession(): Boolean {
        if (!AiBreakAccessibilityService.isServiceEnabled.value) {
            openAccessibilitySettings()
            Toast.makeText(context, "Please enable 'AI BREAK Scroll Agent' in Accessibility to allow scrolling.", Toast.LENGTH_LONG).show()
            return false
        }

        val launched = launchTargetApp()
        if (!launched) return false

        _config.value = _config.value.copy(isSessionActive = true, scrollCount = 0)

        scrollLoopJob?.cancel()
        scrollLoopJob = scope.launch {
            val maxSeconds = _config.value.maxDurationMinutes * 60
            val interval = _config.value.scrollCooldownSec
            var elapsed = 0

            // Initial wait for the external app to render its feed
            delay(3500)

            while (_config.value.isSessionActive && elapsed < maxSeconds) {
                val scrolled = AiBreakAccessibilityService.performScrollUpGesture()
                if (scrolled) {
                    val newCount = _config.value.scrollCount + 1
                    _config.value = _config.value.copy(scrollCount = newCount)
                }
                delay(interval * 1000L)
                elapsed += interval
            }

            stopSession()
        }
        return true
    }

    fun stopSession() {
        scrollLoopJob?.cancel()
        scrollLoopJob = null
        if (_config.value.isSessionActive) {
            _config.value = _config.value.copy(isSessionActive = false)
            Toast.makeText(context, "AI BREAK session completed (${_config.value.scrollCount} reels scrolled).", Toast.LENGTH_SHORT).show()
        }
    }

    fun testScrollOnce(): Boolean {
        if (!AiBreakAccessibilityService.isServiceEnabled.value) {
            Toast.makeText(context, "Accessibility service is not enabled. Tap 'Enable Accessibility' first.", Toast.LENGTH_SHORT).show()
            return false
        }
        val success = AiBreakAccessibilityService.performScrollUpGesture()
        if (success) {
            Toast.makeText(context, "Scroll gesture dispatched successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Failed to dispatch scroll gesture.", Toast.LENGTH_SHORT).show()
        }
        return success
    }

    fun launchTargetApp(): Boolean {
        val targetPkg = _config.value.targetPackage
        val appName = _config.value.targetAppName

        // 1. Try launching native app via standard launch intent
        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPkg)
        if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                Toast.makeText(context, "Launching $appName for AI Scroll session...", Toast.LENGTH_SHORT).show()
                return true
            } catch (e: Exception) {
                // Fallthrough to deep link
            }
        }

        // 2. Try specific deep link URL with explicit package
        val deepLinkUrl = when (targetPkg) {
            "com.instagram.android" -> "https://www.instagram.com/reels/"
            "com.google.android.youtube" -> "https://www.youtube.com/shorts"
            "com.reddit.frontpage" -> "https://www.reddit.com/r/popular/"
            "com.zhiliaoapp.musically" -> "https://www.tiktok.com/"
            else -> "https://www.google.com"
        }

        val deepIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl)).apply {
            setPackage(targetPkg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            if (context.packageManager.resolveActivity(deepIntent, 0) != null) {
                context.startActivity(deepIntent)
                Toast.makeText(context, "Opening $appName Reels feed...", Toast.LENGTH_SHORT).show()
                return true
            }
        } catch (_: Exception) {}

        // 3. Fallback: App is not installed on this device (e.g. fresh phone or emulator)
        Toast.makeText(
            context,
            "$appName is not installed. Opening browser feed instead.",
            Toast.LENGTH_LONG
        ).show()

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLinkUrl)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(browserIntent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open feed: $appName not available.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
