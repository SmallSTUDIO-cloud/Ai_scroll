package com.example.model

/**
 * Encapsulates live perceived content extracted from active external reel apps
 * (Instagram, YouTube Shorts, TikTok, Reddit) via the Accessibility Node tree.
 */
data class ExtractedReelInfo(
    val packageName: String,
    val appName: String,
    val creator: String? = null,
    val caption: String? = null,
    val audioTitle: String? = null,
    val rawVisibleTexts: List<String> = emptyList(),
    val isLikelyGirlOrAesthetic: Boolean = false,
    val isHumorOrMeme: Boolean = false,
    val isTechOrCoding: Boolean = false,
    val isMusicOrDance: Boolean = false
) {
    /**
     * Produces a clean, concise single-line summary of what is currently on the screen.
     */
    fun getSummary(): String {
        return when {
            !caption.isNullOrBlank() && !creator.isNullOrBlank() -> "@$creator: \"${caption.take(65)}\""
            !caption.isNullOrBlank() -> "\"${caption.take(75)}\""
            !creator.isNullOrBlank() -> "Reel by @$creator"
            !audioTitle.isNullOrBlank() -> "Audio: $audioTitle"
            rawVisibleTexts.isNotEmpty() -> rawVisibleTexts.first().take(60)
            else -> "$appName Reel Feed"
        }
    }
}
