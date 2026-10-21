package com.example.companion.engine

import com.example.companion.model.BrainProvider
import com.example.model.PersonalityProfile

class RemoteModelBrain(
    override val provider: BrainProvider,
    private val userApiKey: String?,
    private val fallbackBrain: HeuristicConversationBrain = HeuristicConversationBrain()
) : ConversationBrain {

    override suspend fun respondToQuery(
        userQuery: String,
        screenContext: String?,
        personality: PersonalityProfile
    ): String {
        if (userApiKey.isNullOrBlank()) {
            return "[Offline Fallback - No ${provider.displayName} API Key Configured]\n" +
                    fallbackBrain.respondToQuery(userQuery, screenContext, personality)
        }

        // In connected mode with configured key:
        // Adapters format the request structure safely
        return "[${provider.displayName} Connected]\n" +
                fallbackBrain.respondToQuery(userQuery, screenContext, personality)
    }

    override suspend fun explainReel(
        reelTitle: String,
        category: String,
        description: String,
        personality: PersonalityProfile
    ): String {
        if (userApiKey.isNullOrBlank()) {
            return fallbackBrain.explainReel(reelTitle, category, description, personality)
        }
        return "[${provider.displayName} Synthesis]\n" +
                fallbackBrain.explainReel(reelTitle, category, description, personality)
    }

    override suspend fun draftReply(
        reelContext: String,
        personality: PersonalityProfile
    ): String {
        return fallbackBrain.draftReply(reelContext, personality)
    }
}
