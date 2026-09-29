package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "days")
data class DayLogEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val goodDoneJson: String = "{}", // Map<String, Boolean>
    val slipsJson: String = "[]", // List<String> habit ids
    val urgesJson: String = "[]", // List<UrgeLog>
    val triggersJson: String = "[]", // List<TriggerLog>
    val sleepHours: Float? = null,
    val screenHours: Float? = null,
    val sleepQuality: Int? = null, // 1 to 5
    val mood: Int? = null, // 1 to 5
    val sleepScore: Int? = null, // Sleep Cycle %
    val steps: Int? = null,
    val restHR: Int? = null,
    val weight: Float? = null
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val weekStart: String, // Monday date "YYYY-MM-DD"
    val text: String,
    val targetDays: Int = 4,
    val doneDatesJson: String = "[]", // List<String>
    val createdTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val petName: String = "Bolota",
    val theme: String = "escuro", // "escuro", "claro", "auto"
    val screenGoal: Float = 4.0f,
    val sleepGoal: Float = 7.5f,
    val trainDaysJson: String = "[1,3,5]", // List<Int>
    val goodHabitsJson: String = "",
    val badHabitsJson: String = "",
    val wearAccessoriesJson: String = "{}", // Map<String, String> slot -> achId
    val unlockedAchievementsJson: String = "[]" // List<String>
)
