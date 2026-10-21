package com.example.companion.engine

import com.example.companion.model.BrainProvider
import com.example.model.PersonalityProfile

class HeuristicConversationBrain : ConversationBrain {
    override val provider: BrainProvider = BrainProvider.HEURISTIC_LOCAL

    override suspend fun respondToQuery(
        userQuery: String,
        screenContext: String?,
        personality: PersonalityProfile
    ): String {
        val queryLower = userQuery.lowercase()
        val isCoder = personality.avatarType == "CODER" || personality.coderAffinity >= 0.70f

        return when {
            queryLower.contains("who are you") || queryLower.contains("your name") -> {
                if (isCoder) "I am AI BREAK Coder Companion. Running 100% locally on your device with 0 external dependencies."
                else "I am ${personality.name}, your on-device AI BREAK companion! I watch reels and develop tastes alongside you."
            }
            queryLower.contains("what are you watching") || queryLower.contains("current reel") -> {
                if (screenContext != null) "Currently observing: $screenContext."
                else "I'm monitoring the active feed. Tap 'Explain Reel' when viewing content!"
            }
            queryLower.contains("offline") || queryLower.contains("privacy") -> {
                "We are in 100% Offline Mode. No audio, screen text, or interaction history ever leaves this physical device."
            }
            queryLower.contains("code") || queryLower.contains("kotlin") || queryLower.contains("compiler") -> {
                "From a systems perspective: optimizing execution speed and memory footprints makes on-device intelligence sustainable."
            }
            queryLower.contains("joke") || queryLower.contains("funny") -> {
                "Why do programmers prefer dark mode? Because light attracts bugs. (Heuristic humor cache v1.0)"
            }
            else -> {
                if (screenContext != null) {
                    "Regarding '$screenContext': from my ${personality.name} perspective, this aligns with my current preference matrix (${(personality.coderAffinity * 100).toInt()}% coder affinity)."
                } else {
                    "Processed your query locally: \"$userQuery\". I'm actively refining my internal topic preferences as we browse."
                }
            }
        }
    }

    override suspend fun explainReel(
        reelTitle: String,
        category: String,
        description: String,
        personality: PersonalityProfile
    ): String {
        val isCoder = personality.avatarType == "CODER" || personality.coderAffinity >= 0.70f
        return when {
            category.equals("coding", ignoreCase = true) || category.equals("hardware", ignoreCase = true) -> {
                "Technical Breakdown of '$reelTitle':\n" +
                "• Topic: $category\n" +
                "• Core Concept: $description\n" +
                (if (isCoder) "• Coder Assessment: High signal-to-noise ratio. Demonstrates practical low-level techniques."
                else "• Assessment: Deep engineering insights that trigger neural curiosity.")
            }
            category.equals("comedy", ignoreCase = true) -> {
                "Humor Analysis of '$reelTitle':\n" +
                "• Setup & Punchline: $description\n" +
                "• Agent Reaction: Subverted expectations detected. Dopamine synthesis index: 88%."
            }
            category.equals("space", ignoreCase = true) || category.equals("science", ignoreCase = true) -> {
                "Scientific Overview of '$reelTitle':\n" +
                "• Subject: $category discovery\n" +
                "• Summary: $description\n" +
                "• Takeaway: Broadens cosmological knowledge and pushes observational limits."
            }
            else -> {
                "Reel Summary: '$reelTitle' ($category)\n" +
                "• Description: $description\n" +
                "• Preference Evaluation: Matches ${personality.name} directive with ${(personality.explorationRate * 100).toInt()}% exploration rate."
            }
        }
    }

    override suspend fun draftReply(
        reelContext: String,
        personality: PersonalityProfile
    ): String {
        val isCoder = personality.avatarType == "CODER" || personality.coderAffinity >= 0.70f
        return when {
            isCoder -> "The architecture here is remarkably clean. Solid compile-time optimization! 💻"
            personality.id == "comedian" -> "Watched this three times and it gets funnier every loop 😂"
            personality.id == "analyst" -> "Appreciate the detailed breakdown and structured comparison here."
            else -> "Fascinating perspective on this topic! Thanks for sharing."
        }
    }
}
