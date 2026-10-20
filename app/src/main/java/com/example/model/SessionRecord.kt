package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_records")
data class SessionRecord(
    @PrimaryKey
    val sessionId: String,
    val profileId: String,
    val profileName: String,
    val startTime: Long,
    val durationSeconds: Long,
    val engineType: String, // "HEURISTIC_SIM" or "LOCAL_MODEL"
    val totalSeen: Int,
    val totalLiked: Int,
    val totalSaved: Int,
    val totalSkipped: Int,
    val topTopic: String,
    val summaryHeadline: String,
    val detailedObservations: String,
    val coderAffinityScore: Float
)
