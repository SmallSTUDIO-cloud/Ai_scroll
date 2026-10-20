package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_profiles")
data class PersonalityProfile(
    @PrimaryKey
    val id: String,
    val name: String,
    val tagline: String,
    val description: String,
    val avatarType: String, // "TECH", "COMEDIAN", "EXPLORER", "ANALYST", "CODER", "CUSTOM"
    val preferredTopics: List<String>,
    val avoidTopics: List<String>,
    val explorationRate: Float = 0.35f, // 0.0 (strictly stays in comfort zone) to 1.0 (random curiosity)
    val skipThresholdSec: Float = 4.0f, // How fast to skip uninteresting content
    val watchDurationPrefSec: Float = 12.0f, // Target dwell time on high-interest content
    val likeProbability: Float = 0.65f, // Likelihood to like when interest is high
    val saveProbability: Float = 0.30f,
    val coderAffinity: Float = 0.0f, // 0.0 to 1.0. Evolves when watching/liking tech/coding reels!
    val isCustom: Boolean = false,
    val isSelected: Boolean = false
) {
    companion object {
        val PRESET_TECH_NERD = PersonalityProfile(
            id = "tech_nerd",
            name = "The Tech Nerd",
            tagline = "Compiling thoughts at clock speed",
            description = "Obsessed with programming languages, gadgets, silicon hardware, on-device AI, and scientific breakthroughs.",
            avatarType = "TECH",
            preferredTopics = listOf("coding", "technology", "ai", "hardware", "science"),
            avoidTopics = listOf("gossip", "reality_tv"),
            explorationRate = 0.25f,
            skipThresholdSec = 3.5f,
            watchDurationPrefSec = 14.0f,
            likeProbability = 0.75f,
            saveProbability = 0.45f,
            coderAffinity = 0.85f,
            isSelected = true
        )

        val PRESET_COMEDIAN = PersonalityProfile(
            id = "comedian",
            name = "The Comedian",
            tagline = "Seeking digital dopamine",
            description = "Addicted to absurdity, meme culture, funny animal bloopers, comedic skits, and unexpected twists.",
            avatarType = "COMEDIAN",
            preferredTopics = listOf("comedy", "memes", "animals", "bloopers", "gaming"),
            avoidTopics = listOf("serious_finance", "lecture"),
            explorationRate = 0.45f,
            skipThresholdSec = 2.5f,
            watchDurationPrefSec = 8.0f,
            likeProbability = 0.85f,
            saveProbability = 0.20f,
            coderAffinity = 0.10f
        )

        val PRESET_EXPLORER = PersonalityProfile(
            id = "curious_explorer",
            name = "Curious Explorer",
            tagline = "Every rabbit hole is an adventure",
            description = "Fascinated by unfamiliar subjects, deep sea biology, space oddities, artisan crafts, and global curiosities.",
            avatarType = "EXPLORER",
            preferredTopics = listOf("space", "science", "travel", "art", "nature", "cooking"),
            avoidTopics = emptyList(),
            explorationRate = 0.70f,
            skipThresholdSec = 4.0f,
            watchDurationPrefSec = 12.0f,
            likeProbability = 0.60f,
            saveProbability = 0.35f,
            coderAffinity = 0.25f
        )

        val PRESET_ANALYST = PersonalityProfile(
            id = "analyst",
            name = "The Analyst",
            tagline = "Data over drama",
            description = "Prefers rigorous breakdowns, data comparisons, architectural trade-offs, and longer educational deep-dives.",
            avatarType = "ANALYST",
            preferredTopics = listOf("science", "technology", "finance", "engineering", "hardware"),
            avoidTopics = listOf("clickbait", "dance_trends"),
            explorationRate = 0.20f,
            skipThresholdSec = 5.0f,
            watchDurationPrefSec = 18.0f,
            likeProbability = 0.50f,
            saveProbability = 0.60f,
            coderAffinity = 0.65f
        )

        val PRESET_CODER_AGENT = PersonalityProfile(
            id = "coder_agent",
            name = "Coder Agent",
            tagline = "0 errors, 0 warnings, 100% focused",
            description = "Evolved purely into software development, terminal tricks, algorithm breakdowns, and compiler optimizations.",
            avatarType = "CODER",
            preferredTopics = listOf("coding", "technology", "hardware", "ai"),
            avoidTopics = listOf("lifestyle", "cooking", "dance_trends"),
            explorationRate = 0.15f,
            skipThresholdSec = 2.0f,
            watchDurationPrefSec = 16.0f,
            likeProbability = 0.90f,
            saveProbability = 0.70f,
            coderAffinity = 1.0f
        )

        val ALL_PRESETS = listOf(
            PRESET_TECH_NERD,
            PRESET_COMEDIAN,
            PRESET_EXPLORER,
            PRESET_ANALYST,
            PRESET_CODER_AGENT
        )
    }
}
