package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleFeedData
import com.example.data.repository.AgentRepository
import com.example.engine.AgentAction
import com.example.engine.AgentBrain
import com.example.engine.AgentDecision
import com.example.engine.LocalModelBrain
import com.example.engine.ModelRepository
import com.example.engine.ReelDecisionContext
import com.example.model.FeedItem
import com.example.model.PersonalityProfile
import com.example.model.SessionRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

data class FeedSessionUiState(
    val items: List<FeedItem> = SampleFeedData.ITEMS,
    val currentIndex: Int = 0,
    val profile: PersonalityProfile = PersonalityProfile.PRESET_TECH_NERD,
    val currentDecision: AgentDecision? = null,
    val engineName: String = "Offline Heuristic Engine",
    val secondsLeftOnReel: Float = 0f,
    val dwellTotalSeconds: Float = 0f,
    val isPaused: Boolean = false,
    val isSessionCompleted: Boolean = false,
    val completedSessionId: String? = null,
    val totalSeen: Int = 0,
    val totalLiked: Int = 0,
    val totalSaved: Int = 0,
    val totalSkipped: Int = 0,
    val sessionDurationSec: Long = 0L
)

class FeedSessionViewModel(
    private val agentRepository: AgentRepository,
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedSessionUiState())
    val uiState: StateFlow<FeedSessionUiState> = _uiState.asStateFlow()

    private val brain: AgentBrain = LocalModelBrain(modelRepository)

    private var sessionJob: Job? = null
    private var timerJob: Job? = null
    private val sessionId = UUID.randomUUID().toString()
    private val startTime = System.currentTimeMillis()

    private val seenCategoriesCount = mutableMapOf<String, Int>()

    init {
        viewModelScope.launch {
            val selected = agentRepository.selectedProfile.firstOrNull() ?: PersonalityProfile.PRESET_TECH_NERD
            _uiState.value = _uiState.value.copy(
                profile = selected,
                engineName = brain.engineName
            )
            startSessionTimer()
            processCurrentReelDecision()
        }
    }

    private fun startSessionTimer() {
        timerJob = viewModelScope.launch {
            while (!_uiState.value.isSessionCompleted) {
                delay(1000)
                if (!_uiState.value.isPaused) {
                    val duration = (System.currentTimeMillis() - startTime) / 1000
                    _uiState.value = _uiState.value.copy(sessionDurationSec = duration)
                }
            }
        }
    }

    fun togglePause() {
        _uiState.value = _uiState.value.copy(isPaused = !_uiState.value.isPaused)
    }

    fun onUserSwipeToPage(newIndex: Int) {
        if (newIndex in _uiState.value.items.indices && newIndex != _uiState.value.currentIndex) {
            _uiState.value = _uiState.value.copy(currentIndex = newIndex)
            sessionJob?.cancel()
            processCurrentReelDecision()
        }
    }

    fun toggleUserLike() {
        val currentItem = currentItem() ?: return
        val newStatus = !currentItem.isUserLiked
        updateCurrentItem { it.copy(isUserLiked = newStatus) }

        viewModelScope.launch {
            agentRepository.recordInteraction(
                profileId = _uiState.value.profile.id,
                topic = currentItem.category,
                action = if (newStatus) AgentAction.LIKE else AgentAction.UNLIKE,
                dwellTimeSec = 6.0f,
                userLiked = newStatus
            )
            // Re-evaluate agent thought immediately based on user liking!
            processCurrentReelDecision()
        }
    }

    fun toggleUserSave() {
        val currentItem = currentItem() ?: return
        val newStatus = !currentItem.isUserSaved
        updateCurrentItem { it.copy(isUserSaved = newStatus) }
    }

    fun stopSession() {
        sessionJob?.cancel()
        timerJob?.cancel()

        viewModelScope.launch {
            val state = _uiState.value
            val topCategory = seenCategoriesCount.maxByOrNull { it.value }?.key ?: "technology"

            val headline = when (topCategory) {
                "coding" -> "Agent plunged into compiler theory and kernel code."
                "hardware" -> "Apparently, this agent spent most of its break exploring silicon hardware."
                "space" -> "Agent drifted into cosmic deep space astrophysics."
                "comedy" -> "Agent consumed endless comedy skits and animal bloopers."
                else -> "Agent indulged its curiosity across varied $topCategory topics."
            }

            val sessionRecord = SessionRecord(
                sessionId = sessionId,
                profileId = state.profile.id,
                profileName = state.profile.name,
                startTime = startTime,
                durationSeconds = state.sessionDurationSec,
                engineType = if (brain.isLocalModel) "LOCAL_MODEL" else "HEURISTIC_SIM",
                totalSeen = state.totalSeen,
                totalLiked = state.totalLiked,
                totalSaved = state.totalSaved,
                totalSkipped = state.totalSkipped,
                topTopic = topCategory,
                summaryHeadline = headline,
                detailedObservations = "Explored ${state.totalSeen} reels with ${state.totalLiked} likes and ${state.totalSkipped} skips. Coder affinity evolved to ${((state.profile.coderAffinity) * 100).toInt()}%.",
                coderAffinityScore = state.profile.coderAffinity
            )

            agentRepository.saveSession(sessionRecord)
            _uiState.value = state.copy(
                isSessionCompleted = true,
                completedSessionId = sessionId
            )
        }
    }

    private fun processCurrentReelDecision() {
        sessionJob?.cancel()
        val item = currentItem() ?: return

        sessionJob = viewModelScope.launch {
            seenCategoriesCount[item.category] = (seenCategoriesCount[item.category] ?: 0) + 1
            val seenCount = _uiState.value.totalSeen + 1
            _uiState.value = _uiState.value.copy(totalSeen = seenCount)

            // Gather context
            val context = ReelDecisionContext(
                currentItem = item,
                profile = _uiState.value.profile,
                currentTopicScore = 0.2f,
                sessionItemsSeen = seenCount,
                sessionDurationSec = _uiState.value.sessionDurationSec,
                userLikedThisItem = item.isUserLiked,
                userSavedThisItem = item.isUserSaved,
                consecutiveSimilarTopics = countConsecutiveTopic(item.category)
            )

            val decision = brain.decideNextAction(context)
            _uiState.value = _uiState.value.copy(
                currentDecision = decision,
                dwellTotalSeconds = decision.dwellDurationSec,
                secondsLeftOnReel = decision.dwellDurationSec
            )

            // Dwell countdown loop
            var remaining = decision.dwellDurationSec
            val tick = 0.25f
            while (remaining > 0f) {
                delay((tick * 1000).toLong())
                if (!_uiState.value.isPaused) {
                    remaining -= tick
                    _uiState.value = _uiState.value.copy(secondsLeftOnReel = remaining)
                }
            }

            // Execute the action
            executeAction(decision, item)
        }
    }

    private suspend fun executeAction(decision: AgentDecision, item: FeedItem) {
        when (decision.action) {
            AgentAction.STOP -> {
                stopSession()
            }
            AgentAction.LIKE -> {
                updateCurrentItem { it.copy(isAgentLiked = true) }
                _uiState.value = _uiState.value.copy(totalLiked = _uiState.value.totalLiked + 1)
                agentRepository.recordInteraction(
                    profileId = _uiState.value.profile.id,
                    topic = item.category,
                    action = AgentAction.LIKE,
                    dwellTimeSec = decision.dwellDurationSec,
                    userLiked = item.isUserLiked
                )
                // After liking, pause briefly then swipe up to next
                delay(1500)
                advanceToNextReel()
            }
            AgentAction.SAVE -> {
                updateCurrentItem { it.copy(isAgentSaved = true) }
                _uiState.value = _uiState.value.copy(totalSaved = _uiState.value.totalSaved + 1)
                agentRepository.recordInteraction(
                    profileId = _uiState.value.profile.id,
                    topic = item.category,
                    action = AgentAction.SAVE,
                    dwellTimeSec = decision.dwellDurationSec,
                    userLiked = item.isUserLiked
                )
                delay(1500)
                advanceToNextReel()
            }
            AgentAction.SWIPE_UP -> {
                _uiState.value = _uiState.value.copy(totalSkipped = _uiState.value.totalSkipped + 1)
                agentRepository.recordInteraction(
                    profileId = _uiState.value.profile.id,
                    topic = item.category,
                    action = AgentAction.SWIPE_UP,
                    dwellTimeSec = decision.dwellDurationSec,
                    userLiked = item.isUserLiked
                )
                advanceToNextReel()
            }
            AgentAction.WAIT -> {
                agentRepository.recordInteraction(
                    profileId = _uiState.value.profile.id,
                    topic = item.category,
                    action = AgentAction.WAIT,
                    dwellTimeSec = decision.dwellDurationSec,
                    userLiked = item.isUserLiked
                )
                advanceToNextReel()
            }
            else -> {
                advanceToNextReel()
            }
        }
    }

    private fun advanceToNextReel() {
        val nextIndex = (_uiState.value.currentIndex + 1) % _uiState.value.items.size
        _uiState.value = _uiState.value.copy(currentIndex = nextIndex)
        processCurrentReelDecision()
    }

    private fun currentItem(): FeedItem? {
        val state = _uiState.value
        return state.items.getOrNull(state.currentIndex)
    }

    private fun updateCurrentItem(transform: (FeedItem) -> FeedItem) {
        val state = _uiState.value
        val list = state.items.toMutableList()
        if (state.currentIndex in list.indices) {
            list[state.currentIndex] = transform(list[state.currentIndex])
            _uiState.value = state.copy(items = list)
        }
    }

    private fun countConsecutiveTopic(topic: String): Int {
        var count = 0
        for (i in _uiState.value.currentIndex downTo 0) {
            if (_uiState.value.items.getOrNull(i)?.category.equals(topic, ignoreCase = true)) {
                count++
            } else {
                break
            }
        }
        return count
    }

    override fun onCleared() {
        super.onCleared()
        sessionJob?.cancel()
        timerJob?.cancel()
    }
}
