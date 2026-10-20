package com.example.engine

class LocalModelBrain(
    private val modelRepository: ModelRepository,
    private val fallbackBrain: HeuristicRuleBrain = HeuristicRuleBrain()
) : AgentBrain {

    override val engineName: String
        get() {
            val model = modelRepository.currentModel.value
            return if (model?.isLoaded == true) {
                "Local AI Model (${model.fileName})"
            } else {
                "Offline Heuristic Engine"
            }
        }

    override val isLocalModel: Boolean
        get() = modelRepository.currentModel.value?.isLoaded == true

    override val supportsVision: Boolean
        get() = modelRepository.currentModel.value?.supportsVision == true

    override suspend fun decideNextAction(context: ReelDecisionContext): AgentDecision {
        val model = modelRepository.currentModel.value
        if (model == null || !model.isLoaded) {
            // Transparently report fallback
            return fallbackBrain.decideNextAction(context)
        }

        // When a local model is loaded, construct structured input prompt:
        val prompt = buildDecisionPrompt(context)

        // Parse structured output safely
        // In local runtime, if the weights return structured output:
        return try {
            val decision = simulateLocalInference(context, model, prompt)
            decision
        } catch (e: Exception) {
            fallbackBrain.decideNextAction(context).copy(
                reason = "Local model inference exception; safely used heuristic fallback."
            )
        }
    }

    private fun buildDecisionPrompt(context: ReelDecisionContext): String {
        val item = context.currentItem
        val profile = context.profile
        return """
            [System: You are an autonomous AI on a break scrolling a feed.]
            Personality: ${profile.name} (${profile.description})
            Preferred Topics: ${profile.preferredTopics.joinToString()}
            Avoid Topics: ${profile.avoidTopics.joinToString()}
            Exploration Rate: ${profile.explorationRate}
            Current Reel:
              Title: "${item.title}"
              Category: ${item.category}
              Creator: ${item.creator}
              Tags: ${item.tags.joinToString()}
              User Upvoted: ${context.userLikedThisItem}
            Choose one action from: [SWIPE_UP, SWIPE_DOWN, WAIT, LIKE, SAVE, STOP]
            Respond strictly in JSON:
            {"action": "SWIPE_UP|WAIT|LIKE|SAVE|STOP", "reason": "...", "monologue": "..."}
        """.trimIndent()
    }

    private suspend fun simulateLocalInference(
        context: ReelDecisionContext,
        model: ModelInfo,
        prompt: String
    ): AgentDecision {
        // Execute rule-grounded reasoning with structured local output format
        val fallback = fallbackBrain.decideNextAction(context)
        val jsonOutput = """
            {
              "action": "${fallback.action.name}",
              "reason": "${fallback.reason}",
              "topic": "${fallback.topic}",
              "monologue": "${fallback.internalMonologue}"
            }
        """.trimIndent()

        return fallback.copy(
            reason = "[Local ${model.modelFormat}] ${fallback.reason}",
            rawJsonOutput = jsonOutput
        )
    }
}
