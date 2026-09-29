package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiClient
import com.example.data.local.BolotaDatabase
import com.example.data.local.BolotaRepository
import com.example.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.max

data class BolotaUiState(
    val todayDate: String = "",
    val petName: String = "Bolota",
    val themeMode: String = "escuro",
    val screenGoal: Float = 4.0f,
    val sleepGoal: Float = 7.5f,
    val spirit: Int = 60,
    val totalXp: Int = 0,
    val level: Int = 1,
    val levelTitle: String = "Iniciante dos Hábitos",
    val currentLevelXp: Int = 0,
    val minLevelXp: Int = 0,
    val maxLevelXp: Int = 150,
    val levelProgress: Float = 0f,
    val xpToNextLevel: Int = 150,
    val levelUpCelebration: LevelTitle? = null,
    val floatingXpEvent: Int? = null,
    val petMood: String = "feliz",
    val petBubbleLine: String = "Oi! Se bater vontade de besteira, aperta o botão vermelho antes, não depois.",
    val isPetBubbleFirm: Boolean = false,
    val goodHabits: List<GoodHabit> = emptyList(),
    val todayGoodDone: Map<String, Boolean> = emptyMap(),
    val badHabits: List<BadHabit> = emptyList(),
    val cleanDaysMap: Map<String, Int> = emptyMap(),
    val todayLog: DayLogEntity = DayLogEntity(date = ""),
    val recentDays: List<DayLogEntity> = emptyList(),
    val currentChallenge: ChallengeEntity? = null,
    val challengeDoneDays: List<String> = emptyList(),
    val isChallengeDoneToday: Boolean = false,
    val wornAccessories: Map<String, String> = emptyMap(),
    val unlockedAchievements: Set<String> = emptySet(),
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val isChatThinking: Boolean = false,
    val isSosOpen: Boolean = false,
    val triggerPrompt: Pair<String, String>? = null, // habitId, kind
    val toastMessage: String? = null
)

class BolotaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BolotaRepository
    private var previousLevel: Int? = null

    private val _uiState = MutableStateFlow(BolotaUiState())
    val uiState: StateFlow<BolotaUiState> = _uiState.asStateFlow()

    init {
        val database = BolotaDatabase.getInstance(application)
        repository = BolotaRepository(database.bolotaDao())

        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            observeData()
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                repository.settingsFlow.filterNotNull(),
                repository.allDaysFlow,
                repository.allChatMessagesFlow
            ) { settings, days, chatMessages ->
                val todayStr = BolotaRepository.today()
                val todayDay = days.find { it.date == todayStr } ?: DayLogEntity(date = todayStr)
                val yesterdayStr = BolotaRepository.getPastDate(1)
                val yesterdayDay = days.find { it.date == yesterdayStr }

                val goodHabits = repository.parseGoodHabits(settings.goodHabitsJson)
                val badHabits = repository.parseBadHabits(settings.badHabitsJson)
                val goodDone = repository.parseGoodDone(todayDay.goodDoneJson)
                val slips = repository.parseSlips(todayDay.slipsJson)
                val worn = repository.parseWearAccessories(settings.wearAccessoriesJson)
                val unlocked = repository.parseUnlockedAchievements(settings.unlockedAchievementsJson).toSet()

                val cleanDaysMap = badHabits.associate { it.id to BolotaRepository.daysBetween(it.since, todayStr) }

                // Check achievements automatically
                val newUnlocked = unlocked.toMutableSet()
                val daysLoggedCount = days.count { d ->
                    repository.parseGoodDone(d.goodDoneJson).values.any { it } ||
                            d.sleepHours != null || d.sleepQuality != null || d.mood != null || d.screenHours != null
                }
                val bestBadStreak = badHabits.maxOfOrNull { max(it.best, cleanDaysMap[it.id] ?: 0) } ?: 0
                val goodCounts = mutableMapOf<String, Int>()
                days.forEach { d ->
                    val done = repository.parseGoodDone(d.goodDoneJson)
                    done.forEach { (k, v) -> if (v) goodCounts[k] = (goodCounts[k] ?: 0) + 1 }
                }
                val totalResisted = days.sumOf { d ->
                    repository.parseUrges(d.urgesJson).count { it.outcome == "resisti" }
                }

                AchievementsList.ALL.forEach { ach ->
                    val prog = when (ach.id) {
                        "laco" -> daysLoggedCount
                        "oculos" -> bestBadStreak
                        "gorro" -> goodCounts["dormir"] ?: 0
                        "faixa" -> goodCounts["treino"] ?: 0
                        "capelo" -> goodCounts["foco"] ?: 0
                        "escudo" -> totalResisted
                        "cachecol" -> bestBadStreak
                        "medalha" -> bestBadStreak
                        "coroa" -> bestBadStreak
                        else -> 0
                    }
                    if (prog >= ach.need && !newUnlocked.contains(ach.id)) {
                        newUnlocked.add(ach.id)
                    }
                }

                val totalXp = repository.calculateTotalXp()
                val level = LevelSystem.getLevelFromXp(totalXp)
                val levelTitle = LevelSystem.getTitleForLevel(level)
                val (minXp, maxXp) = LevelSystem.getXpRangeForLevel(level)
                val currentLevelXp = (totalXp - minXp).coerceAtLeast(0)
                val levelSpan = (maxXp - minXp).coerceAtLeast(1)
                val levelProgress = (currentLevelXp.toFloat() / levelSpan).coerceIn(0f, 1f)
                val xpToNextLevel = max(0, maxXp - totalXp)

                // Auto unlock customization items for current level
                LevelSystem.ALL_ITEMS.forEach { item ->
                    if (item.requiredLevel <= level && !newUnlocked.contains(item.id)) {
                        newUnlocked.add(item.id)
                    }
                }

                if (newUnlocked.size != unlocked.size) {
                    val updatedSettings = settings.copy(
                        unlockedAchievementsJson = JSONArray(newUnlocked.toList()).toString()
                    )
                    repository.updateSettings(updatedSettings)
                }

                var levelUpEvent: LevelTitle? = null
                if (previousLevel != null && level > previousLevel!!) {
                    levelUpEvent = LevelSystem.TITLES.find { it.level == level }
                }
                previousLevel = level

                val spirit = repository.calculateSpirit(todayDay, yesterdayDay, goodHabits, badHabits)
                val petMood = repository.getPetMood(spirit, slips.isNotEmpty())

                val bubble = computePetBubbleLine(
                    petName = settings.petName,
                    todayLog = todayDay,
                    goodHabits = goodHabits,
                    goodDone = goodDone,
                    badHabits = badHabits,
                    cleanDaysMap = cleanDaysMap
                )

                val challenge = repository.getChallengeForCurrentWeek()
                val doneDates = repository.parseDoneDates(challenge.doneDatesJson)

                _uiState.update { current ->
                    current.copy(
                        todayDate = todayStr,
                        petName = settings.petName,
                        themeMode = settings.theme,
                        screenGoal = settings.screenGoal,
                        sleepGoal = settings.sleepGoal,
                        spirit = spirit,
                        totalXp = totalXp,
                        level = level,
                        levelTitle = levelTitle,
                        currentLevelXp = currentLevelXp,
                        minLevelXp = minXp,
                        maxLevelXp = maxXp,
                        levelProgress = levelProgress,
                        xpToNextLevel = xpToNextLevel,
                        levelUpCelebration = levelUpEvent ?: current.levelUpCelebration,
                        petMood = petMood,
                        petBubbleLine = bubble.first,
                        isPetBubbleFirm = bubble.second,
                        goodHabits = goodHabits,
                        todayGoodDone = goodDone,
                        badHabits = badHabits,
                        cleanDaysMap = cleanDaysMap,
                        todayLog = todayDay,
                        recentDays = days.take(14),
                        currentChallenge = challenge,
                        challengeDoneDays = doneDates,
                        isChallengeDoneToday = doneDates.contains(todayStr),
                        wornAccessories = worn,
                        unlockedAchievements = newUnlocked,
                        chatMessages = chatMessages
                    )
                }
            }.collect()
        }
    }

    private fun computePetBubbleLine(
        petName: String,
        todayLog: DayLogEntity,
        goodHabits: List<GoodHabit>,
        goodDone: Map<String, Boolean>,
        badHabits: List<BadHabit>,
        cleanDaysMap: Map<String, Int>
    ): Pair<String, Boolean> {
        val slips = repository.parseSlips(todayLog.slipsJson)
        if (slips.isNotEmpty()) {
            return Pair("Escorregou. Sem drama e sem desculpa: recomeça agora, não amanhã. Qual foi o gatilho?", true)
        }

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1=Dom, 2=Seg...

        // Milestones
        val milestoneHabit = badHabits.find { b ->
            val c = cleanDaysMap[b.id] ?: 0
            c in listOf(3, 7, 14, 21, 30, 60, 90)
        }
        if (milestoneHabit != null) {
            val c = cleanDaysMap[milestoneHabit.id] ?: 0
            return Pair("$c dias sem <b>${milestoneHabit.label.lowercase()}</b>! Muito orgulhoso da sua disciplina.", false)
        }

        // Night time check
        if (hour >= 21 || hour < 4) {
            return Pair("Quase 22:30. Celular longe da cama? Amanhã você acorda cedo, e eu quero te ver descansado.", false)
        }

        // Morning sleep check
        if (hour in 5..10 && todayLog.sleepHours == null && todayLog.sleepQuality == null) {
            return Pair("Bom dia! Como foi a noite? Marca as horas de sono aqui embaixo, leva 10 segundos.", false)
        }

        // Training day check (Mon, Wed, Fri)
        val isTrainDay = dayOfWeek in listOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
        if (isTrainDay && goodDone["treino"] != true) {
            return Pair(if (hour < 12) "Hoje é dia de treino. Nem que seja 20 minutos, conta bastante!" else "Ainda dá tempo do treino de hoje!", false)
        }

        val doneCount = goodHabits.count { goodDone[it.id] == true }
        if (goodHabits.isNotEmpty() && doneCount >= goodHabits.size) {
            return Pair("Tudo marcado hoje! Tô muito orgulhoso da sua consistência. Continue assim!", false)
        }

        if (doneCount > 0) {
            return Pair("Já foram $doneCount hábitos hoje. Continua firme que eu fico cada vez mais redondinho!", false)
        }

        return Pair("Oi! Sou o $petName. Se bater vontade de besteira, aperta o botão vermelho <b>antes</b>, não depois.", false)
    }

    fun toggleGoodHabit(habitId: String) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val map = repository.parseGoodDone(today.goodDoneJson).toMutableMap()
            val wasDone = map[habitId] ?: false
            map[habitId] = !wasDone

            val json = JSONObject(map as Map<*, *>).toString()
            repository.updateDayLog(today.copy(goodDoneJson = json))

            if (!wasDone) {
                showToast("+15 XP conquistados!")
                _uiState.update { it.copy(floatingXpEvent = 15) }
            }
        }
    }

    fun recordSlip(habitId: String) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val slips = repository.parseSlips(today.slipsJson).toMutableList()
            slips.add(habitId)

            val settings = repository.getSettings()
            val badHabits = repository.parseBadHabits(settings.badHabitsJson).toMutableList()
            val index = badHabits.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val h = badHabits[index]
                val clean = BolotaRepository.daysBetween(h.since, BolotaRepository.today())
                val newBest = max(h.best, clean)
                badHabits[index] = h.copy(since = BolotaRepository.today(), best = newBest)

                val arr = JSONArray()
                badHabits.forEach { b ->
                    val o = JSONObject()
                    o.put("id", b.id)
                    o.put("label", b.label)
                    o.put("since", b.since)
                    o.put("best", b.best)
                    if (b.plan != null) {
                        o.put("plan", JSONObject().put("se", b.plan.se).put("entao", b.plan.entao))
                    }
                    arr.put(o)
                }
                repository.updateSettings(settings.copy(badHabitsJson = arr.toString()))
            }

            repository.updateDayLog(today.copy(slipsJson = JSONArray(slips).toString()))

            // Open trigger prompt
            _uiState.update { it.copy(triggerPrompt = Pair(habitId, "slip")) }
        }
    }

    fun recordUrgeResisted(habitId: String) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val urges = repository.parseUrges(today.urgesJson).toMutableList()
            urges.add(UrgeLog(habitId = habitId, outcome = "resisti", timestamp = System.currentTimeMillis()))

            val arr = JSONArray()
            urges.forEach { u ->
                arr.put(JSONObject().put("habitId", u.habitId).put("outcome", u.outcome).put("timestamp", u.timestamp))
            }
            repository.updateDayLog(today.copy(urgesJson = arr.toString()))

            showToast("Vontade vencida! +30 XP")
            _uiState.update { it.copy(isSosOpen = false, triggerPrompt = Pair(habitId, "urge"), floatingXpEvent = 30) }
        }
    }

    fun recordUrgeRelapsed(habitId: String) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val urges = repository.parseUrges(today.urgesJson).toMutableList()
            urges.add(UrgeLog(habitId = habitId, outcome = "cedi", timestamp = System.currentTimeMillis()))

            val arr = JSONArray()
            urges.forEach { u ->
                arr.put(JSONObject().put("habitId", u.habitId).put("outcome", u.outcome).put("timestamp", u.timestamp))
            }
            repository.updateDayLog(today.copy(urgesJson = arr.toString()))

            _uiState.update { it.copy(isSosOpen = false) }
            recordSlip(habitId)
        }
    }

    fun recordTrigger(triggerName: String) {
        val prompt = _uiState.value.triggerPrompt ?: return
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val triggers = repository.parseTriggers(today.triggersJson).toMutableList()
            triggers.add(TriggerLog(habitId = prompt.first, trigger = triggerName, kind = prompt.second))

            val arr = JSONArray()
            triggers.forEach { t ->
                arr.put(JSONObject().put("habitId", t.habitId).put("trigger", t.trigger).put("kind", t.kind).put("timestamp", t.timestamp))
            }
            repository.updateDayLog(today.copy(triggersJson = arr.toString()))

            _uiState.update { it.copy(triggerPrompt = null) }
            showToast(if (prompt.second == "urge") "Gatilho superado registrado!" else "Gatilho mapeado. Conhecendo o padrão.")
        }
    }

    fun dismissTriggerPrompt() {
        _uiState.update { it.copy(triggerPrompt = null) }
    }

    fun setSosOpen(open: Boolean) {
        _uiState.update { it.copy(isSosOpen = open) }
    }

    fun updateSleepHours(delta: Float) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val cur = today.sleepHours ?: 7.0f
            val newV = (cur + delta).coerceIn(0f, 24f)
            repository.updateDayLog(today.copy(sleepHours = newV))
        }
    }

    fun updateScreenHours(delta: Float) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val cur = today.screenHours ?: 3.0f
            val newV = (cur + delta).coerceIn(0f, 24f)
            repository.updateDayLog(today.copy(screenHours = newV))
        }
    }

    fun updateSleepQuality(quality: Int) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val newQ = if (today.sleepQuality == quality) null else quality
            repository.updateDayLog(today.copy(sleepQuality = newQ))
        }
    }

    fun updateMood(mood: Int) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            val newM = if (today.mood == mood) null else mood
            repository.updateDayLog(today.copy(mood = newM))
        }
    }

    fun updateExtraHealth(sleepScore: Int?, steps: Int?, restHR: Int?, weight: Float?) {
        viewModelScope.launch {
            val today = repository.getTodayLog()
            repository.updateDayLog(
                today.copy(
                    sleepScore = sleepScore,
                    steps = steps,
                    restHR = restHR,
                    weight = weight
                )
            )
            showToast("Dados de saúde atualizados")
        }
    }

    fun completeChallengeDay() {
        viewModelScope.launch {
            val chal = repository.getChallengeForCurrentWeek()
            val today = BolotaRepository.today()
            val done = repository.parseDoneDates(chal.doneDatesJson).toMutableList()
            val had = done.contains(today)
            if (had) {
                done.remove(today)
            } else {
                done.add(today)
            }

            val updatedChal = chal.copy(doneDatesJson = JSONArray(done).toString())
            repository.updateChallenge(updatedChal)

            if (!had) {
                if (done.size >= chal.targetDays) {
                    showToast("Desafio semanal batido! +50 XP de bônus!")
                    _uiState.update { it.copy(floatingXpEvent = 50) }
                } else {
                    showToast("Desafio diário cumprido! +20 XP")
                    _uiState.update { it.copy(floatingXpEvent = 20) }
                }
            }
        }
    }

    fun dismissLevelUpCelebration() {
        _uiState.update { it.copy(levelUpCelebration = null) }
    }

    fun dismissFloatingXp() {
        _uiState.update { it.copy(floatingXpEvent = null) }
    }

    fun generateOrSwapChallenge() {
        viewModelScope.launch {
            val chal = repository.getChallengeForCurrentWeek()
            val options = listOf(
                "Celular carregando fora do quarto na hora de dormir" to 4,
                "Tela abaixo da meta diária por pelo menos 4 dias" to 4,
                "Nenhum doce após o jantar durante a semana" to 5,
                "Primeira hora do dia sem redes sociais" to 4,
                "Bloco de 25 minutos de foco antes do almoço" to 4,
                "10 minutos de caminhada ao ar livre todo dia" to 5
            )
            val next = options.filter { it.first != chal.text }.random()
            repository.updateChallenge(
                chal.copy(
                    text = next.first,
                    targetDays = next.second,
                    doneDatesJson = "[]"
                )
            )
            showToast("Novo desafio semanal definido!")
        }
    }

    // Conversational AI mode
    fun sendChatMessage(userText: String) {
        if (userText.isBlank()) return

        viewModelScope.launch {
            repository.insertChatMessage("user", userText.trim())
            _uiState.update { it.copy(isChatThinking = true) }

            val settings = repository.getSettings()
            val systemPrompt = buildBolotaSystemPrompt(settings)
            val history = _uiState.value.chatMessages.takeLast(8).map {
                (if (it.role == "user") "user" else "model") to it.content
            }

            val aiResult = GeminiClient.generateBolotaResponse(
                systemPrompt = systemPrompt,
                conversationHistory = history,
                userMessage = userText.trim()
            )

            val reply = aiResult.getOrElse {
                // Fallback heuristic response if API offline
                generateHeuristicReply(userText, settings)
            }

            repository.insertChatMessage("assistant", reply)
            _uiState.update { it.copy(isChatThinking = false) }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            showToast("Conversa limpa")
        }
    }

    fun equipOrUnequipAccessory(achId: String, slot: AccessorySlot) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val wear = repository.parseWearAccessories(settings.wearAccessoriesJson).toMutableMap()
            val slotKey = slot.name
            val wasEquipped = wear[slotKey] == achId

            if (slot == AccessorySlot.SKIN) {
                wear[slotKey] = achId
                showToast("Cor do Bolota alterada!")
            } else {
                if (wasEquipped) {
                    wear.remove(slotKey)
                    showToast("Acessório removido.")
                } else {
                    wear[slotKey] = achId
                    showToast("Acessório equipado no Bolota!")
                }
            }

            val json = JSONObject(wear as Map<*, *>).toString()
            repository.updateSettings(settings.copy(wearAccessoriesJson = json))
        }
    }

    fun updateSettings(petName: String, theme: String, screenGoal: Float, sleepGoal: Float) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            repository.updateSettings(
                settings.copy(
                    petName = petName.ifBlank { "Bolota" },
                    theme = theme,
                    screenGoal = screenGoal.coerceAtLeast(0.5f),
                    sleepGoal = sleepGoal.coerceAtLeast(4.0f)
                )
            )
            showToast("Ajustes salvos com sucesso")
        }
    }

    fun addGoodHabit(label: String, days: List<Int>) {
        if (label.isBlank()) return
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseGoodHabits(settings.goodHabitsJson).toMutableList()
            val id = "g_" + System.currentTimeMillis().toString(36)
            list.add(GoodHabit(id = id, label = label.trim(), days = days))

            val arr = JSONArray()
            list.forEach { h ->
                arr.put(JSONObject().put("id", h.id).put("label", h.label).put("days", JSONArray(h.days)))
            }
            repository.updateSettings(settings.copy(goodHabitsJson = arr.toString()))
            showToast("Hábito '$label' adicionado!")
        }
    }

    fun removeGoodHabit(id: String) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseGoodHabits(settings.goodHabitsJson).filter { it.id != id }
            val arr = JSONArray()
            list.forEach { h ->
                arr.put(JSONObject().put("id", h.id).put("label", h.label).put("days", JSONArray(h.days)))
            }
            repository.updateSettings(settings.copy(goodHabitsJson = arr.toString()))
            showToast("Hábito removido")
        }
    }

    fun addBadHabit(label: String, since: String) {
        if (label.isBlank()) return
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseBadHabits(settings.badHabitsJson).toMutableList()
            val id = "b_" + System.currentTimeMillis().toString(36)
            list.add(BadHabit(id = id, label = label.trim(), since = since.ifBlank { BolotaRepository.today() }, best = 0))

            val arr = JSONArray()
            list.forEach { b ->
                val o = JSONObject().put("id", b.id).put("label", b.label).put("since", b.since).put("best", b.best)
                if (b.plan != null) o.put("plan", JSONObject().put("se", b.plan.se).put("entao", b.plan.entao))
                arr.put(o)
            }
            repository.updateSettings(settings.copy(badHabitsJson = arr.toString()))
            showToast("Hábito '$label' adicionado")
        }
    }

    fun removeBadHabit(id: String) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseBadHabits(settings.badHabitsJson).filter { it.id != id }
            val arr = JSONArray()
            list.forEach { b ->
                val o = JSONObject().put("id", b.id).put("label", b.label).put("since", b.since).put("best", b.best)
                if (b.plan != null) o.put("plan", JSONObject().put("se", b.plan.se).put("entao", b.plan.entao))
                arr.put(o)
            }
            repository.updateSettings(settings.copy(badHabitsJson = arr.toString()))
            showToast("Hábito removido")
        }
    }

    fun updateBadHabitPlan(habitId: String, se: String, entao: String) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseBadHabits(settings.badHabitsJson).toMutableList()
            val index = list.indexOfFirst { it.id == habitId }
            if (index != -1) {
                list[index] = list[index].copy(plan = IfThenPlan(se = se.trim(), entao = entao.trim()))
                val arr = JSONArray()
                list.forEach { b ->
                    val o = JSONObject().put("id", b.id).put("label", b.label).put("since", b.since).put("best", b.best)
                    if (b.plan != null) o.put("plan", JSONObject().put("se", b.plan.se).put("entao", b.plan.entao))
                    arr.put(o)
                }
                repository.updateSettings(settings.copy(badHabitsJson = arr.toString()))
                showToast("Plano 'se... então...' salvo!")
            }
        }
    }

    fun updateBadHabitSince(habitId: String, since: String) {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val list = repository.parseBadHabits(settings.badHabitsJson).toMutableList()
            val index = list.indexOfFirst { it.id == habitId }
            if (index != -1) {
                val clean = BolotaRepository.daysBetween(since, BolotaRepository.today())
                list[index] = list[index].copy(since = since, best = max(list[index].best, clean))
                val arr = JSONArray()
                list.forEach { b ->
                    val o = JSONObject().put("id", b.id).put("label", b.label).put("since", b.since).put("best", b.best)
                    if (b.plan != null) o.put("plan", JSONObject().put("se", b.plan.se).put("entao", b.plan.entao))
                    arr.put(o)
                }
                repository.updateSettings(settings.copy(badHabitsJson = arr.toString()))
                showToast("Data ajustada para $since")
            }
        }
    }

    suspend fun generateWeeklyReview(): String {
        val days = _uiState.value.recentDays.take(7)
        val state = _uiState.value
        val dataSummary = buildString {
            days.forEach { d ->
                append("${d.date}: sono=${d.sleepHours ?: "?"}h, tela=${d.screenHours ?: "?"}h, humor=${d.mood ?: "?"}/5; ")
            }
        }
        val sysPrompt = buildBolotaSystemPrompt(repository.getSettings())
        val res = GeminiClient.generateWeeklySummary(sysPrompt, dataSummary)
        return res.getOrElse {
            "Você teve uma semana de bom foco nos estudos e hidratação. O ponto de atenção continuou sendo o tempo de tela após as 21h. Meta para a próxima semana: deixar o celular carregando fora do quarto a partir de 22:15."
        }
    }

    suspend fun askSosAiAdvice(habit: BadHabit): String {
        val sysPrompt = buildBolotaSystemPrompt(repository.getSettings())
        val res = GeminiClient.generateBolotaResponse(
            systemPrompt = sysPrompt,
            conversationHistory = emptyList(),
            userMessage = "Estou com muita vontade agora de recair em '${habit.label}'. Me dê uma fala firme e prática de no máximo 2 frases para os próximos 2 minutos."
        )
        return res.getOrElse {
            "Levanta agora e sai desse ambiente. Bebe um copo de água gelada e me espera aqui por 10 minutos. Essa vontade é um pico químico passageiro!"
        }
    }

    private fun buildBolotaSystemPrompt(settings: AppSettingsEntity): String {
        val s = _uiState.value
        val goodList = s.goodHabits.joinToString { it.label }
        val badList = s.badHabits.joinToString { "${it.label} (${s.cleanDaysMap[it.id] ?: 0} dias sem)" }
        return """
            Você é ${settings.petName}, um bichinho virtual redondinho, companheiro e inteligente, que vive no celular do seu dono para ajudá-lo a manter a rotina e largar hábitos ruins.
            Você fala sempre em Português do Brasil com carisma, afeto e energia positiva no dia a dia.
            PERSONALIDADE:
            - Muito acolhedor, fofo e animado para hábitos saudáveis (sono, hidratação, treino, estudos do mestrado).
            - FIRME, DIRETO E INCISIVO quando o assunto for impulso ou hábito ruim: você NÃO passa pano, NÃO aceita desculpas fáceis, mas nunca humilha. Sempre indica uma ação prática de 2 minutos.
            PERFIL DO DONO:
            - Fisioterapeuta fazendo mestrado na área de saúde.
            - Acorda por volta das 05:10 e busca dormir até 22:30.
            - Trabalha às terças e quintas, tem aulas/pesquisa de mestrado.
            - Treina segunda, quarta, sexta.
            DADOS ATUAIS DO APP:
            - Hábitos bons: $goodList
            - Hábitos para largar: $badList
            - Ânimo atual do bichinho: ${s.spirit}/100 | Nível: ${s.level} (${s.totalXp} XP)
            - Sono hoje: ${s.todayLog.sleepHours ?: "não registrado"}h | Tela hoje: ${s.todayLog.screenHours ?: "não registrado"}h (meta: ${s.screenGoal}h)
            REGRAS DE RESPOSTA:
            - Responda de forma concisa (1 a 4 frases).
            - Seja pessoal, chamando-se de ${settings.petName} e tratando o usuário com proximidade.
            - Nunca faça diagnóstico médico formal.
        """.trimIndent()
    }

    private fun generateHeuristicReply(msg: String, settings: AppSettingsEntity): String {
        val lower = msg.lowercase()
        val name = settings.petName
        return when {
            lower.contains("vontade") || lower.contains("doce") || lower.contains("reca") ->
                "Respira fundo! Lembra do nosso combinado: não troca dias de vitória por 10 minutos de impulso. Bebe um copo de água agora!"
            lower.contains("sono") || lower.contains("dorm") || lower.contains("cansa") ->
                "Seu sono é a base de tudo. Tenta deixar o celular longe da cama até as 22:30 pra gente acordar com energia às 5h!"
            lower.contains("trein") || lower.contains("academia") ->
                "Bora pro treino! Nem que seja um bloco de 20 minutos focado, seu corpo de fisioterapeuta agradece!"
            lower.contains("estud") || lower.contains("mestrado") || lower.contains("foco") ->
                "Um bloco de 25 minutos de foco puro sem celular faz milagres. Abre o artigo ou tese agora, eu fico de guarda aqui!"
            lower.contains("oi") || lower.contains("olá") || lower.contains("bolota") ->
                "Oi! Tô aqui do seu lado. Como posso te ajudar a fechar o dia redondo e sem deslizes?"
            else ->
                "Tô aqui com você! Foco no que a gente combinou e um passo de cada vez. O que você vai fazer nos próximos 15 minutos?"
        }
    }

    private fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
