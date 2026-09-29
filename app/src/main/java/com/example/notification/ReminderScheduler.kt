package com.example.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    const val UNIQUE_PERIODIC_WORK_NAME = "bolota_daily_reminder_work"
    const val UNIQUE_TEST_WORK_NAME = "bolota_test_reminder_work"

    private const val PREFS_NAME = "bolota_notification_prefs"
    private const val KEY_ENABLED = "reminders_enabled"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_MINUTE = "reminder_minute"

    fun isRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ENABLED, true)
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_HOUR, 20) // Default 20:00 (8 PM)
        val minute = prefs.getInt(KEY_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setReminderPreferences(context: Context, enabled: Boolean, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putInt(KEY_HOUR, hour)
            .putInt(KEY_MINUTE, minute)
            .apply()

        if (enabled) {
            scheduleDailyReminder(context, hour, minute)
        } else {
            cancelDailyReminder(context)
        }
    }

    fun scheduleDailyReminder(context: Context, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelayMs = target.timeInMillis - now.timeInMillis

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
            .addTag("bolota_reminder")
            .build()

        try {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDailyReminder(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_PERIODIC_WORK_NAME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerImmediateTestReminder(context: Context) {
        val testWorkRequest = OneTimeWorkRequestBuilder<DailyReminderWorker>()
            .addTag("bolota_test_reminder")
            .build()

        try {
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_TEST_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                testWorkRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
