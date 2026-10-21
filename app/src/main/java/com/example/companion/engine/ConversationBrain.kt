package com.example.companion.engine

import com.example.companion.model.BrainProvider
import com.example.model.PersonalityProfile

interface ConversationBrain {
    val provider: BrainProvider

    suspend fun respondToQuery(
        userQuery: String,
        screenContext: String?,
        personality: PersonalityProfile
    ): String

    suspend fun explainReel(
        reelTitle: String,
        category: String,
        description: String,
        personality: PersonalityProfile
    ): String

    suspend fun draftReply(
        reelContext: String,
        personality: PersonalityProfile
    ): String
}
