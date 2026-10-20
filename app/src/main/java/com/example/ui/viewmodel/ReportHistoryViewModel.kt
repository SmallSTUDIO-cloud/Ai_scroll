package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AgentRepository
import com.example.model.SessionRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportHistoryViewModel(
    private val repository: AgentRepository
) : ViewModel() {

    val allSessions: StateFlow<List<SessionRecord>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentReport = MutableStateFlow<SessionRecord?>(null)
    val currentReport: StateFlow<SessionRecord?> = _currentReport.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val record = repository.getSessionById(sessionId).firstOrNull()
            _currentReport.value = record
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun wipeAllMemoryAndHistory() {
        viewModelScope.launch {
            repository.resetAllData()
            _currentReport.value = null
        }
    }

    fun generateShareableText(record: SessionRecord): String {
        return """
            --- AI BREAK REPORT ---
            Agent: ${record.profileName}
            Engine: ${record.engineType}
            Duration: ${record.durationSeconds}s
            Reels Watched: ${record.totalSeen}
            Likes: ${record.totalLiked} | Saves: ${record.totalSaved} | Skips: ${record.totalSkipped}
            Top Topic: ${record.topTopic.uppercase()}
            Coder Affinity: ${(record.coderAffinityScore * 100).toInt()}%

            Takeaway:
            "${record.summaryHeadline}"

            Observations:
            ${record.detailedObservations}

            100% On-Device & Fully Local. AI BREAK.
        """.trimIndent()
    }
}
