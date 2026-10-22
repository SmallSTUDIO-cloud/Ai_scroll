package com.example.companion.engine

import com.example.BuildConfig
import com.example.companion.model.BrainProvider
import com.example.model.ExtractedReelInfo
import com.example.model.PersonalityProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiReelBrain(
    private val customApiKey: String? = null
) : ConversationBrain {

    override val provider: BrainProvider = BrainProvider.GEMINI_ONLINE
    private val fallbackBrain = HeuristicConversationBrain()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(18, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    private fun resolveApiKey(): String {
        val custom = customApiKey?.trim()
        if (!custom.isNullOrEmpty() && custom != "MY_GEMINI_API_KEY") {
            return custom
        }
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    override suspend fun respondToQuery(
        userQuery: String,
        screenContext: String?,
        personality: PersonalityProfile
    ): String = withContext(Dispatchers.IO) {
        val key = resolveApiKey()
        if (key.isBlank()) {
            return@withContext fallbackBrain.respondToQuery(userQuery, screenContext, personality)
        }

        try {
            val systemPrompt = "You are ${personality.name}, an AI companion watching social reels with the user. " +
                    "Coder Affinity: ${(personality.coderAffinity * 100).toInt()}%. Exploration: ${(personality.explorationRate * 100).toInt()}%. " +
                    "Keep answers concise (under 2 sentences), authentic, conversational, and energetic."

            val prompt = if (screenContext != null) {
                "Active Screen Context: $screenContext\nUser asked: $userQuery"
            } else {
                userQuery
            }

            val result = queryGemini(key, systemPrompt, prompt)
            result ?: fallbackBrain.respondToQuery(userQuery, screenContext, personality)
        } catch (e: Exception) {
            fallbackBrain.respondToQuery(userQuery, screenContext, personality)
        }
    }

    override suspend fun explainReel(
        reelTitle: String,
        category: String,
        description: String,
        personality: PersonalityProfile
    ): String = withContext(Dispatchers.IO) {
        val key = resolveApiKey()
        if (key.isBlank()) {
            return@withContext fallbackBrain.explainReel(reelTitle, category, description, personality)
        }

        try {
            val systemPrompt = "You are ${personality.name}, analyzing a social media reel. Provide a 2-bullet punchy breakdown of the video's core concept and takeaway."
            val userPrompt = "Reel: $reelTitle\nCategory: $category\nDescription: $description"
            val result = queryGemini(key, systemPrompt, userPrompt)
            result ?: fallbackBrain.explainReel(reelTitle, category, description, personality)
        } catch (e: Exception) {
            fallbackBrain.explainReel(reelTitle, category, description, personality)
        }
    }

    override suspend fun draftReply(
        reelContext: String,
        personality: PersonalityProfile
    ): String = withContext(Dispatchers.IO) {
        val key = resolveApiKey()
        if (key.isBlank()) {
            return@withContext fallbackBrain.draftReply(reelContext, personality)
        }

        try {
            val systemPrompt = "You are a real human commenting on a viral reel. Draft a single, natural, 1-line comment (under 12 words) with appropriate tone and emojis. Avoid sounding like a bot."
            val userPrompt = "Reel context: $reelContext"
            val result = queryGemini(key, systemPrompt, userPrompt)
            result?.trim()?.removeSurrounding("\"") ?: fallbackBrain.draftReply(reelContext, personality)
        } catch (e: Exception) {
            fallbackBrain.draftReply(reelContext, personality)
        }
    }

    /**
     * Synthesizes an intelligent, context-aware comment based on the live reel data,
     * captions, metrics, and community discussion.
     */
    suspend fun synthesizeSmartComment(
        reel: ExtractedReelInfo,
        topComments: List<String>,
        personality: PersonalityProfile
    ): String = withContext(Dispatchers.IO) {
        val key = resolveApiKey()
        if (key.isBlank()) {
            return@withContext generateHeuristicSmartComment(reel, topComments, personality)
        }

        try {
            val systemPrompt = "Draft a genuine 1-line comment for this reel that sounds 100% human. " +
                    "Under 12 words. Do NOT use hashtags. Include 1 emoji. React specifically to the video topic or comments."
            val userPrompt = buildString {
                append("Creator: @${reel.creator ?: "unknown"}\n")
                if (!reel.caption.isNullOrBlank()) append("Caption: ${reel.caption}\n")
                if (!reel.audioTitle.isNullOrBlank()) append("Audio: ${reel.audioTitle}\n")
                if (topComments.isNotEmpty()) append("Top comment: \"${topComments.first()}\"\n")
                if (reel.isLikelyGirlOrAesthetic) append("Theme: Aesthetic / Beauty / Fashion\n")
                if (reel.isTechOrCoding) append("Theme: Programming / Software\n")
                if (reel.isHumorOrMeme) append("Theme: Comedy / Meme\n")
            }

            val result = queryGemini(key, systemPrompt, userPrompt)
            result?.trim()?.removeSurrounding("\"") ?: generateHeuristicSmartComment(reel, topComments, personality)
        } catch (e: Exception) {
            generateHeuristicSmartComment(reel, topComments, personality)
        }
    }

    private fun generateHeuristicSmartComment(
        reel: ExtractedReelInfo,
        topComments: List<String>,
        personality: PersonalityProfile
    ): String {
        return when {
            reel.isLikelyGirlOrAesthetic -> {
                listOf(
                    "The aesthetic on this is unreal ✨",
                    "Why is this so clean though? 10/10 ✨",
                    "The lighting and transition was immaculate 🤩",
                    "Genuinely gorgeous vibes here ❤️"
                ).random()
            }
            reel.isTechOrCoding -> {
                listOf(
                    "Clean architecture and workflow! Taking notes 💻🔥",
                    "High signal-to-noise ratio. Really appreciate this insight 🧠",
                    "That terminal and editor setup is super clean 💻",
                    "Solid compiler optimization trick right there 🧠"
                ).random()
            }
            reel.isHumorOrMeme -> {
                listOf(
                    "Bro the ending caught me completely off guard 💀😭",
                    "Not me watching this 4 times in a row 😭😂",
                    "Why is this so relatable though 💀",
                    "The timing on that punchline was 10/10 😂"
                ).random()
            }
            topComments.isNotEmpty() && topComments.first().contains("💀") -> {
                "Bro the comments are ruthless today 💀"
            }
            !reel.creator.isNullOrBlank() -> {
                "@${reel.creator} never misses with these edits 🔥"
            }
            else -> {
                "Algorithm blessed my feed with this one ✨"
            }
        }
    }

    private fun queryGemini(apiKey: String, systemPrompt: String, userText: String): String? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userText) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 120)
            })
        }

        val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: return null

        if (!response.isSuccessful) return null

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates") ?: return null
        val firstCandidate = candidates.optJSONObject(0) ?: return null
        val content = firstCandidate.optJSONObject("content") ?: return null
        val parts = content.optJSONArray("parts") ?: return null
        val firstPart = parts.optJSONObject(0) ?: return null
        return firstPart.optString("text")?.trim()
    }
}
