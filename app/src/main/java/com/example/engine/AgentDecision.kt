package com.example.engine

data class AgentDecision(
    val action: AgentAction,
    val reason: String,
    val topic: String,
    val internalMonologue: String,
    val confidence: Float,
    val dwellDurationSec: Float,
    val interestScore: Float,
    val isEvolvingToCoder: Boolean = false,
    val rawJsonOutput: String? = null
)
