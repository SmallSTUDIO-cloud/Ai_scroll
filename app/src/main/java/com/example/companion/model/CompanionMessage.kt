package com.example.companion.model

enum class MessageSender {
    USER,
    COMPANION,
    SYSTEM
}

data class CompanionMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reelContext: String? = null,
    val isDraftReply: Boolean = false
)
