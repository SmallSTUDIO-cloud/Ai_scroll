package com.example.service

import android.content.Context
import android.content.Intent
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RealDeviceConfig(
    val isEnabled: Boolean = false,
    val targetPackage: String = "com.instagram.android",
    val targetAppName: String = "Instagram",
    val maxDurationMinutes: Int = 5,
    val scrollCooldownSec: Int = 6,
    val isSessionActive: Boolean = false
)

class RealDeviceController(private val context: Context) {

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

    fun setSessionActive(active: Boolean) {
        _config.value = _config.value.copy(isSessionActive = active)
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun launchTargetApp() {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(_config.value.targetPackage)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
