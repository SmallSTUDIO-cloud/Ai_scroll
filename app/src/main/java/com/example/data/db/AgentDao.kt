package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.model.PersonalityProfile
import com.example.model.SessionRecord
import com.example.model.TopicPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentDao {

    // Profiles
    @Query("SELECT * FROM agent_profiles ORDER BY isSelected DESC, name ASC")
    fun getAllProfiles(): Flow<List<PersonalityProfile>>

    @Query("SELECT * FROM agent_profiles WHERE isSelected = 1 LIMIT 1")
    fun getSelectedProfile(): Flow<PersonalityProfile?>

    @Query("SELECT * FROM agent_profiles WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedProfileSync(): PersonalityProfile?

    @Query("SELECT * FROM agent_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): PersonalityProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: PersonalityProfile)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProfiles(profiles: List<PersonalityProfile>)

    @Update
    suspend fun updateProfile(profile: PersonalityProfile)

    @Query("UPDATE agent_profiles SET isSelected = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun setSelectedProfile(selectedId: String)

    @Query("DELETE FROM agent_profiles WHERE id = :id AND isCustom = 1")
    suspend fun deleteCustomProfile(id: String)

    // Topic Preferences (Dynamic Memory)
    @Query("SELECT * FROM topic_preferences WHERE profileId = :profileId ORDER BY score DESC")
    fun getPreferencesForProfile(profileId: String): Flow<List<TopicPreference>>

    @Query("SELECT * FROM topic_preferences WHERE profileId = :profileId AND topic = :topic LIMIT 1")
    suspend fun getTopicPreference(profileId: String, topic: String): TopicPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(preference: TopicPreference)

    @Query("DELETE FROM topic_preferences WHERE profileId = :profileId")
    suspend fun resetPreferencesForProfile(profileId: String)

    @Query("DELETE FROM topic_preferences")
    suspend fun clearAllPreferences()

    // Session Records
    @Query("SELECT * FROM session_records ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionRecord>>

    @Query("SELECT * FROM session_records WHERE sessionId = :sessionId LIMIT 1")
    fun getSessionById(sessionId: String): Flow<SessionRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionRecord)

    @Query("DELETE FROM session_records WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM session_records")
    suspend fun clearAllSessions()

    @Transaction
    suspend fun resetAllData() {
        clearAllPreferences()
        clearAllSessions()
    }
}
