package com.example.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "topic_preferences",
    indices = [Index(value = ["profileId", "topic"], unique = true)]
)
data class TopicPreference(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: String,
    val topic: String,
    val score: Float = 0.0f, // -1.0 (dislike) to +1.0 (strong love)
    val watches: Int = 0,
    val skips: Int = 0,
    val likes: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
