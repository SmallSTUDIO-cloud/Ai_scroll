package com.example.model

/**
 * Encapsulates multi-metric real-time screen perception extracted from active reel applications
 * (Instagram, YouTube Shorts, TikTok, Reddit) via the Accessibility Node tree.
 */
data class ExtractedReelInfo(
    val packageName: String,
    val appName: String,
    val creator: String? = null,
    val caption: String? = null,
    val audioTitle: String? = null,
    val likeCountText: String? = null,
    val commentCountText: String? = null,
    val shareCountText: String? = null,
    val viewCountText: String? = null,
    val rawVisibleTexts: List<String> = emptyList(),
    val isLikelyGirlOrAesthetic: Boolean = false,
    val isHumorOrMeme: Boolean = false,
    val isTechOrCoding: Boolean = false,
    val isMusicOrDance: Boolean = false,
    val isEducationalOrStory: Boolean = false
) {
    /**
     * Determines whether this video has high engagement metrics based on detected like/share/comment counts.
     */
    val isHighEngagement: Boolean
        get() {
            val combined = "${likeCountText.orEmpty()} ${shareCountText.orEmpty()} ${commentCountText.orEmpty()} ${viewCountText.orEmpty()}".uppercase()
            return combined.contains("M") || combined.contains("K")
        }

    /**
     * Calculates an adaptive watch duration in seconds:
     * - Captivating reels (rich educational/story captions, coding insights, stunning aesthetics, or high viral engagement): 18-35 seconds (watches full video)
     * - Standard reels: 7-12 seconds
     * - Low-signal or rapid clips: 5-7 seconds
     */
    fun computeAdaptiveWatchDurationSeconds(): Int {
        val captionLength = caption?.length ?: 0
        return when {
            // Long educational / coding / story / aesthetic deep dive -> Watch fully
            isTechOrCoding || isEducationalOrStory || (captionLength > 120 && isHighEngagement) -> {
                (20..32).random()
            }
            isLikelyGirlOrAesthetic -> {
                // User explicit preference for cute/beautiful girl aesthetic -> linger longer to appreciate
                (16..26).random()
            }
            isHumorOrMeme -> {
                // Meme punchline pacing
                (8..14).random()
            }
            captionLength > 50 -> {
                (10..16).random()
            }
            else -> {
                (5..8).random()
            }
        }
    }

    /**
     * Produces a clean, concise single-line summary of what is currently on screen.
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
