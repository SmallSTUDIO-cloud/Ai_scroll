package com.example.companion.model

enum class CompanionState(val displayName: String) {
    IDLE("Idle"),
    LISTENING("Listening..."),
    THINKING("Thinking..."),
    ANALYZING_SCREEN("Analyzing Screen..."),
    SPEAKING("Speaking..."),
    HAPPY("Excited"),
    CURIOUS("Curious"),
    ERROR("Unavailable")
}

enum class BrainProvider(val displayName: String, val isOnline: Boolean) {
    HEURISTIC_LOCAL("Heuristic (100% Offline)", false),
    LOCAL_MODEL("Local Model (GGUF/TFLite)", false),
    OPENAI("OpenAI (ChatGPT)", true),
    ANTHROPIC("Anthropic (Claude)", true)
}
