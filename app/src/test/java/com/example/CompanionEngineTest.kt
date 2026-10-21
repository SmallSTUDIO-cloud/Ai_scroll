package com.example

import com.example.companion.FloatingCompanionController
import com.example.companion.engine.HeuristicConversationBrain
import com.example.companion.engine.RemoteModelBrain
import com.example.companion.model.BrainProvider
import com.example.companion.model.CompanionState
import com.example.model.PersonalityProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CompanionEngineTest {

    @Test
    fun testHeuristicCompanionAnswersGeneralQuery() = runBlocking {
        val brain = HeuristicConversationBrain()
        val response = brain.respondToQuery(
            userQuery = "who are you?",
            screenContext = "Kotlin Compiler AST parser",
            personality = PersonalityProfile.PRESET_TECH_NERD
        )
        assertNotNull(response)
        assertTrue(response.contains("AI BREAK", ignoreCase = true))
    }

    @Test
    fun testHeuristicCompanionExplainsCodingReel() = runBlocking {
        val brain = HeuristicConversationBrain()
        val explanation = brain.explainReel(
            reelTitle = "Building a C Compiler in Kotlin",
            category = "coding",
            description = "Lexer and code generator",
            personality = PersonalityProfile.PRESET_CODER_AGENT
        )
        assertTrue(explanation.contains("Technical Breakdown", ignoreCase = true))
        assertTrue(explanation.contains("Coder Assessment", ignoreCase = true))
    }

    @Test
    fun testHeuristicCompanionDraftsReply() = runBlocking {
        val brain = HeuristicConversationBrain()
        val draft = brain.draftReply(
            reelContext = "Optimizing Coroutines",
            personality = PersonalityProfile.PRESET_CODER_AGENT
        )
        assertTrue(draft.isNotBlank())
        assertTrue(draft.contains("optimization", ignoreCase = true) || draft.contains("clean", ignoreCase = true))
    }

    @Test
    fun testRemoteModelBrainGracefulOfflineFallback() = runBlocking {
        // When user hasn't configured an API key, it must cleanly fall back
        val remoteBrain = RemoteModelBrain(
            provider = BrainProvider.OPENAI,
            userApiKey = null
        )
        val reply = remoteBrain.respondToQuery(
            userQuery = "explain this feed",
            screenContext = null,
            personality = PersonalityProfile.PRESET_TECH_NERD
        )
        assertTrue(reply.contains("Offline Fallback", ignoreCase = true))
    }

    @Test
    fun testCompanionControllerStateFlow() {
        FloatingCompanionController.setCompanionState(CompanionState.THINKING)
        assertEquals(CompanionState.THINKING, FloatingCompanionController.companionState.value)

        FloatingCompanionController.setCompanionState(CompanionState.IDLE)
        assertEquals(CompanionState.IDLE, FloatingCompanionController.companionState.value)
    }
}
