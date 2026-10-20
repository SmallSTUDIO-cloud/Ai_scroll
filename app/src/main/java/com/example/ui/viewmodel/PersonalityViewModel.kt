package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AgentRepository
import com.example.model.PersonalityProfile
import com.example.model.TopicPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class PersonalityViewModel(
    private val repository: AgentRepository
) : ViewModel() {

    val allProfiles: StateFlow<List<PersonalityProfile>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedProfile: StateFlow<PersonalityProfile?> = repository.selectedProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentPreferences: StateFlow<List<TopicPreference>> = selectedProfile
        .flatMapLatest { profile ->
            if (profile != null) repository.getPreferences(profile.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editorMessage = MutableStateFlow<String?>(null)
    val editorMessage: StateFlow<String?> = _editorMessage.asStateFlow()

    fun selectProfile(id: String) {
        viewModelScope.launch {
            repository.setSelectedProfile(id)
            _editorMessage.value = "Selected agent profile updated."
        }
    }

    fun createCustomProfile(
        name: String,
        tagline: String,
        description: String,
        preferredTopics: List<String>,
        avoidTopics: List<String>,
        explorationRate: Float,
        skipThresholdSec: Float,
        watchDurationSec: Float
    ) {
        viewModelScope.launch {
            val custom = PersonalityProfile(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name.ifBlank { "Custom Agent" },
                tagline = tagline.ifBlank { "User-defined exploratory parameters" },
                description = description.ifBlank { "Personalized agent configuration." },
                avatarType = "CUSTOM",
                preferredTopics = preferredTopics,
                avoidTopics = avoidTopics,
                explorationRate = explorationRate.coerceIn(0.05f, 0.95f),
                skipThresholdSec = skipThresholdSec.coerceIn(1.5f, 10.0f),
                watchDurationPrefSec = watchDurationSec.coerceIn(5.0f, 25.0f),
                likeProbability = 0.60f,
                saveProbability = 0.30f,
                coderAffinity = if (preferredTopics.contains("coding")) 0.6f else 0.1f,
                isCustom = true,
                isSelected = true
            )
            repository.saveProfile(custom)
            repository.setSelectedProfile(custom.id)
            _editorMessage.value = "Custom agent profile '${custom.name}' created!"
        }
    }

    fun resetActiveProfileMemory() {
        val current = selectedProfile.value ?: return
        viewModelScope.launch {
            repository.resetPreferencesForProfile(current.id)
            _editorMessage.value = "Agent preference memory wiped for ${current.name}."
        }
    }

    fun clearMessage() {
        _editorMessage.value = null
    }
}
