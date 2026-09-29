package com.example.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.BolotaDatabase
import com.example.data.local.BolotaRepository
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val appContext = applicationContext

        // Check if notifications can be shown
        if (!BolotaNotificationHelper.canSendNotifications(appContext)) {
            return Result.success()
        }

        try {
            val database = BolotaDatabase.getInstance(appContext)
            val dao = database.bolotaDao()

            val settings = dao.getSettings()
            val petName = settings?.petName ?: "Bolota"

            val todayStr = BolotaRepository.today()
            val todayDay = dao.getDay(todayStr)

            // Calculate pending habits for today
            val cal = Calendar.getInstance()
            val todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Dom, 1=Seg, 2=Ter...

            var totalTodayHabits = 0
            var completedTodayHabits = 0

            if (settings != null && settings.goodHabitsJson.isNotBlank()) {
                try {
                    val arr = JSONArray(settings.goodHabitsJson)
                    val doneObj = if (todayDay != null && todayDay.goodDoneJson.isNotBlank()) {
                        JSONObject(todayDay.goodDoneJson)
                    } else {
                        JSONObject()
                    }

                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val id = obj.optString("id")
                        val daysArr = obj.optJSONArray("days")
                        val scheduledDays = mutableListOf<Int>()
                        if (daysArr != null) {
                            for (j in 0 until daysArr.length()) {
                                scheduledDays.add(daysArr.getInt(j))
                            }
                        }

                        // If scheduled for today (or empty list means all days)
                        if (scheduledDays.isEmpty() || scheduledDays.contains(todayDayOfWeek)) {
                            totalTodayHabits++
                            if (doneObj.optBoolean(id, false)) {
                                completedTodayHabits++
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to generic reminder
                }
            }

            val pendingCount = (totalTodayHabits - completedTodayHabits).coerceAtLeast(0)

            BolotaNotificationHelper.showDailyReminderNotification(
                context = appContext,
                petName = petName,
                pendingHabitsCount = pendingCount,
                totalHabitsCount = totalTodayHabits
            )

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
