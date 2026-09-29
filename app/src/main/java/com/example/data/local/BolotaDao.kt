package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChallengeEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DayLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BolotaDao {
    // Days
    @Query("SELECT * FROM days ORDER BY date DESC")
    fun getAllDaysFlow(): Flow<List<DayLogEntity>>

    @Query("SELECT * FROM days WHERE date = :date LIMIT 1")
    fun getDayFlow(date: String): Flow<DayLogEntity?>

    @Query("SELECT * FROM days WHERE date = :date LIMIT 1")
    suspend fun getDay(date: String): DayLogEntity?

    @Query("SELECT * FROM days ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentDays(limit: Int): List<DayLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDay(day: DayLogEntity)

    // Challenges
    @Query("SELECT * FROM challenges WHERE weekStart = :weekStart LIMIT 1")
    fun getChallengeFlow(weekStart: String): Flow<ChallengeEntity?>

    @Query("SELECT * FROM challenges WHERE weekStart = :weekStart LIMIT 1")
    suspend fun getChallenge(weekStart: String): ChallengeEntity?

    @Query("SELECT * FROM challenges ORDER BY weekStart DESC")
    fun getAllChallengesFlow(): Flow<List<ChallengeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChallenge(challenge: ChallengeEntity)

    // Chat
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    // Settings
    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: AppSettingsEntity)
}
