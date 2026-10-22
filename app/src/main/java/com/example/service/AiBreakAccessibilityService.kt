package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.model.ExtractedReelInfo
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

        private val _latestExtractedReel = MutableStateFlow<ExtractedReelInfo?>(null)
        val latestExtractedReel: StateFlow<ExtractedReelInfo?> = _latestExtractedReel.asStateFlow()

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

        fun extractCurrentReel(): ExtractedReelInfo? {
            val service = instance ?: return null
            val info = service.extractVisibleReelContent()
            if (info != null) {
                _latestExtractedReel.value = info
            }
            return info
        }

        fun openComments(): Boolean {
            val service = instance ?: return false
            return service.findAndClickCommentButton()
        }

        fun readTopComments(): List<String> {
            val service = instance ?: return emptyList()
            return service.extractVisibleComments()
        }

        fun closeComments(): Boolean {
            val service = instance ?: return false
            return service.performCloseComments()
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
     * Inspects the active window's node hierarchy to read live caption, creator username,
     * and audio tags without needing screen-capture or video recording permissions.
     */
    fun extractVisibleReelContent(): ExtractedReelInfo? {
        val root = rootInActiveWindow ?: return null
        val currentPkg = _lastDetectedPackage.value ?: root.packageName?.toString() ?: "unknown"

        val appName = when {
            currentPkg.contains("instagram") -> "Instagram"
            currentPkg.contains("youtube") -> "YouTube Shorts"
            currentPkg.contains("musically") || currentPkg.contains("tiktok") -> "TikTok"
            currentPkg.contains("reddit") -> "Reddit"
            else -> "Social Feed"
        }

        val allTexts = mutableListOf<String>()
        val allNodeInfos = mutableListOf<NodeSnapshot>()
        traverseNodeTree(root, allTexts, allNodeInfos)

        if (allTexts.isEmpty()) {
            return ExtractedReelInfo(
                packageName = currentPkg,
                appName = appName,
                caption = "Browsing $appName Reels...",
                rawVisibleTexts = emptyList()
            )
        }

        var creator: String? = null
        var caption: String? = null
        var audioTitle: String? = null

        // 1. Scan for creator name
        for (snap in allNodeInfos) {
            val txt = snap.text?.trim() ?: continue
            val id = snap.viewId?.lowercase() ?: ""
            val desc = snap.contentDescription?.lowercase() ?: ""

            if (creator == null) {
                if (id.contains("author") || id.contains("username") || id.contains("user_name") ||
                    id.contains("channel_name") || id.contains("reel_channel") || desc.contains("profile")
                ) {
                    creator = txt.removePrefix("@").take(28)
                } else if (txt.startsWith("@") && txt.length in 3..30 && !txt.contains(" ")) {
                    creator = txt.removePrefix("@")
                }
            }

            // 2. Scan for audio title
            if (audioTitle == null) {
                if (id.contains("audio") || id.contains("music") || id.contains("sound") ||
                    txt.contains("Original audio", ignoreCase = true) || txt.contains("♫") ||
                    desc.contains("audio") || desc.contains("music")
                ) {
                    audioTitle = txt.take(45)
                }
            }

            // 3. Scan for main caption
            if (caption == null) {
                if (id.contains("caption") || id.contains("reel_title") || id.contains("video_title") ||
                    id.contains("description")
                ) {
                    caption = txt
                }
            }
        }

        // Fallback for caption: find longest descriptive text that isn't a button label
        if (caption == null) {
            val candidateTexts = allTexts.filter { text ->
                val lower = text.lowercase()
                text.length > 15 &&
                        !lower.contains("follow") &&
                        !lower.contains("subscribe") &&
                        !lower.contains("share") &&
                        !lower.contains("remix") &&
                        !lower.contains("reply")
            }
            if (candidateTexts.isNotEmpty()) {
                caption = candidateTexts.maxByOrNull { it.length }
            }
        }

        val fullTextCorp = (allTexts.joinToString(" ") + " " + (caption ?: "")).lowercase()

        // Topic detection heuristics (including user requested attraction to cute/beautiful girls)
        val isLikelyGirlOrAesthetic = fullTextCorp.contains("girl") ||
                fullTextCorp.contains("cute") ||
                fullTextCorp.contains("beautiful") ||
                fullTextCorp.contains("pretty") ||
                fullTextCorp.contains("aesthetic") ||
                fullTextCorp.contains("model") ||
                fullTextCorp.contains("outfit") ||
                fullTextCorp.contains("cosplay") ||
                fullTextCorp.contains("makeup") ||
                fullTextCorp.contains("dance") ||
                fullTextCorp.contains("fashion")

        val isHumorOrMeme = fullTextCorp.contains("funny") ||
                fullTextCorp.contains("meme") ||
                fullTextCorp.contains("lol") ||
                fullTextCorp.contains("lmao") ||
                fullTextCorp.contains("joke") ||
                fullTextCorp.contains("prank") ||
                fullTextCorp.contains("fails") ||
                fullTextCorp.contains("wait for it")

        val isTechOrCoding = fullTextCorp.contains("code") ||
                fullTextCorp.contains("coding") ||
                fullTextCorp.contains("developer") ||
                fullTextCorp.contains("python") ||
                fullTextCorp.contains("javascript") ||
                fullTextCorp.contains("android") ||
                fullTextCorp.contains("ai") ||
                fullTextCorp.contains("tech") ||
                fullTextCorp.contains("setup")

        val isMusicOrDance = fullTextCorp.contains("music") ||
                fullTextCorp.contains("song") ||
                fullTextCorp.contains("dance") ||
                fullTextCorp.contains("beat") ||
                fullTextCorp.contains("singing")

        return ExtractedReelInfo(
            packageName = currentPkg,
            appName = appName,
            creator = creator,
            caption = caption,
            audioTitle = audioTitle,
            rawVisibleTexts = allTexts.take(8),
            isLikelyGirlOrAesthetic = isLikelyGirlOrAesthetic,
            isHumorOrMeme = isHumorOrMeme,
            isTechOrCoding = isTechOrCoding,
            isMusicOrDance = isMusicOrDance
        )
    }

    private data class NodeSnapshot(
        val text: String?,
        val contentDescription: String?,
        val viewId: String?
    )

    private fun traverseNodeTree(
        node: AccessibilityNodeInfo?,
        collectedTexts: MutableList<String>,
        collectedSnapshots: MutableList<NodeSnapshot>
    ) {
        if (node == null) return

        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()
        val viewId = node.viewIdResourceName

        if (!text.isNullOrBlank()) {
            collectedTexts.add(text)
        }
        if (text != null || desc != null || viewId != null) {
            collectedSnapshots.add(NodeSnapshot(text, desc, viewId))
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                traverseNodeTree(child, collectedTexts, collectedSnapshots)
                try {
                    child.recycle()
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Finds and triggers the comment action button on Instagram, YouTube Shorts, TikTok, or Reddit.
     * Uses node click first; if node is wrapped or non-clickable, computes its screen coordinate and dispatches a gesture tap.
     */
    fun findAndClickCommentButton(): Boolean {
        val root = rootInActiveWindow ?: return false
        val commentNode = findCommentButtonNode(root)

        if (commentNode != null) {
            // 1. Try accessibility action click
            if (commentNode.isClickable && commentNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }

            // 2. Try parent clickable
            var parent = commentNode.parent
            while (parent != null) {
                if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                parent = parent.parent
            }

            // 3. Fallback: coordinate tap on button bounds
            val rect = Rect()
            commentNode.getBoundsInScreen(rect)
            if (rect.width() > 0 && rect.height() > 0) {
                return dispatchTapCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
            }
        }

        // 4. Fallback coordinate for typical reels comment button on right rail (~88% width, ~68% height)
        val metrics: DisplayMetrics = resources.displayMetrics
        return dispatchTapCoordinates(metrics.widthPixels * 0.88f, metrics.heightPixels * 0.68f)
    }

    private fun findCommentButtonNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null

        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""

        val isCommentTarget = desc.contains("comment") ||
                desc.contains("view comments") ||
                viewId.contains("comment_button") ||
                viewId.contains("row_feed_button_comment") ||
                viewId.contains("comments_button") ||
                (text.contains("comment") && text.length < 15)

        if (isCommentTarget) {
            return node
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val found = findCommentButtonNode(child)
            if (found != null) {
                return found
            }
        }
        return null
    }

    /**
     * Extracts visible comment texts from an open comment sheet/drawer.
     */
    fun extractVisibleComments(): List<String> {
        val root = rootInActiveWindow ?: return emptyList()
        val allTexts = mutableListOf<String>()
        val snapshots = mutableListOf<NodeSnapshot>()
        traverseNodeTree(root, allTexts, snapshots)

        // Filter out button titles, status items, and short metadata
        val commentTexts = allTexts.filter { str ->
            val lower = str.lowercase().trim()
            str.length in 8..180 &&
                    !lower.contains("comments") &&
                    !lower.contains("add a comment") &&
                    !lower.contains("reply") &&
                    !lower.contains("likes") &&
                    !lower.contains("translate") &&
                    !lower.contains("share") &&
                    !lower.contains("report") &&
                    !lower.contains("view replies") &&
                    !lower.matches(Regex("^[0-9]+[a-z]?$")) // not pure numbers or "2h"
        }

        return commentTexts.take(6)
    }

    /**
     * Closes the open comment bottom sheet to return to the video feed.
     */
    fun performCloseComments(): Boolean {
        val root = rootInActiveWindow
        if (root != null) {
            val closeNode = findCloseNode(root)
            if (closeNode != null) {
                if (closeNode.isClickable && closeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    return true
                }
                val rect = Rect()
                closeNode.getBoundsInScreen(rect)
                if (rect.width() > 0 && rect.height() > 0) {
                    return dispatchTapCoordinates(rect.centerX().toFloat(), rect.centerY().toFloat())
                }
            }
        }

        // Global back gesture dismisses the comment bottom sheet cleanly on all Android apps
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    private fun findCloseNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""
        val id = node.viewIdResourceName?.lowercase() ?: ""
        if (desc.contains("close") || desc.contains("dismiss") || desc.contains("back") || id.contains("close") || id.contains("dismiss")) {
            return node
        }
        for (i in 0 until node.childCount) {
            val found = findCloseNode(node.getChild(i))
            if (found != null) return found
        }
        return null
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
        return dispatchTapCoordinates(tapX, tapY)
    }

    private fun dispatchTapCoordinates(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        return dispatchGesture(gesture, null, null)
    }
}
