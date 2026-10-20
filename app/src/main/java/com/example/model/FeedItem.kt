package com.example.model

data class FeedItem(
    val id: String,
    val title: String,
    val category: String, // e.g. "coding", "technology", "comedy", "science", "space", "cooking", "gaming", "animals"
    val creator: String,
    val description: String,
    val durationSec: Int,
    val likesCount: Long,
    val savesCount: Long,
    val tags: List<String>,
    val generativeVisualType: String, // Canvas generative theme
    val isUserLiked: Boolean = false,
    val isUserSaved: Boolean = false,
    val isAgentLiked: Boolean = false,
    val isAgentSaved: Boolean = false
)
