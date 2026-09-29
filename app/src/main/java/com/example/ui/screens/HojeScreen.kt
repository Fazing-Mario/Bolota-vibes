package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadHabit
import com.example.data.model.GoodHabit
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BolotaUiState
import com.example.ui.viewmodel.BolotaViewModel
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HojeScreen(
    uiState: BolotaUiState,
    viewModel: BolotaViewModel,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddGoodDialog by remember { mutableStateOf(false) }
    var showAddBadDialog by remember { mutableStateOf(false) }
    var armedSlipId by remember { mutableStateOf<String?>(null) }
    var showMoreHealthData by remember { mutableStateOf(false) }

    // Clear armed slip after 4 seconds
    LaunchedEffect(armedSlipId) {
        if (armedSlipId != null) {
            kotlinx.coroutines.delay(4000L)
            armedSlipId = null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
    ) {
        // PET CARD & SPEECH BUBBLE
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BolotaPetView(
                    mood = uiState.petMood,
                    level = uiState.level,
                    wearAccessories = uiState.wornAccessories,
                    size = 124.dp,
                    modifier = Modifier.clickable { onNavigateToChat() }
                )

                Spacer(modifier = Modifier.width(10.dp))

                SpeechBubble(
                    text = uiState.petBubbleLine,
                    isFirm = uiState.isPetBubbleFirm,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToChat() }
                )
            }
        }

        // METERS (SPIRIT & LEVEL XP)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Spirit
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Ânimo",
                                    fontSize = 12.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${uiState.spirit}%",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.spirit / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = BolotaPetOrange,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }

                        // XP / Level
                        Column(modifier = Modifier.weight(1.2f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Nível ${uiState.level}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${uiState.totalXp} XP",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BolotaAccentDark
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.levelProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = BolotaAccentDark,
                                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Level perks & Customization banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                            .clickable { onNavigateToChat() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⭐ ${uiState.levelTitle}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Faltam ${uiState.xpToNextLevel} XP para o próximo nível",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onNavigateToChat,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Guarda-roupa 🎨", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // TRIGGER TAGGING PROMPT (If slip occurred or urge won)
        if (uiState.triggerPrompt != null) {
            item {
                val isUrgeWon = uiState.triggerPrompt.second == "urge"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (isUrgeWon) BolotaAccentDark else BolotaDangerDark
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isUrgeWon) "Boa! O que puxou a vontade?" else "O que puxou o deslize?",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(onClick = { viewModel.dismissTriggerPrompt() }) {
                                Text("pular", fontSize = 12.sp)
                            }
                        }

                        Text(
                            text = "Mapear o gatilho ajuda o Bolota a achar o padrão de comportamento.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val triggers = listOf("Tédio", "Cansaço", "Estresse", "Ansiedade", "Sozinho", "Celular na cama", "Fome", "Rede social")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            triggers.forEach { trig ->
                                SuggestionChip(
                                    onClick = { viewModel.recordTrigger(trig) },
                                    label = { Text(trig, fontSize = 12.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // SOS EMERGENCY BUTTON
        item {
            Button(
                onClick = { viewModel.setSosOpen(true) },
                modifier = Modifier
                    .testTag("sos_button")
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BolotaDangerDark),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Bateu vontade?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = "aperta antes, não depois",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }

        // WEEKLY CHALLENGE CARD
        uiState.currentChallenge?.let { chal ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (uiState.challengeDoneDays.size >= chal.targetDays) BolotaAccentDark else MaterialTheme.colorScheme.outline
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DESAFIO DA SEMANA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${uiState.challengeDoneDays.size} de ${chal.targetDays} dias",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = chal.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Day completion dots
                            val daysOfWeek = listOf("S", "T", "Q", "Q", "S", "S", "D")
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                daysOfWeek.forEachIndexed { idx, label ->
                                    val isDone = idx < uiState.challengeDoneDays.size
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) BolotaAccentDark else Color.Transparent)
                                            .border(
                                                1.dp,
                                                if (isDone) BolotaAccentDark else MaterialTheme.colorScheme.outline,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.completeChallengeDay() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (uiState.isChallengeDoneToday) BolotaAccentSoftDark else BolotaAccentDark,
                                        contentColor = if (uiState.isChallengeDoneToday) BolotaAccentDark else Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (uiState.isChallengeDoneToday) "Cumpri hoje ✓" else "Cumpri hoje",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.generateOrSwapChallenge() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Trocar desafio",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // GOOD HABITS SECTION
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoje",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { showAddGoodDialog = true }) {
                    Text("+ Novo hábito", fontSize = 13.sp)
                }
            }
        }

        // Good habits grid (pairs of 2)
        val goodList = uiState.goodHabits
        val cal = Calendar.getInstance()
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Dom, 1=Seg...
        items(goodList.chunked(2).size) { chunkIdx ->
            val chunk = goodList.chunked(2)[chunkIdx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chunk.forEach { habit ->
                    val isDone = uiState.todayGoodDone[habit.id] ?: false
                    val isScheduledToday = habit.days.isEmpty() || habit.days.contains(currentDayOfWeek)
                    GoodHabitChip(
                        habit = habit,
                        isDone = isDone,
                        isTodayScheduled = isScheduledToday,
                        onToggle = { viewModel.toggleGoodHabit(habit.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (chunk.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // BAD HABITS SECTION ("DIAS SEM")
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Dias sem",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "só marque quando escorregar",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(onClick = { showAddBadDialog = true }) {
                    Text("+ Pra largar", fontSize = 13.sp)
                }
            }
        }

        items(uiState.badHabits.size) { idx ->
            val habit = uiState.badHabits[idx]
            val cleanDays = uiState.cleanDaysMap[habit.id] ?: 0
            val isArmed = armedSlipId == habit.id
            BadHabitCard(
                habit = habit,
                cleanDays = cleanDays,
                isArmed = isArmed,
                onSlipClick = {
                    if (isArmed) {
                        armedSlipId = null
                        viewModel.recordSlip(habit.id)
                    } else {
                        armedSlipId = habit.id
                    }
                }
            )
        }

        // DAILY HEALTH CHECK-IN SECTION
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saúde de hoje",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "~30 segundos",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sleep and Screen Steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Sleep Hours Stepper
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Horas de sono",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.updateSleepHours(-0.5f) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = uiState.todayLog.sleepHours?.let { "${it}h" } ?: "—",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.width(52.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.updateSleepHours(0.5f) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Screen Time Stepper
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tempo de tela",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.updateScreenHours(-0.5f) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Text(
                                        text = uiState.todayLog.screenHours?.let { "${it}h" } ?: "—",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        modifier = Modifier.width(52.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    OutlinedButton(
                                        onClick = { viewModel.updateScreenHours(0.5f) },
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.size(36.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Sleep Quality Scale (1-5)
                        Column {
                            Text(
                                text = "Como foi a noite?",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val sleepQualities = listOf("Péssima", "Ruim", "Ok", "Boa", "Ótima")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                sleepQualities.forEachIndexed { i, label ->
                                    val isSelected = uiState.todayLog.sleepQuality == (i + 1)
                                    Button(
                                        onClick = { viewModel.updateSleepQuality(i + 1) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Mood Scale (1-5)
                        Column {
                            Text(
                                text = "Como você está?",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val moods = listOf("Mal", "Baixo", "Ok", "Bem", "Ótimo")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                moods.forEachIndexed { i, label ->
                                    val isSelected = uiState.todayLog.mood == (i + 1)
                                    Button(
                                        onClick = { viewModel.updateMood(i + 1) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        // Expandable More Data (Steps, Sleep Cycle, HR, Weight)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMoreHealthData = !showMoreHealthData }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (showMoreHealthData) "Ocultar dados extras" else "Mais dados (Sleep Cycle, relógio, peso)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (showMoreHealthData) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        AnimatedVisibility(visible = showMoreHealthData) {
                            var sleepScoreInput by remember { mutableStateOf(uiState.todayLog.sleepScore?.toString() ?: "") }
                            var stepsInput by remember { mutableStateOf(uiState.todayLog.steps?.toString() ?: "") }
                            var hrInput by remember { mutableStateOf(uiState.todayLog.restHR?.toString() ?: "") }
                            var weightInput by remember { mutableStateOf(uiState.todayLog.weight?.toString() ?: "") }

                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = sleepScoreInput,
                                        onValueChange = {
                                            sleepScoreInput = it
                                            viewModel.updateExtraHealth(
                                                sleepScore = it.toIntOrNull(),
                                                steps = stepsInput.toIntOrNull(),
                                                restHR = hrInput.toIntOrNull(),
                                                weight = weightInput.toFloatOrNull()
                                            )
                                        },
                                        label = { Text("Sleep Cycle %") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = stepsInput,
                                        onValueChange = {
                                            stepsInput = it
                                            viewModel.updateExtraHealth(
                                                sleepScore = sleepScoreInput.toIntOrNull(),
                                                steps = it.toIntOrNull(),
                                                restHR = hrInput.toIntOrNull(),
                                                weight = weightInput.toFloatOrNull()
                                            )
                                        },
                                        label = { Text("Passos") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = hrInput,
                                        onValueChange = {
                                            hrInput = it
                                            viewModel.updateExtraHealth(
                                                sleepScore = sleepScoreInput.toIntOrNull(),
                                                steps = stepsInput.toIntOrNull(),
                                                restHR = it.toIntOrNull(),
                                                weight = weightInput.toFloatOrNull()
                                            )
                                        },
                                        label = { Text("FC repouso (bpm)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = {
                                            weightInput = it
                                            viewModel.updateExtraHealth(
                                                sleepScore = sleepScoreInput.toIntOrNull(),
                                                steps = stepsInput.toIntOrNull(),
                                                restHR = hrInput.toIntOrNull(),
                                                weight = it.toFloatOrNull()
                                            )
                                        },
                                        label = { Text("Peso (kg)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // PAST 7 DAYS HEAT STRIP
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Últimos 7 dias",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "hábitos bons feitos",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Build 7 days
                val calNow = Calendar.getInstance()
                val weekDots = (6 downTo 0).map { daysAgo ->
                    val c = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                    val dayStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(c.time)
                    val dLog = uiState.recentDays.find { it.date == dayStr }
                    val count = dLog?.let {
                        org.json.JSONObject(it.goodDoneJson).let { o ->
                            var cnt = 0
                            val itKeys = o.keys()
                            while (itKeys.hasNext()) if (o.optBoolean(itKeys.next())) cnt++
                            cnt
                        }
                    } ?: 0
                    val hasSlip = dLog?.let { org.json.JSONArray(it.slipsJson).length() > 0 } ?: false
                    val dayLetters = listOf("D", "S", "T", "Q", "Q", "S", "S")
                    val dayLetter = dayLetters[c.get(Calendar.DAY_OF_WEEK) - 1]
                    DayDotData(
                        dayLetter = dayLetter,
                        count = count,
                        hasSlip = hasSlip,
                        isToday = daysAgo == 0
                    )
                }

                WeekStripView(days = weekDots)
            }
        }
    }

    // ADD GOOD HABIT DIALOG
    if (showAddGoodDialog) {
        var habitName by remember { mutableStateOf("") }
        val daysSelected = remember { mutableStateListOf<Int>() }
        AlertDialog(
            onDismissRequest = { showAddGoodDialog = false },
            title = { Text("Novo hábito bom") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Nome do hábito") },
                        placeholder = { Text("Ex.: Alongamento 10 min") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Dias programados (nenhum = todo dia):", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val days = listOf("D", "S", "T", "Q", "Q", "S", "S")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEachIndexed { i, d ->
                            val isSel = daysSelected.contains(i)
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    if (isSel) daysSelected.remove(i) else daysSelected.add(i)
                                },
                                label = { Text(d, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            viewModel.addGoodHabit(habitName, daysSelected.toList())
                            showAddGoodDialog = false
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoodDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ADD BAD HABIT DIALOG
    if (showAddBadDialog) {
        var habitName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddBadDialog = false },
            title = { Text("Hábito pra largar") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        label = { Text("Nome do hábito") },
                        placeholder = { Text("Ex.: Celular na cama") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "O contador começará a contar os dias limpos a partir de hoje. No SOS, você poderá definir um plano se... então...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (habitName.isNotBlank()) {
                            viewModel.addBadHabit(habitName, uiState.todayDate)
                            showAddBadDialog = false
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBadDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // SOS BOTTOM SHEET MODAL
    if (uiState.isSosOpen) {
        SosModalBottomSheet(
            badHabits = uiState.badHabits,
            petName = uiState.petName,
            onResisted = { habit -> viewModel.recordUrgeResisted(habit.id) },
            onRelapsed = { habit -> viewModel.recordUrgeRelapsed(habit.id) },
            onDismiss = { viewModel.setSosOpen(false) },
            onAskAiAdvice = { habit -> viewModel.askSosAiAdvice(habit) }
        )
    }

    // LEVEL UP CELEBRATION DIALOG
    uiState.levelUpCelebration?.let { levelTitle ->
        val unlockedAtThisLevel = com.example.data.model.LevelSystem.ALL_ITEMS.filter { it.requiredLevel == levelTitle.level }
        AlertDialog(
            onDismissRequest = { viewModel.dismissLevelUpCelebration() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎉 SUBIU DE NÍVEL!", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BolotaAccentDark)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BolotaPetView(
                        mood = "radiante",
                        level = levelTitle.level,
                        wearAccessories = uiState.wornAccessories,
                        size = 110.dp
                    )

                    Text(
                        text = "Nível ${levelTitle.level} · ${levelTitle.title}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Sua dedicação e hábitos concluídos fortaleceram o ${uiState.petName}!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (unlockedAtThisLevel.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎁 Novos Itens Desbloqueados:",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaPetOrange
                        )
                        unlockedAtThisLevel.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(item.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.name,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLevelUpCelebration()
                        onNavigateToChat()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BolotaAccentDark)
                ) {
                    Text("Ver Guarda-roupa 🎨", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLevelUpCelebration() }) {
                    Text("Continuar")
                }
            }
        )
    }

    // FLOATING XP TOAST / BANNER
    uiState.floatingXpEvent?.let { xpGain ->
        LaunchedEffect(xpGain) {
            kotlinx.coroutines.delay(1800L)
            viewModel.dismissFloatingXp()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BolotaAccentDark),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+$xpGain XP!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
