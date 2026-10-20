package com.example.data.repository

import com.example.data.db.AgentDao
import com.example.engine.AgentAction
import com.example.model.PersonalityProfile
import com.example.model.SessionRecord
import com.example.model.TopicPreference
import kotlinx.coroutines.flow.Flow
import kotlin.math.max
import kotlin.math.min

class AgentRepository(private val agentDao: AgentDao) {

    val allProfiles: Flow<List<PersonalityProfile>> = agentDao.getAllProfiles()
    val selectedProfile: Flow<PersonalityProfile?> = agentDao.getSelectedProfile()
    val allSessions: Flow<List<SessionRecord>> = agentDao.getAllSessions()

    fun getPreferences(profileId: String): Flow<List<TopicPreference>> =
        agentDao.getPreferencesForProfile(profileId)

    fun getSessionById(sessionId: String): Flow<SessionRecord?> =
        agentDao.getSessionById(sessionId)

    suspend fun setSelectedProfile(id: String) {
        agentDao.setSelectedProfile(id)
    }

    suspend fun saveProfile(profile: PersonalityProfile) {
        agentDao.insertProfile(profile)
    }

    suspend fun deleteCustomProfile(id: String) {
        agentDao.deleteCustomProfile(id)
    }

    suspend fun recordInteraction(
        profileId: String,
        topic: String,
        action: AgentAction,
        dwellTimeSec: Float,
        userLiked: Boolean
    ) {
        val currentPref = agentDao.getTopicPreference(profileId, topic) ?: TopicPreference(
            profileId = profileId,
            topic = topic,
            score = 0.0f,
            watches = 0,
            skips = 0,
            likes = 0
        )

        var newScore = currentPref.score
        var newWatches = currentPref.watches
        var newSkips = currentPref.skips
        var newLikes = currentPref.likes

        when (action) {
            AgentAction.LIKE, AgentAction.SAVE -> {
                newScore += 0.20f
                newLikes += 1
                newWatches += 1
            }
            AgentAction.WAIT -> {
                if (dwellTimeSec >= 5.0f) {
                    newScore += 0.08f
                    newWatches += 1
                }
            }
            AgentAction.SWIPE_UP -> {
                if (dwellTimeSec <= 4.0f) {
                    newScore -= 0.10f
                    newSkips += 1
                } else {
                    newWatches += 1
                }
            }
            else -> {}
        }

        if (userLiked) {
            newScore += 0.15f
        }

        val clampedScore = min(1.0f, max(-1.0f, newScore))
        agentDao.upsertPreference(
            currentPref.copy(
                score = clampedScore,
                watches = newWatches,
                skips = newSkips,
                likes = newLikes,
                lastUpdated = System.currentTimeMillis()
            )
        )

        // Evolve Coder Affinity if interacting with coding/tech/hardware
        val isCodeTopic = topic in listOf("coding", "hardware", "technology", "ai")
        if (isCodeTopic) {
            val profile = agentDao.getProfileById(profileId)
            if (profile != null) {
                val affinityBoost = if (action == AgentAction.LIKE || userLiked) 0.05f else 0.02f
                val newAffinity = min(1.0f, profile.coderAffinity + affinityBoost)
                agentDao.updateProfile(profile.copy(coderAffinity = newAffinity))
            }
        }
    }

    suspend fun saveSession(session: SessionRecord) {
        agentDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        agentDao.deleteSession(sessionId)
    }

    suspend fun resetPreferencesForProfile(profileId: String) {
        agentDao.resetPreferencesForProfile(profileId)
    }

    suspend fun resetAllData() {
        agentDao.resetAllData()
    }
}
