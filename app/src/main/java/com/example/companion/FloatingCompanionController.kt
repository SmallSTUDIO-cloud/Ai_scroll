package com.example.companion

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.example.companion.engine.ConversationBrain
import com.example.companion.engine.HeuristicConversationBrain
import com.example.companion.engine.RemoteModelBrain
import com.example.companion.model.BrainProvider
import com.example.companion.model.CompanionMessage
import com.example.companion.model.CompanionState
import com.example.companion.model.MessageSender
import com.example.companion.service.FloatingCompanionService
import com.example.model.PersonalityProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val _messages = MutableStateFlow<List<CompanionMessage>>(
        listOf(
            CompanionMessage(
                sender = MessageSender.COMPANION,
                text = "Hello! I am your AI BREAK companion. Tap 'Explain Reel' or ask me anything as we scroll."
            )
        )
    )
    val messages: StateFlow<List<CompanionMessage>> = _messages.asStateFlow()

    private val _currentScreenContext = MutableStateFlow<String?>("James Webb Space Telescope Primordial Galaxy")
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
        val contextSnippet = _currentScreenContext.value ?: "General Feed Screen"
        _companionState.value = CompanionState.ANALYZING_SCREEN

        scope.launch {
            delay(800)
            val explanation = activeBrain.explainReel(
                reelTitle = contextSnippet,
                category = "technology",
                description = "Observing on-screen UI metadata and reel tags.",
                personality = activeProfile
            )
            _messages.value = _messages.value + CompanionMessage(
                sender = MessageSender.COMPANION,
                text = explanation,
                reelContext = contextSnippet
            )
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
            return
        }
        val intent = Intent(context, FloatingCompanionService::class.java)
        context.startService(intent)
    }

    fun stopCompanionService(context: Context) {
        val intent = Intent(context, FloatingCompanionService::class.java)
        context.stopService(intent)
    }
}
