package com.example.engine

import com.example.model.FeedItem
import com.example.model.PersonalityProfile

data class ReelDecisionContext(
    val currentItem: FeedItem,
    val profile: PersonalityProfile,
    val currentTopicScore: Float,
    val sessionItemsSeen: Int,
    val sessionDurationSec: Long,
    val userLikedThisItem: Boolean,
    val userSavedThisItem: Boolean,
    val consecutiveSimilarTopics: Int,
    val itemsRemainingInSessionLimit: Int = 50
)
