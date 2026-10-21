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
import com.example.model.PersonalityProfile
import com.example.service.AiBreakAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

object FloatingCompanionController {

    private val scope = CoroutineScope(Dispatchers.Main)

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

    private val _isProcrastinating = MutableStateFlow(false)
    val isProcrastinating: StateFlow<Boolean> = _isProcrastinating.asStateFlow()

    private val _messages = MutableStateFlow<List<CompanionMessage>>(
        listOf(
            CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Hello! I am your AI BREAK companion. Tap 'Explain Reel' or ask me anything as we scroll."
            )
        )
    )
    val messages: StateFlow<List<CompanionMessage>> = _messages.asStateFlow()

    private val _currentScreenContext = MutableStateFlow<String?>("Trending Social Feed")
    val currentScreenContext: StateFlow<String?> = _currentScreenContext.asStateFlow()

    private val _isSheetExpanded = MutableStateFlow(false)
    val isSheetExpanded: StateFlow<Boolean> = _isSheetExpanded.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

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
    }

    fun setOverlayPopupVisible(visible: Boolean) {
        _isOverlayPopupVisible.value = visible
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
            delay(600) // Small natural thinking delay
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
            delay(1500)
            _companionState.value = CompanionState.IDLE
        }
    }

    fun explainCurrentScreen() {
        val contextSnippet = _currentScreenContext.value ?: "Trending Social Reel"
        _companionState.value = CompanionState.ANALYZING_SCREEN

        scope.launch {
            delay(800)
            val explanation = activeBrain.explainReel(
                reelTitle = contextSnippet,
                category = "social_feed",
                description = "Observing on-screen UI metadata, creator context, and reel tags.",
                personality = activeProfile
            )
            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = explanation,
                reelContext = contextSnippet
            )
            _currentThought.value = "Analyzed: ${explanation.take(60)}..."
            _companionState.value = CompanionState.SPEAKING
            delay(1500)
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
            _companionState.value = CompanionState.IDLE
        }
    }

    fun triggerHumanNextReel(context: Context) {
        val scrolled = AiBreakAccessibilityService.performScrollUpGesture()
        val thoughts = listOf(
            "Scrolling... Let's see what the algorithm cooks up next 👀",
            "Swiped to next reel! The audio transitions on these are wild.",
            "Checking out the next creator... vibes are good ✨",
            "Next reel! Hoping for some high-IQ coding or cute animal clips 🐶",
            "Algorithm is cooking today. Let's see the next short!"
        )
        val selectedThought = thoughts[Random.nextInt(thoughts.size)]
        _currentThought.value = selectedThought
        _companionState.value = CompanionState.CURIOUS

        if (!scrolled) {
            Toast.makeText(context, "Note: Enable 'AI BREAK Scroll Agent' in Accessibility for auto-scroll.", Toast.LENGTH_SHORT).show()
        }

        scope.launch {
            delay(1200)
            _companionState.value = CompanionState.IDLE
        }
    }

    fun triggerHumanLikeReel(context: Context) {
        val liked = AiBreakAccessibilityService.performDoubleTapLike()
        val cuteThoughts = listOf(
            "Double-tapped! This reel is adorable ❤️",
            "Liked! The aesthetic & music on this is 10/10 ✨",
            "Dropped a heart! Creator definitely understood the assignment.",
            "Double-tap dispatched! W reel, algorithm got it right ❤️"
        )
        _currentThought.value = cuteThoughts[Random.nextInt(cuteThoughts.size)]
        _companionState.value = CompanionState.HAPPY

        if (liked) {
            Toast.makeText(context, "❤️ Double-tapped to like reel!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Double-tap gesture dispatched (Enable Accessibility for background control)", Toast.LENGTH_SHORT).show()
        }

        scope.launch {
            delay(1500)
            _companionState.value = CompanionState.IDLE
        }
    }

    fun triggerStopWithProcrastination(context: Context, onStopped: () -> Unit = {}) {
        _isProcrastinating.value = true
        _companionState.value = CompanionState.CURIOUS
        _currentThought.value = "Wait, bro! Just ONE MORE reel! This algorithm is too interesting... 👀"
        Toast.makeText(context, "AI Companion: \"Wait, just ONE more reel!\"", Toast.LENGTH_SHORT).show()

        scope.launch {
            delay(2400)
            // One final scroll!
            AiBreakAccessibilityService.performScrollUpGesture()
            delay(1500)
            _currentThought.value = "Okay fine, taking a break now! Productive mode activated! 🚀"
            _companionState.value = CompanionState.HAPPY
            Toast.makeText(context, "AI BREAK session completed! Back to work 🚀", Toast.LENGTH_SHORT).show()
            delay(1200)
            _isProcrastinating.value = false
            _companionState.value = CompanionState.IDLE
            onStopped()
        }
    }

    fun draftAndCopyComment(context: Context): String {
        val profileName = activeProfile.name
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

        _currentThought.value = "Drafted: \"$selectedComment\" (Copied to clipboard!)"
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
