package com.example.engine

interface AgentBrain {
    val engineName: String
    val isLocalModel: Boolean
    val supportsVision: Boolean

    suspend fun decideNextAction(context: ReelDecisionContext): AgentDecision
}
