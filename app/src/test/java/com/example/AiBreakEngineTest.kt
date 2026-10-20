package com.example

import com.example.data.SampleFeedData
import com.example.engine.AgentAction
import com.example.engine.HeuristicRuleBrain
import com.example.engine.ReelDecisionContext
import com.example.model.PersonalityProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiBreakEngineTest {

    @Test
    fun testSampleFeedHasAtLeast30Items() {
        assertTrue(
            "Expected at least 30 diverse feed items, found ${SampleFeedData.ITEMS.size}",
            SampleFeedData.ITEMS.size >= 30
        )
        // Verify diverse categories are covered
        val categories = SampleFeedData.ITEMS.map { it.category }.toSet()
        assertTrue(categories.contains("coding"))
        assertTrue(categories.contains("comedy"))
        assertTrue(categories.contains("science"))
        assertTrue(categories.contains("space"))
        assertTrue(categories.contains("cooking"))
        assertTrue(categories.contains("hardware"))
    }

    @Test
    fun testPersonalityPresetsCompleteness() {
        val presets = PersonalityProfile.ALL_PRESETS
        assertEquals(5, presets.size)
        val ids = presets.map { it.id }.toSet()
        assertTrue(ids.contains("tech_nerd"))
        assertTrue(ids.contains("comedian"))
        assertTrue(ids.contains("curious_explorer"))
        assertTrue(ids.contains("analyst"))
        assertTrue(ids.contains("coder_agent"))
    }

    @Test
    fun testHeuristicBrainDecidesValidAction() = runBlocking {
        val brain = HeuristicRuleBrain()
        val item = SampleFeedData.ITEMS.first { it.category == "coding" }
        val context = ReelDecisionContext(
            currentItem = item,
            profile = PersonalityProfile.PRESET_TECH_NERD,
            currentTopicScore = 0.5f,
            sessionItemsSeen = 2,
            sessionDurationSec = 15L,
            userLikedThisItem = false,
            userSavedThisItem = false,
            consecutiveSimilarTopics = 0
        )

        val decision = brain.decideNextAction(context)
        assertNotNull(decision.action)
        assertTrue(decision.internalMonologue.isNotBlank())
        assertTrue(decision.reason.isNotBlank())
        assertTrue(decision.confidence in 0f..1f)
        assertTrue(decision.dwellDurationSec > 0f)
    }

    @Test
    fun testHumanLikeInfluencesAgentAndBoostsInterest() = runBlocking {
        val brain = HeuristicRuleBrain()
        val item = SampleFeedData.ITEMS.first { it.category == "comedy" }

        // Context without user like
        val ctxWithoutLike = ReelDecisionContext(
            currentItem = item,
            profile = PersonalityProfile.PRESET_TECH_NERD, // Tech nerd usually has low interest in comedy
            currentTopicScore = 0.0f,
            sessionItemsSeen = 1,
            sessionDurationSec = 10L,
            userLikedThisItem = false,
            userSavedThisItem = false,
            consecutiveSimilarTopics = 0
        )
        val decision1 = brain.decideNextAction(ctxWithoutLike)

        // Context with human user like
        val ctxWithLike = ctxWithoutLike.copy(userLikedThisItem = true)
        val decision2 = brain.decideNextAction(ctxWithLike)

        assertTrue(
            "User like should elevate agent interest score",
            decision2.interestScore >= decision1.interestScore
        )
    }

    @Test
    fun testBreakQuotaStopsSessionSafely() = runBlocking {
        val brain = HeuristicRuleBrain()
        val item = SampleFeedData.ITEMS.first()
        val context = ReelDecisionContext(
            currentItem = item,
            profile = PersonalityProfile.PRESET_TECH_NERD,
            currentTopicScore = 0.0f,
            sessionItemsSeen = 50,
            sessionDurationSec = 300L,
            userLikedThisItem = false,
            userSavedThisItem = false,
            consecutiveSimilarTopics = 0,
            itemsRemainingInSessionLimit = 50
        )

        val decision = brain.decideNextAction(context)
        assertEquals(AgentAction.STOP, decision.action)
    }
}
