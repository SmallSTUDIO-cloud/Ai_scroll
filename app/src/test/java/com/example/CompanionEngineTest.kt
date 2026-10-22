package com.example

import com.example.companion.FloatingCompanionController
import com.example.companion.engine.HeuristicConversationBrain
import com.example.companion.engine.RemoteModelBrain
import com.example.companion.model.BrainProvider
import com.example.companion.model.CompanionState
import com.example.companion.ui.BubbleAccent
import com.example.model.ExtractedReelInfo
import com.example.model.PersonalityProfile
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

    @Test
    fun testExtractedReelInfoSummaryAndHeuristics() {
        val reel = ExtractedReelInfo(
            packageName = "com.instagram.android",
            appName = "Instagram",
            creator = "tech_creator",
            caption = "Building an ultra-fast Kotlin coroutines dispatcher for high throughput",
            audioTitle = "Coding Beats Vol. 1",
            rawVisibleTexts = listOf("tech_creator", "Follow", "Coding Beats Vol. 1"),
            isTechOrCoding = true
        )

        val summary = reel.getSummary()
        assertTrue(summary.contains("@tech_creator"))
        assertTrue(summary.contains("Building an ultra-fast"))
        assertTrue(reel.isTechOrCoding)
    }

    @Test
    fun testShowComicBubbleAndDismiss() {
        FloatingCompanionController.showComicBubble(
            text = "Bro she is gorgeous... algorithm knows my taste 🤩✨",
            emoji = "🤩",
            tag = "INSTAGRAM",
            accent = BubbleAccent.ROSE,
            durationMs = 5000L
        )

        assertTrue(FloatingCompanionController.isComicBubbleVisible.value)
        assertEquals("🤩", FloatingCompanionController.activeEmojiReaction.value)
        assertEquals("Bro she is gorgeous... algorithm knows my taste 🤩✨", FloatingCompanionController.activeComicThought.value?.text)

        FloatingCompanionController.dismissComicBubble()
        org.junit.Assert.assertFalse(FloatingCompanionController.isComicBubbleVisible.value)
    }
}
