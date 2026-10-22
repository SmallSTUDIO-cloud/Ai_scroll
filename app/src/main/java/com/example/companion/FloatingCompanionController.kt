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
import com.example.companion.engine.GeminiReelBrain
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

    private val _selectedProvider = MutableStateFlow(BrainProvider.GEMINI_ONLINE)
    val selectedProvider: StateFlow<BrainProvider> = _selectedProvider.asStateFlow()

    private val _inAppBubbleEnabled = MutableStateFlow(true)
    val inAppBubbleEnabled: StateFlow<Boolean> = _inAppBubbleEnabled.asStateFlow()

    private val _isOverlayPopupVisible = MutableStateFlow(false)
    val isOverlayPopupVisible: StateFlow<Boolean> = _isOverlayPopupVisible.asStateFlow()

    private val _currentThought = MutableStateFlow("Cognitive reels companion active. Observing feed like a human...")
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

    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment: StateFlow<Boolean> = _isPostingComment.asStateFlow()

    // Counters for human-like cadence (~1 like every 8-12 reels, ~1 comment post every 10-14 reels)
    private var reelsSinceLastLike = 0
    private var targetLikeInterval = Random.nextInt(8, 12)

    private var reelsSinceLastComment = 0
    private var targetCommentInterval = Random.nextInt(10, 14)

    private val _messages = MutableStateFlow<List<CompanionMessage>>(
        listOf(
            CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Hello! I am your cognitive AI companion. I monitor reel context, adapt watch time, and interact like a real viewer."
            )
        )
    )
    val messages: StateFlow<List<CompanionMessage>> = _messages.asStateFlow()

    private val _currentScreenContext = MutableStateFlow<String?>("Trending Social Feed")
    val currentScreenContext: StateFlow<String?> = _currentScreenContext.asStateFlow()

    private val _isSheetExpanded = MutableStateFlow(false)
    val isSheetExpanded: StateFlow<Boolean> = _isSheetExpanded.asStateFlow()

    private var activeProfile: PersonalityProfile = PersonalityProfile.PRESET_TECH_NERD

    private val heuristicBrain = HeuristicConversationBrain()
    private val geminiBrain = GeminiReelBrain()
    private var activeBrain: ConversationBrain = geminiBrain

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
            BrainProvider.GEMINI_ONLINE -> GeminiReelBrain(userApiKey)
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
                text = replyText.take(80),
                emoji = "💬",
                tag = "COMPANION",
                accent = BubbleAccent.CYAN
            )
            delay(1500)
            _companionState.value = CompanionState.IDLE
        }
    }

    /**
     * Inspects active reel content in the background (creator, captions, metrics, views, audio)
     * and shows a lively, authentic human reflection.
     */
    fun perceiveAndReactToScreen(context: Context): Int {
        val reelInfo = AiBreakAccessibilityService.extractCurrentReel()
        if (reelInfo != null) {
            _lastExtractedReel.value = reelInfo
            val summary = reelInfo.getSummary()
            _currentScreenContext.value = summary

            val watchDuration = reelInfo.computeAdaptiveWatchDurationSeconds()

            val (thoughtText, emoji, accent, state) = when {
                // User explicit requirement: Attracted to cute/beautiful girls videos
                reelInfo.isLikelyGirlOrAesthetic -> {
                    val girlThoughts = listOf(
                        "Bro she is gorgeous... algorithm definitely locked in on my taste 🤩✨",
                        "Wait her aesthetic and outfit is actually 10/10! Unreal ✨",
                        "Okay she is stunning! Definitely giving this one a heart ❤️",
                        "Watching this one fully... vibe is immaculate 🤩"
                    )
                    Quadruple(
                        girlThoughts[Random.nextInt(girlThoughts.size)],
                        "🤩",
                        BubbleAccent.ROSE,
                        CompanionState.HAPPY
                    )
                }

                // Tech / Coding / Engineering
                reelInfo.isTechOrCoding -> {
                    val techThoughts = listOf(
                        "Clean dev architecture! Taking notes on this workflow 💻🧠",
                        "High signal-to-noise ratio here. Staying to watch the breakdown 🧠",
                        "That setup and compile speed is clean though 🔥",
                        "Solid engineering insight. High IQ content 🧠"
                    )
                    Quadruple(
                        techThoughts[Random.nextInt(techThoughts.size)],
                        "🧠",
                        BubbleAccent.BLUE,
                        CompanionState.CURIOUS
                    )
                }

                // Humorous or meme
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

                // Music / Dance
                reelInfo.isMusicOrDance -> {
                    val musicThoughts = listOf(
                        "This audio has been stuck in my head all day 🎶🔥",
                        "The beat drop transition was insanely clean ✨",
                        "Vibes on this reel are unmatched! 🎵"
                    )
                    Quadruple(
                        musicThoughts[Random.nextInt(musicThoughts.size)],
                        "🔥",
                        BubbleAccent.EMERALD,
                        CompanionState.CURIOUS
                    )
                }

                // High viral engagement reel
                reelInfo.isHighEngagement -> {
                    Quadruple(
                        "Viral clip detected! Watching to see why everyone is sharing this 👀🔥",
                        "🔥",
                        BubbleAccent.AMBER,
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
                delay(2200)
                if (_companionState.value == state) {
                    _companionState.value = CompanionState.IDLE
                }
            }
            return watchDuration
        }
        return 8
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    /**
     * Called whenever a reel is swiped (either manually or during an autonomous session).
     * Handles perception, adaptive watch time, autonomous likes (~1 in 10), and comment posting.
     */
    fun onReelSwiped(context: Context, onAdaptiveWaitDetermined: (Int) -> Unit = {}) {
        reelsSinceLastLike++
        reelsSinceLastComment++

        scope.launch {
            delay(1600)

            // 1. Perceive screen content and calculate adaptive watch time
            val watchTime = perceiveAndReactToScreen(context)
            onAdaptiveWaitDetermined(watchTime)

            // 2. Check like cadence (~1 in 8-12 reels)
            if (reelsSinceLastLike >= targetLikeInterval) {
                delay(1200)
                reelsSinceLastLike = 0
                targetLikeInterval = Random.nextInt(8, 12)
                triggerHumanLikeReel(context)
                return@launch
            }

            // 3. Check comment posting cadence (~1 in 10-14 reels)
            if (reelsSinceLastComment >= targetCommentInterval && !_isPostingComment.value) {
                delay(1800)
                reelsSinceLastComment = 0
                targetCommentInterval = Random.nextInt(10, 14)
                triggerAutonomousCommentPosting(context)
            }
        }
    }

    /**
     * Autonomously opens comments, reads top discussion, synthesizes an authentic comment
     * using Gemini / Heuristic brain, types it into the input node, submits it, and closes the drawer.
     */
    fun triggerAutonomousCommentPosting(context: Context) {
        if (_isPostingComment.value) return
        _isPostingComment.value = true

        commentInspectionJob?.cancel()
        commentInspectionJob = scope.launch {
            val reel = _lastExtractedReel.value ?: AiBreakAccessibilityService.extractCurrentReel()
            _companionState.value = CompanionState.THINKING

            showComicBubble(
                text = "Checking comments to drop an authentic reply... 💬",
                emoji = "💬",
                tag = "COMMENTING",
                accent = BubbleAccent.AMBER,
                durationMs = 2600L
            )

            val opened = AiBreakAccessibilityService.openComments()
            if (!opened) {
                _isPostingComment.value = false
                return@launch
            }

            delay(1300)

            // Read visible comments to ground our reply
            val comments = AiBreakAccessibilityService.readTopComments()

            // Synthesize comment using active brain
            val commentToPost = if (activeBrain is GeminiReelBrain && reel != null) {
                (activeBrain as GeminiReelBrain).synthesizeSmartComment(reel, comments, activeProfile)
            } else if (reel != null) {
                geminiBrain.synthesizeSmartComment(reel, comments, activeProfile)
            } else {
                activeBrain.draftReply(reel?.getSummary() ?: "Active Reel", activeProfile)
            }

            showComicBubble(
                text = "Typing: \"$commentToPost\" ✍️",
                emoji = "✍️",
                tag = "POSTING",
                accent = BubbleAccent.BLUE,
                durationMs = 3000L
            )

            // Submit comment via accessibility action
            AiBreakAccessibilityService.postCommentAutonomously(commentToPost) { success ->
                scope.launch {
                    if (success) {
                        _companionState.value = CompanionState.HAPPY
                        showComicBubble(
                            text = "Comment posted! Dropped some love on that reel ✨❤️",
                            emoji = "❤️",
                            tag = "POSTED",
                            accent = BubbleAccent.ROSE,
                            durationMs = 3500L
                        )
                        Toast.makeText(context, "💬 Bot commented: \"$commentToPost\"", Toast.LENGTH_SHORT).show()
                    } else {
                        showComicBubble(
                            text = "Comment input unavailable or locked. Resuming video... 👀",
                            emoji = "👀",
                            tag = "COMMENTS",
                            accent = BubbleAccent.CYAN,
                            durationMs = 2500L
                        )
                    }

                    delay(2000)
                    _isPostingComment.value = false
                    _companionState.value = CompanionState.IDLE
                }
            }
        }
    }

    fun explainCurrentScreen() {
        val reel = _lastExtractedReel.value ?: AiBreakAccessibilityService.extractCurrentReel()
        val contextSnippet = reel?.getSummary() ?: _currentScreenContext.value ?: "Trending Social Reel"
        _companionState.value = CompanionState.ANALYZING_SCREEN

        scope.launch {
            showComicBubble(
                text = "Analyzing reel caption, metrics, and video context... 🧠",
                emoji = "🧠",
                tag = "ANALYZING",
                accent = BubbleAccent.CYAN,
                durationMs = 2000L
            )
            delay(700)

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
            delay(500)
            val draft = activeBrain.draftReply(contextSnippet, activeProfile)
            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Proposed Reply:\n\n\"$draft\"",
                reelContext = contextSnippet,
                isDraftReply = true
            )
            showComicBubble(
                text = "Drafted: \"$draft\"",
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
