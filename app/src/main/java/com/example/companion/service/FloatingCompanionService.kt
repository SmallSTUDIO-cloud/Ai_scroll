package com.example.companion.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
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
import com.example.MainActivity
import com.example.R
import com.example.companion.FloatingCompanionController
import com.example.companion.ui.CompanionAvatarBubble
import com.example.companion.ui.CompactOverlayPopup
import com.example.companion.ui.PopOutComicBubble
import com.example.ui.theme.AiBreakTheme
import kotlin.math.hypot

class FloatingCompanionService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val appViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = appViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "ai_break_floating_companion"
        private const val NOTIFICATION_ID = 2026
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startInForeground()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        initOverlay()
        FloatingCompanionController.setServiceRunning(true)
    }

    private fun startInForeground() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AI BREAK Companion Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the active AI BREAK companion floating bubble over apps"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AI BREAK Companion Active")
            .setContentText("Hovering over other apps with live reel perception.")
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun initOverlay() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val metrics: DisplayMetrics = resources.displayMetrics
        val screenWidth = metrics.widthPixels

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = (screenWidth - 250).coerceAtLeast(20)
            y = 350
        }
        windowParams = params

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@FloatingCompanionService)
            setViewTreeViewModelStoreOwner(this@FloatingCompanionService)
            setViewTreeSavedStateRegistryOwner(this@FloatingCompanionService)

            setContent {
                AiBreakTheme {
                    val state by FloatingCompanionController.companionState.collectAsState()
                    val provider by FloatingCompanionController.selectedProvider.collectAsState()
                    val isPopupOpen by FloatingCompanionController.isOverlayPopupVisible.collectAsState()
                    val activeComicThought by FloatingCompanionController.activeComicThought.collectAsState()
                    val isComicBubbleVisible by FloatingCompanionController.isComicBubbleVisible.collectAsState()
                    val activeEmoji by FloatingCompanionController.activeEmojiReaction.collectAsState()

                    Column(
                        modifier = Modifier.wrapContentSize(),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Pop-out comic speech bubble right above/beside avatar
                        PopOutComicBubble(
                            thought = activeComicThought,
                            visible = isComicBubbleVisible && !isPopupOpen,
                            onClick = {
                                FloatingCompanionController.toggleOverlayPopup()
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // The Circular Avatar Bubble with expression emoji badge
                        CompanionAvatarBubble(
                            state = state,
                            providerLabel = provider.displayName,
                            activeEmoji = activeEmoji,
                            onClick = {
                                FloatingCompanionController.toggleOverlayPopup()
                            }
                        )

                        // Compact In-Place Floating Popup right under the avatar
                        AnimatedVisibility(
                            visible = isPopupOpen,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                CompactOverlayPopup(
                                    onDismiss = {
                                        FloatingCompanionController.setOverlayPopupVisible(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Free-floating drag listener without edge snapping
        composeView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isClick = true

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        return false // Let click pass to composables if it's a tap
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (hypot(dx.toDouble(), dy.toDouble()) > 14) {
                            isClick = false
                            params.x = initialX + dx
                            params.y = initialY + dy
                            try {
                                windowManager.updateViewLayout(composeView, params)
                            } catch (_: Exception) {}
                            return true
                        }
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isClick) {
                            // Free-floating: stays exactly where the user released it!
                            return true
                        }
                        return false
                    }
                }
                return false
            }
        })

        overlayView = composeView
        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        appViewModelStore.clear()

        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
        overlayView = null
        FloatingCompanionController.setServiceRunning(false)
        FloatingCompanionController.setOverlayPopupVisible(false)
    }
}
