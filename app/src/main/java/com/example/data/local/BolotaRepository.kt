package com.example.data.local

import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class BolotaRepository(private val dao: BolotaDao) {

    val allDaysFlow: Flow<List<DayLogEntity>> = dao.getAllDaysFlow()
    val allChatMessagesFlow: Flow<List<ChatMessageEntity>> = dao.getAllChatMessagesFlow()
    val settingsFlow: Flow<AppSettingsEntity?> = dao.getSettingsFlow()

    companion object {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        fun today(): String = dateFormat.format(Date())

        fun daysBetween(fromDate: String, toDate: String): Int {
            return try {
                val f = dateFormat.parse(fromDate)?.time ?: return 0
                val t = dateFormat.parse(toDate)?.time ?: return 0
                val diff = (t - f) / (1000L * 60 * 60 * 24)
                max(0, diff.toInt())
            } catch (e: Exception) {
                0
            }
        }

        fun getMondayOfWeek(date: Date = Date()): String {
            val cal = Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Sun, 2=Mon...
            val daysFromMonday = (dayOfWeek + 5) % 7
            cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday)
            return dateFormat.format(cal.time)
        }

        fun getPastDate(daysAgo: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            return dateFormat.format(cal.time)
        }
    }

    suspend fun initializeDefaultsIfNeeded() {
        val currentSettings = dao.getSettings()
        if (currentSettings == null) {
            val t = today()
            val initialGoodHabits = listOf(
                GoodHabit(id = "treino", label = "Treino", days = listOf(1, 3, 5)),
                GoodHabit(id = "agua", label = "Água em dia", days = emptyList()),
                GoodHabit(id = "foco", label = "Bloco de foco", days = emptyList()),
                GoodHabit(id = "dormir", label = "Deitar até 22:30", days = emptyList())
            )
            val initialBadHabits = listOf(
                BadHabit(
                    id = "doces",
                    label = "Salgadinhos e doces",
                    since = t,
                    best = 0,
                    plan = IfThenPlan(
                        se = "bater vontade de doce depois do almoço",
                        entao = "como uma fruta e escovo os dentes"
                    )
                ),
                BadHabit(
                    id = "privado",
                    label = "Hábito privado",
                    since = t,
                    best = 0,
                    plan = IfThenPlan(
                        se = "estiver na cama com o celular",
                        entao = "deixo o celular carregando fora do quarto"
                    )
                ),
                BadHabit(
                    id = "procrast",
                    label = "Procrastinação",
                    since = t,
                    best = 0,
                    plan = IfThenPlan(
                        se = "abrir rede social na hora de estudar",
                        entao = "fecho o app e faço 2 minutos da tarefa"
                    )
                )
            )

            val goodJson = JSONArray()
            initialGoodHabits.forEach { h ->
                val o = JSONObject()
                o.put("id", h.id)
                o.put("label", h.label)
                o.put("days", JSONArray(h.days))
                goodJson.put(o)
            }

            val badJson = JSONArray()
            initialBadHabits.forEach { b ->
                val o = JSONObject()
                o.put("id", b.id)
                o.put("label", b.label)
                o.put("since", b.since)
                o.put("best", b.best)
                if (b.plan != null) {
                    val p = JSONObject()
                    p.put("se", b.plan.se)
                    p.put("entao", b.plan.entao)
                    o.put("plan", p)
                }
                badJson.put(o)
            }

            dao.insertOrUpdateSettings(
                AppSettingsEntity(
                    id = 1,
                    petName = "Bolota",
                    theme = "escuro",
                    screenGoal = 4.0f,
                    sleepGoal = 7.5f,
                    trainDaysJson = "[1,3,5]",
                    goodHabitsJson = goodJson.toString(),
                    badHabitsJson = badJson.toString(),
                    wearAccessoriesJson = "{}",
                    unlockedAchievementsJson = "[]"
                )
            )
        }

        // Initialize today if not present
        val t = today()
        if (dao.getDay(t) == null) {
            dao.insertOrUpdateDay(DayLogEntity(date = t))
        }

        // Initialize weekly challenge if not present
        val monday = getMondayOfWeek()
        if (dao.getChallenge(monday) == null) {
            dao.insertOrUpdateChallenge(
                ChallengeEntity(
                    weekStart = monday,
                    text = "Celular fora do quarto na hora de dormir",
                    targetDays = 4,
                    doneDatesJson = "[]"
                )
            )
        }
    }

    suspend fun getTodayLog(): DayLogEntity {
        val t = today()
        return dao.getDay(t) ?: run {
            val newDay = DayLogEntity(date = t)
            dao.insertOrUpdateDay(newDay)
            newDay
        }
    }

    suspend fun updateDayLog(day: DayLogEntity) {
        dao.insertOrUpdateDay(day)
    }

    suspend fun updateSettings(settings: AppSettingsEntity) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun getSettings(): AppSettingsEntity {
        return dao.getSettings() ?: run {
            initializeDefaultsIfNeeded()
            dao.getSettings()!!
        }
    }

    suspend fun getChallengeForCurrentWeek(): ChallengeEntity {
        val monday = getMondayOfWeek()
        return dao.getChallenge(monday) ?: run {
            val newChal = ChallengeEntity(
                weekStart = monday,
                text = "Celular carregando fora do quarto na hora de dormir",
                targetDays = 4,
                doneDatesJson = "[]"
            )
            dao.insertOrUpdateChallenge(newChal)
            newChal
        }
    }

    suspend fun updateChallenge(challenge: ChallengeEntity) {
        dao.insertOrUpdateChallenge(challenge)
    }

    suspend fun insertChatMessage(role: String, content: String) {
        dao.insertChatMessage(ChatMessageEntity(role = role, content = content))
    }

    suspend fun clearChat() {
        dao.clearChatMessages()
    }

    // Parsing helpers
    fun parseGoodHabits(json: String): List<GoodHabit> {
        val list = mutableListOf<GoodHabit>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val daysArr = o.optJSONArray("days")
                val daysList = mutableListOf<Int>()
                if (daysArr != null) {
                    for (j in 0 until daysArr.length()) daysList.add(daysArr.getInt(j))
                }
                list.add(
                    GoodHabit(
                        id = o.getString("id"),
                        label = o.getString("label"),
                        days = daysList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseBadHabits(json: String): List<BadHabit> {
        val list = mutableListOf<BadHabit>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                var plan: IfThenPlan? = null
                val p = o.optJSONObject("plan")
                if (p != null) {
                    plan = IfThenPlan(se = p.optString("se", ""), entao = p.optString("entao", ""))
                }
                list.add(
                    BadHabit(
                        id = o.getString("id"),
                        label = o.getString("label"),
                        since = o.optString("since", today()),
                        best = o.optInt("best", 0),
                        plan = plan
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseGoodDone(json: String): Map<String, Boolean> {
        val map = mutableMapOf<String, Boolean>()
        if (json.isBlank()) return map
        try {
            val o = JSONObject(json)
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = o.optBoolean(k, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun parseSlips(json: String): List<String> {
        val list = mutableListOf<String>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) list.add(arr.getString(i))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseUrges(json: String): List<UrgeLog> {
        val list = mutableListOf<UrgeLog>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    UrgeLog(
                        habitId = o.getString("habitId"),
                        outcome = o.getString("outcome"),
                        timestamp = o.optLong("timestamp", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseTriggers(json: String): List<TriggerLog> {
        val list = mutableListOf<TriggerLog>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    TriggerLog(
                        habitId = o.getString("habitId"),
                        trigger = o.getString("trigger"),
                        kind = o.getString("kind"),
                        timestamp = o.optLong("timestamp", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseWearAccessories(json: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (json.isBlank()) return map
        try {
            val o = JSONObject(json)
            val keys = o.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = o.getString(k)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun parseUnlockedAchievements(json: String): List<String> {
        val list = mutableListOf<String>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) list.add(arr.getString(i))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseDoneDates(json: String): List<String> {
        val list = mutableListOf<String>()
        if (json.isBlank()) return list
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) list.add(arr.getString(i))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Calculations
    suspend fun calculateTotalXp(): Int {
        val days = dao.getAllDaysFlow().first()
        val settings = getSettings()
        val badHabits = parseBadHabits(settings.badHabitsJson)
        val challenges = dao.getAllChallengesFlow().first()

        var xp = 0
        for (d in days) {
            val goodMap = parseGoodDone(d.goodDoneJson)
            xp += goodMap.values.count { it } * 15
            if (d.sleepHours != null || d.sleepQuality != null) xp += 10
            if (d.screenHours != null) xp += 10
            if (d.mood != null) xp += 10
            val urges = parseUrges(d.urgesJson)
            xp += urges.count { it.outcome == "resisti" } * 30
        }

        for (b in badHabits) {
            val clean = daysBetween(b.since, today())
            xp += clean * 10
        }

        for (c in challenges) {
            val done = parseDoneDates(c.doneDatesJson)
            xp += done.size * 20
            if (done.size >= c.targetDays) {
                xp += 50 // Bonus for completing challenge
            }
        }

        return xp
    }

    fun calculateSpirit(
        todayLog: DayLogEntity?,
        yesterdayLog: DayLogEntity?,
        goodHabits: List<GoodHabit>,
        badHabits: List<BadHabit>
    ): Int {
        var v = 55
        if (todayLog != null) {
            val goodDone = parseGoodDone(todayLog.goodDoneJson)
            val doneCount = goodHabits.count { goodDone[it.id] == true }
            v += doneCount * 8
            if (todayLog.sleepQuality != null) v += (todayLog.sleepQuality - 3) * 4
            if (todayLog.mood != null) v += (todayLog.mood - 3) * 4
            val urges = parseUrges(todayLog.urgesJson)
            v += urges.count { it.outcome == "resisti" } * 8
            val slips = parseSlips(todayLog.slipsJson)
            v -= slips.size * 18
        }
        if (yesterdayLog != null) {
            val ySlips = parseSlips(yesterdayLog.slipsJson)
            v -= ySlips.size * 8
        }
        if (badHabits.isNotEmpty()) {
            val avgClean = badHabits.map { daysBetween(it.since, today()) }.average()
            v += min(12.0, avgClean).toInt()
        }
        return v.coerceIn(5, 100)
    }

    fun getPetMood(spirit: Int, hasSlipsToday: Boolean): String {
        if (hasSlipsToday) return "firme"
        return when {
            spirit >= 80 -> "radiante"
            spirit >= 58 -> "feliz"
            spirit >= 35 -> "ok"
            else -> "triste"
        }
    }
}
