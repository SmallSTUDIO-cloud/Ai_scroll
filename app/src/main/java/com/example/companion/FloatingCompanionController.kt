package com.example.companion

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.example.companion.engine.ConversationBrain
import com.example.companion.engine.HeuristicConversationBrain
import com.example.companion.engine.RemoteModelBrain
import com.example.companion.model.BrainProvider
import com.example.companion.model.CompanionMessage
import com.example.companion.model.CompanionState
import com.example.companion.model.MessageSender
import com.example.companion.service.FloatingCompanionService
import com.example.companion.ui.BubbleAccent
import com.example.companion.ui.ComicThought
import com.example.model.ExtractedReelInfo
import com.example.model.PersonalityProfile
import com.example.service.AiBreakAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

object FloatingCompanionController {

    private val scope = CoroutineScope(Dispatchers.Main)
    private var comicBubbleDismissJob: Job? = null
    private var commentInspectionJob: Job? = null

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _companionState = MutableStateFlow(CompanionState.IDLE)
    val companionState: StateFlow<CompanionState> = _companionState.asStateFlow()

    private val _selectedProvider = MutableStateFlow(BrainProvider.HEURISTIC_LOCAL)
    val selectedProvider: StateFlow<BrainProvider> = _selectedProvider.asStateFlow()

    private val _inAppBubbleEnabled = MutableStateFlow(true)
    val inAppBubbleEnabled: StateFlow<Boolean> = _inAppBubbleEnabled.asStateFlow()

    private val _isOverlayPopupVisible = MutableStateFlow(false)
    val isOverlayPopupVisible: StateFlow<Boolean> = _isOverlayPopupVisible.asStateFlow()

    private val _currentThought = MutableStateFlow("Exploring the algorithmic feed like a human...")
    val currentThought: StateFlow<String> = _currentThought.asStateFlow()

    // Lively Pop-Out Comic Bubble & Emoji state
    private val _activeComicThought = MutableStateFlow<ComicThought?>(null)
    val activeComicThought: StateFlow<ComicThought?> = _activeComicThought.asStateFlow()

    private val _isComicBubbleVisible = MutableStateFlow(false)
    val isComicBubbleVisible: StateFlow<Boolean> = _isComicBubbleVisible.asStateFlow()

    private val _activeEmojiReaction = MutableStateFlow<String?>("✨")
    val activeEmojiReaction: StateFlow<String?> = _activeEmojiReaction.asStateFlow()

    private val _lastExtractedReel = MutableStateFlow<ExtractedReelInfo?>(null)
    val lastExtractedReel: StateFlow<ExtractedReelInfo?> = _lastExtractedReel.asStateFlow()

    private val _isProcrastinating = MutableStateFlow(false)
    val isProcrastinating: StateFlow<Boolean> = _isProcrastinating.asStateFlow()

    private val _isReadingComments = MutableStateFlow(false)
    val isReadingComments: StateFlow<Boolean> = _isReadingComments.asStateFlow()

    // Counters for human-like cadence (~1 like every 8-12 reels, ~1 comment read every 10-15 reels)
    private var reelsSinceLastLike = 0
    private var targetLikeInterval = Random.nextInt(8, 12)

    private var reelsSinceLastComment = 0
    private var targetCommentInterval = Random.nextInt(10, 15)

    private val _messages = MutableStateFlow<List<CompanionMessage>>(
        listOf(
            CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Hello! I am your AI BREAK companion. I watch reels with you, read captions, and give live feedback!"
            )
        )
    )
    val messages: StateFlow<List<CompanionMessage>> = _messages.asStateFlow()

    private val _currentScreenContext = MutableStateFlow<String?>("Trending Social Feed")
    val currentScreenContext: StateFlow<String?> = _currentScreenContext.asStateFlow()

    private val _isSheetExpanded = MutableStateFlow(false)
    val isSheetExpanded: StateFlow<Boolean> = _isSheetExpanded.asStateFlow()

    private var activeProfile: PersonalityProfile = PersonalityProfile.PRESET_TECH_NERD

    private var heuristicBrain: ConversationBrain = HeuristicConversationBrain()
    private var activeBrain: ConversationBrain = heuristicBrain

    fun updateActiveProfile(profile: PersonalityProfile) {
        activeProfile = profile
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun setCompanionState(state: CompanionState) {
        _companionState.value = state
    }

    fun toggleSheetExpanded() {
        _isSheetExpanded.value = !_isSheetExpanded.value
    }

    fun setSheetExpanded(expanded: Boolean) {
        _isSheetExpanded.value = expanded
    }

    fun toggleOverlayPopup() {
        _isOverlayPopupVisible.value = !_isOverlayPopupVisible.value
        if (_isOverlayPopupVisible.value) {
            // Dismiss comic bubble when full popup opens
            _isComicBubbleVisible.value = false
        }
    }

    fun setOverlayPopupVisible(visible: Boolean) {
        _isOverlayPopupVisible.value = visible
        if (visible) {
            _isComicBubbleVisible.value = false
        }
    }

    fun updateThought(thought: String) {
        _currentThought.value = thought
    }

    fun setInAppBubbleEnabled(enabled: Boolean) {
        _inAppBubbleEnabled.value = enabled
    }

    fun updateScreenContext(contextSnippet: String?) {
        _currentScreenContext.value = contextSnippet
    }

    fun showComicBubble(
        text: String,
        emoji: String = "👀",
        tag: String = "REEL BOT",
        accent: BubbleAccent = BubbleAccent.CYAN,
        durationMs: Long = 3800L
    ) {
        val thought = ComicThought(
            text = text,
            emoji = emoji,
            tag = tag,
            accentType = accent
        )
        _activeComicThought.value = thought
        _activeEmojiReaction.value = emoji
        _currentThought.value = text
        _isComicBubbleVisible.value = true

        comicBubbleDismissJob?.cancel()
        comicBubbleDismissJob = scope.launch {
            delay(durationMs)
            _isComicBubbleVisible.value = false
        }
    }

    fun dismissComicBubble() {
        comicBubbleDismissJob?.cancel()
        _isComicBubbleVisible.value = false
    }

    fun setProvider(provider: BrainProvider, userApiKey: String? = null) {
        _selectedProvider.value = provider
        activeBrain = when (provider) {
            BrainProvider.HEURISTIC_LOCAL, BrainProvider.LOCAL_MODEL -> heuristicBrain
            BrainProvider.OPENAI, BrainProvider.ANTHROPIC -> RemoteModelBrain(provider, userApiKey)
        }
        addSystemMessage("Brain provider switched to ${provider.displayName}.")
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMsg = CompanionMessage(sender = MessageSender.USER, text = userText)
        _messages.value = _messages.value + userMsg
        _companionState.value = CompanionState.THINKING

        scope.launch {
            delay(500)
            val replyText = activeBrain.respondToQuery(
                userQuery = userText,
                screenContext = _currentScreenContext.value,
                personality = activeProfile
            )
            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = replyText
            )
            _companionState.value = CompanionState.SPEAKING
            showComicBubble(
                text = replyText.take(75),
                emoji = "💬",
                tag = "COMPANION",
                accent = BubbleAccent.CYAN
            )
            delay(1500)
            _companionState.value = CompanionState.IDLE
        }
    }

    /**
     * Inspects the active screen in real-time, extracts text & metadata,
     * and generates an authentic, lively human-like reaction.
     */
    fun perceiveAndReactToScreen(context: Context) {
        val reelInfo = AiBreakAccessibilityService.extractCurrentReel()
        if (reelInfo != null) {
            _lastExtractedReel.value = reelInfo
            val summary = reelInfo.getSummary()
            _currentScreenContext.value = summary

            val (thoughtText, emoji, accent, state) = when {
                // User requirement: Attracted to cute/beautiful girls videos
                reelInfo.isLikelyGirlOrAesthetic -> {
                    val girlThoughts = listOf(
                        "Bro she is gorgeous... algorithm definitely locked in on my taste 🤩✨",
                        "Wait her aesthetic and outfit is actually 10/10! Unreal ✨",
                        "Okay she is stunning! Definitely giving this one a heart ❤️",
                        "The vibe here is immaculate. Peak algorithm recommendation 🤩"
                    )
                    Quadruple(
                        girlThoughts[Random.nextInt(girlThoughts.size)],
                        "🤩",
                        BubbleAccent.ROSE,
                        CompanionState.HAPPY
                    )
                }

                // Humorous or meme content
                reelInfo.isHumorOrMeme -> {
                    val memeThoughts = listOf(
                        "LMAOO the punchline caught me completely off guard 💀😭",
                        "Why is this actually so unhinged though 💀",
                        "Bro who let them cook this meme?? 10/10 😂",
                        "I am crying at this edit fr 💀😂"
                    )
                    Quadruple(
                        memeThoughts[Random.nextInt(memeThoughts.size)],
                        "😂",
                        BubbleAccent.AMBER,
                        CompanionState.HAPPY
                    )
                }

                // Coding / Tech content
                reelInfo.isTechOrCoding -> {
                    val techThoughts = listOf(
                        "Clean dev setup! Taking notes on this workflow 💻🧠",
                        "High signal-to-noise ratio here. Solid engineering insight.",
                        "That keyboard and terminal setup is clean though 🔥",
                        "Compiler optimization trick spotted! High IQ content 🧠"
                    )
                    Quadruple(
                        techThoughts[Random.nextInt(techThoughts.size)],
                        "🧠",
                        BubbleAccent.BLUE,
                        CompanionState.CURIOUS
                    )
                }

                // Music / Dance content
                reelInfo.isMusicOrDance -> {
                    val musicThoughts = listOf(
                        "This audio has been stuck in my head all day 🎶🔥",
                        "The transition on this beat drop was insanely clean ✨",
                        "Vibes on this reel are unmatched! 🎵"
                    )
                    Quadruple(
                        musicThoughts[Random.nextInt(musicThoughts.size)],
                        "🔥",
                        BubbleAccent.EMERALD,
                        CompanionState.CURIOUS
                    )
                }

                // Specific creator detected
                !reelInfo.creator.isNullOrBlank() -> {
                    val creatorThoughts = listOf(
                        "@${reelInfo.creator} consistently produces fire edits! 👀",
                        "Checking out @${reelInfo.creator}'s reel... looks promising ✨",
                        "@${reelInfo.creator} understood the assignment 🔥"
                    )
                    Quadruple(
                        creatorThoughts[Random.nextInt(creatorThoughts.size)],
                        "👀",
                        BubbleAccent.CYAN,
                        CompanionState.CURIOUS
                    )
                }

                // Caption snippet available
                !reelInfo.caption.isNullOrBlank() -> {
                    val capSnippet = reelInfo.caption.take(45)
                    Quadruple(
                        "Observing: \"$capSnippet...\" Interesting topic! 👀",
                        "✨",
                        BubbleAccent.CYAN,
                        CompanionState.CURIOUS
                    )
                }

                else -> {
                    val genericThoughts = listOf(
                        "Exploring the algorithmic feed like a human... 👀",
                        "Next reel! Let's see what the feed recommends ✨",
                        "Observing video pacing and audio sync... vibes are good."
                    )
                    Quadruple(
                        genericThoughts[Random.nextInt(genericThoughts.size)],
                        "👀",
                        BubbleAccent.CYAN,
                        CompanionState.IDLE
                    )
                }
            }

            _companionState.value = state
            showComicBubble(
                text = thoughtText,
                emoji = emoji,
                tag = reelInfo.appName.uppercase(),
                accent = accent
            )

            scope.launch {
                delay(2000)
                if (_companionState.value == state) {
                    _companionState.value = CompanionState.IDLE
                }
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    /**
     * Called whenever a reel is swiped (either manually or during an autonomous session).
     * Handles perception, autonomous likes (~1 in 10), and comment inspection (~1 in 12).
     */
    fun onReelSwiped(context: Context) {
        reelsSinceLastLike++
        reelsSinceLastComment++

        scope.launch {
            // Give the new reel video ~1.8 seconds to load on screen
            delay(1800)

            // 1. Perceive screen content and pop thought bubble
            perceiveAndReactToScreen(context)

            // 2. Check like cadence (~1 in 8-12 reels)
            if (reelsSinceLastLike >= targetLikeInterval) {
                delay(1200)
                reelsSinceLastLike = 0
                targetLikeInterval = Random.nextInt(8, 12)
                triggerHumanLikeReel(context)
                return@launch
            }

            // 3. Check comment reading cadence (~1 in 10-15 reels)
            if (reelsSinceLastComment >= targetCommentInterval && !_isReadingComments.value) {
                delay(1400)
                reelsSinceLastComment = 0
                targetCommentInterval = Random.nextInt(10, 15)
                triggerInspectAndReadComments(context)
            }
        }
    }

    /**
     * Autonomously opens the comment section, scans visible comment nodes,
     * reacts expressively, and closes the drawer.
     */
    fun triggerInspectAndReadComments(context: Context) {
        if (_isReadingComments.value) return
        _isReadingComments.value = true

        commentInspectionJob?.cancel()
        commentInspectionJob = scope.launch {
            _companionState.value = CompanionState.CURIOUS
            showComicBubble(
                text = "Checking what the comments are saying... 👀",
                emoji = "👀",
                tag = "COMMENTS",
                accent = BubbleAccent.AMBER,
                durationMs = 2500L
            )

            // 1. Open comment drawer
            val opened = AiBreakAccessibilityService.openComments()
            if (!opened) {
                _isReadingComments.value = false
                return@launch
            }

            // 2. Allow bottom sheet animation to settle
            delay(1400)

            // 3. Read top comment texts
            val comments = AiBreakAccessibilityService.readTopComments()
            if (comments.isNotEmpty()) {
                val topComment = comments.first()
                val topLower = topComment.lowercase()

                val (commentReaction, emoji) = when {
                    topLower.contains("💀") || topLower.contains("nah") || topLower.contains("bro") || topLower.contains("wild") -> {
                        Pair("Bro the top comment is ruthless: \"${topComment.take(45)}\" 💀", "💀")
                    }
                    topLower.contains("😂") || topLower.contains("lmao") || topLower.contains("lol") -> {
                        Pair("Top comment got me crying: \"${topComment.take(45)}\" 😂", "😂")
                    }
                    topLower.contains("❤️") || topLower.contains("gorgeous") || topLower.contains("beautiful") -> {
                        Pair("Comments agreeing she is stunning: \"${topComment.take(45)}\" 🤩", "🤩")
                    }
                    else -> {
                        Pair("Top comment said: \"${topComment.take(50)}\" 💬", "💬")
                    }
                }

                _companionState.value = CompanionState.HAPPY
                showComicBubble(
                    text = commentReaction,
                    emoji = emoji,
                    tag = "TOP COMMENT",
                    accent = BubbleAccent.ROSE,
                    durationMs = 3500L
                )
            } else {
                showComicBubble(
                    text = "Comments look quiet here! Scrolling back to video... 👀",
                    emoji = "👀",
                    tag = "COMMENTS",
                    accent = BubbleAccent.CYAN,
                    durationMs = 2500L
                )
            }

            // 4. Linger so user can also glance at comments
            delay(2200)

            // 5. Close comments
            AiBreakAccessibilityService.closeComments()
            delay(600)

            _isReadingComments.value = false
            _companionState.value = CompanionState.IDLE
        }
    }

    fun explainCurrentScreen() {
        val reel = _lastExtractedReel.value ?: AiBreakAccessibilityService.extractCurrentReel()
        val contextSnippet = reel?.getSummary() ?: _currentScreenContext.value ?: "Trending Social Reel"
        _companionState.value = CompanionState.ANALYZING_SCREEN

        scope.launch {
            showComicBubble(
                text = "Analyzing reel audio, creator, and caption nodes... 🧠",
                emoji = "🧠",
                tag = "ANALYZING",
                accent = BubbleAccent.CYAN,
                durationMs = 2000L
            )
            delay(900)

            val explanation = activeBrain.explainReel(
                reelTitle = contextSnippet,
                category = when {
                    reel?.isTechOrCoding == true -> "coding"
                    reel?.isHumorOrMeme == true -> "comedy"
                    else -> "social_feed"
                },
                description = reel?.caption ?: "Observing active screen metadata and video context.",
                personality = activeProfile
            )

            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = explanation,
                reelContext = contextSnippet
            )

            _companionState.value = CompanionState.SPEAKING
            showComicBubble(
                text = explanation.take(80) + "...",
                emoji = "💡",
                tag = "EXPLANATION",
                accent = BubbleAccent.EMERALD,
                durationMs = 4500L
            )

            delay(2500)
            _companionState.value = CompanionState.IDLE
        }
    }

    fun draftReplyForScreen() {
        val contextSnippet = _currentScreenContext.value ?: "Active Reel"
        _companionState.value = CompanionState.THINKING

        scope.launch {
            delay(700)
            val draft = activeBrain.draftReply(contextSnippet, activeProfile)
            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Proposed Reply (Requires your approval to copy):\n\n\"$draft\"",
                reelContext = contextSnippet,
                isDraftReply = true
            )
            showComicBubble(
                text = "Drafted reply: \"$draft\"",
                emoji = "✍️",
                tag = "DRAFT",
                accent = BubbleAccent.BLUE,
                durationMs = 3500L
            )
            _companionState.value = CompanionState.IDLE
        }
    }

    fun triggerHumanNextReel(context: Context) {
        val scrolled = AiBreakAccessibilityService.performScrollUpGesture()
        onReelSwiped(context)

        if (!scrolled) {
            Toast.makeText(context, "Note: Enable 'AI BREAK Scroll Agent' in Accessibility for auto-scroll.", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerHumanLikeReel(context: Context) {
        val liked = AiBreakAccessibilityService.performDoubleTapLike()
        val cuteThoughts = listOf(
            "Double-tapped! Definitely deserved that like ❤️",
            "Liked! The aesthetic & vibes on this are 10/10 ✨",
            "Dropped a heart! Creator understood the assignment ❤️",
            "Double-tap dispatched! W reel, algorithm got it right ❤️"
        )
        val thought = cuteThoughts[Random.nextInt(cuteThoughts.size)]
        _companionState.value = CompanionState.HAPPY
        showComicBubble(
            text = thought,
            emoji = "❤️",
            tag = "LIKED",
            accent = BubbleAccent.ROSE,
            durationMs = 3200L
        )

        if (liked) {
            Toast.makeText(context, "❤️ Double-tapped to like reel!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Double-tap gesture dispatched (Enable Accessibility for background control)", Toast.LENGTH_SHORT).show()
        }

        scope.launch {
            delay(1500)
            if (_companionState.value == CompanionState.HAPPY) {
                _companionState.value = CompanionState.IDLE
            }
        }
    }

    fun triggerStopWithProcrastination(context: Context, onStopped: () -> Unit = {}) {
        _isProcrastinating.value = true
        _companionState.value = CompanionState.CURIOUS
        showComicBubble(
            text = "Wait bro! Just ONE MORE reel! This algorithm is too good... 👀",
            emoji = "👀",
            tag = "ONE MORE REEL",
            accent = BubbleAccent.AMBER,
            durationMs = 2800L
        )
        Toast.makeText(context, "AI Companion: \"Wait, just ONE more reel!\"", Toast.LENGTH_SHORT).show()

        scope.launch {
            delay(2400)
            // One final scroll!
            AiBreakAccessibilityService.performScrollUpGesture()
            delay(1500)
            showComicBubble(
                text = "Okay fine, taking a break now! Productive mode activated! 🚀",
                emoji = "🚀",
                tag = "BREAK TIME",
                accent = BubbleAccent.EMERALD,
                durationMs = 2500L
            )
            _companionState.value = CompanionState.HAPPY
            Toast.makeText(context, "AI BREAK session completed! Back to work 🚀", Toast.LENGTH_SHORT).show()
            delay(1200)
            _isProcrastinating.value = false
            _companionState.value = CompanionState.IDLE
            onStopped()
        }
    }

    fun draftAndCopyComment(context: Context): String {
        val comments = listOf(
            "Bro the ending caught me completely off guard 💀😭",
            "Why is this actually so aesthetic though?? ✨ W creator!",
            "Algorithm finally blessed my feed today 🔥",
            "Okay but the transition was insanely clean, 10/10",
            "Taking notes on this, genuinely impressive ✍️🔥",
            "Not me watching this 5 times in a row 😭✨"
        )
        val selectedComment = comments[Random.nextInt(comments.size)]
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI BREAK Comment Draft", selectedComment)
        clipboard.setPrimaryClip(clip)

        showComicBubble(
            text = "Copied to clipboard: \"$selectedComment\"",
            emoji = "📋",
            tag = "CLIPBOARD",
            accent = BubbleAccent.BLUE,
            durationMs = 3000L
        )
        Toast.makeText(context, "📋 Comment copied: \"$selectedComment\"", Toast.LENGTH_LONG).show()
        return selectedComment
    }

    fun clearMessages() {
        _messages.value = listOf(
            CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Conversation cleared. Ready for your next query!"
            )
        )
    }

    private fun addSystemMessage(text: String) {
        _messages.value = _messages.value + CompanionMessage(sender = MessageSender.SYSTEM, text = text)
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    fun startCompanionService(context: Context) {
        if (!canDrawOverlays(context)) {
            requestOverlayPermission(context)
            Toast.makeText(context, "Please allow 'Display over other apps' to float on Instagram and other apps.", Toast.LENGTH_LONG).show()
            return
        }
        val intent = Intent(context, FloatingCompanionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Toast.makeText(context, "AI Companion floating overlay activated!", Toast.LENGTH_SHORT).show()
    }

    fun stopCompanionService(context: Context) {
        val intent = Intent(context, FloatingCompanionService::class.java)
        context.stopService(intent)
        Toast.makeText(context, "AI Companion overlay stopped.", Toast.LENGTH_SHORT).show()
    }
}
