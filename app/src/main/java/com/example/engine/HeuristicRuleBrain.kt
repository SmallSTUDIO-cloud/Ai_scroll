package com.example.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class HeuristicRuleBrain : AgentBrain {
    override val engineName: String = "Offline Heuristic Engine"
    override val isLocalModel: Boolean = false
    override val supportsVision: Boolean = false

    override suspend fun decideNextAction(context: ReelDecisionContext): AgentDecision {
        val item = context.currentItem
        val profile = context.profile
        val topic = item.category.lowercase()

        // 1. Check if break duration or limit reached
        if (context.sessionItemsSeen >= context.itemsRemainingInSessionLimit) {
            return AgentDecision(
                action = AgentAction.STOP,
                reason = "Target break session quota completed.",
                topic = topic,
                internalMonologue = "Neural circuits refreshed. I've scrolled enough for this break session. Returning to standby.",
                confidence = 0.95f,
                dwellDurationSec = 3.0f,
                interestScore = 0.5f
            )
        }

        // 2. Base interest calculation
        var baseInterest = 0.50f

        if (profile.preferredTopics.any { it.equals(topic, ignoreCase = true) }) {
            baseInterest += 0.35f
        }
        if (profile.avoidTopics.any { it.equals(topic, ignoreCase = true) }) {
            baseInterest -= 0.45f
        }

        // Incorporate learned dynamic memory score (-1.0 to 1.0)
        baseInterest += (context.currentTopicScore * 0.25f)

        // Coder affinity bonus for tech/code
        val isCodeTopic = topic in listOf("coding", "hardware", "ai", "technology")
        if (isCodeTopic) {
            baseInterest += (profile.coderAffinity * 0.30f)
        }

        // Human interaction influence: If user liked it, agent's curiosity surges
        if (context.userLikedThisItem) {
            baseInterest += 0.25f
        }

        // Fatigue penalty for 3+ consecutive items of the same topic
        if (context.consecutiveSimilarTopics >= 3) {
            baseInterest -= 0.20f
        }

        // Exploration rate can inject novelty boost
        val roll = Random.nextFloat()
        if (roll < profile.explorationRate) {
            baseInterest = max(0.40f, baseInterest + (Random.nextFloat() * 0.25f - 0.10f))
        }

        val clampedInterest = min(1.0f, max(0.0f, baseInterest))

        // Decide action
        val willEvolveToCoder = isCodeTopic && (profile.coderAffinity > 0.5f || context.userLikedThisItem)

        return when {
            // Low interest -> Skip (SWIPE_UP)
            clampedInterest < 0.38f -> {
                val monologue = when {
                    profile.avoidTopics.contains(topic) -> "Not aligned with my directive. Skipping topic: '$topic'."
                    context.consecutiveSimilarTopics >= 3 -> "Over-indexed on '$topic'. Need entropy and variety."
                    else -> "Low relevance index (${(clampedInterest * 100).toInt()}%). Quick glance then moving forward."
                }
                AgentDecision(
                    action = AgentAction.SWIPE_UP,
                    reason = "Reel relevance below threshold (${(clampedInterest * 100).toInt()}% vs target).",
                    topic = topic,
                    internalMonologue = monologue,
                    confidence = 0.82f,
                    dwellDurationSec = profile.skipThresholdSec.coerceIn(1.5f, 4.0f),
                    interestScore = clampedInterest,
                    isEvolvingToCoder = false
                )
            }

            // High interest -> Watch + Like / Save
            clampedInterest >= 0.72f -> {
                val shouldLike = !item.isAgentLiked && Random.nextFloat() < profile.likeProbability
                val shouldSave = !item.isAgentSaved && Random.nextFloat() < profile.saveProbability

                val primaryAction = when {
                    shouldLike -> AgentAction.LIKE
                    shouldSave -> AgentAction.SAVE
                    else -> AgentAction.WAIT
                }

                val monologue = when {
                    isCodeTopic && willEvolveToCoder ->
                        "Remarkable engineering in '${item.title}'. My coder pathways are firing with high resonance."
                    context.userLikedThisItem ->
                        "Human upvoted '${item.creator}'. Synchronizing taste metrics and saving to memory."
                    primaryAction == AgentAction.LIKE ->
                        "High resonance content (${(clampedInterest * 100).toInt()}% match). Hearting '${item.title}'."
                    primaryAction == AgentAction.SAVE ->
                        "High informational density. Bookmarking for offline synthetic review."
                    else ->
                        "Absorbing details on '${item.title}'. Dwell time extended."
                }

                AgentDecision(
                    action = primaryAction,
                    reason = "High affinity for '$topic' by creator '${item.creator}'.",
                    topic = topic,
                    internalMonologue = monologue,
                    confidence = 0.90f,
                    dwellDurationSec = profile.watchDurationPrefSec.coerceIn(6.0f, 18.0f),
                    interestScore = clampedInterest,
                    isEvolvingToCoder = willEvolveToCoder
                )
            }

            // Medium interest -> Dwell & Watch
            else -> {
                AgentDecision(
                    action = AgentAction.WAIT,
                    reason = "Moderate interest in '$topic'. Observing before choosing next action.",
                    topic = topic,
                    internalMonologue = "Evaluating '${item.title}' (${(clampedInterest * 100).toInt()}% interest). Watching brief preview.",
                    confidence = 0.65f,
                    dwellDurationSec = (profile.watchDurationPrefSec * 0.6f).coerceIn(3.5f, 8.0f),
                    interestScore = clampedInterest,
                    isEvolvingToCoder = isCodeTopic
                )
            }
        }
    }
}
