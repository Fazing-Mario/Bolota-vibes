package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AppSettingsEntity
import com.example.data.model.ChallengeEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DayLogEntity

@Database(
    entities = [
        DayLogEntity::class,
        ChallengeEntity::class,
        ChatMessageEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BolotaDatabase : RoomDatabase() {
    abstract fun bolotaDao(): BolotaDao

    companion object {
        @Volatile
        private var INSTANCE: BolotaDatabase? = null

        fun getInstance(context: Context): BolotaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BolotaDatabase::class.java,
                    "bolota_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
