package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BarChart
import com.example.ui.components.ChartPoint
import com.example.ui.components.LineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.BolotaUiState
import com.example.ui.viewmodel.BolotaViewModel
import kotlinx.coroutines.launch

@Composable
fun SaudeScreen(
    uiState: BolotaUiState,
    viewModel: BolotaViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isGeneratingReview by remember { mutableStateOf(false) }
    var weeklyReviewText by remember {
        mutableStateOf<String?>(
            "O Bolota lê seus últimos 7 dias e te diz o que foi bem, o padrão que merece atenção e uma meta concreta para a próxima semana."
        )
    }

    val pastDays = uiState.recentDays.reversed() // oldest to newest for charts

    // Compute 7-day stats
    val last7 = pastDays.takeLast(7)
    val sleepVals = last7.mapNotNull { it.sleepHours }
    val avgSleep = if (sleepVals.isNotEmpty()) sleepVals.average().toFloat() else null
    val screenVals = last7.mapNotNull { it.screenHours }
    val avgScreen = if (screenVals.isNotEmpty()) screenVals.average().toFloat() else null
    val moodVals = last7.mapNotNull { it.mood }
    val avgMood = if (moodVals.isNotEmpty()) moodVals.average().toInt() else null
    val moodLabels = listOf("Mal", "Baixo", "Ok", "Bem", "Ótimo")
    val avgMoodLabel = if (avgMood != null && avgMood in 1..5) moodLabels[avgMood - 1] else "—"
    val cleanDaysAll = uiState.cleanDaysMap.values.minOrNull() ?: 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Saúde e Métricas",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "últimos 14 dias de rotina",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4 KPI Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avg Sleep
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = avgSleep?.let { String.format("%.1fh", it) } ?: "—",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "sono médio (7d)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sleep Quality / Mood
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = avgMoodLabel,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaPetOrange
                        )
                        Text(
                            text = "humor médio",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avg Screen
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = avgScreen?.let { String.format("%.1fh", it) } ?: "—",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (avgScreen != null && avgScreen > uiState.screenGoal) BolotaDangerDark else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "tela · meta ${uiState.screenGoal}h",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Days Without Slip
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = cleanDaysAll.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaAccentDark
                        )
                        Text(
                            text = "dias sem deslizes",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // AI WEEKLY REVIEW CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BolotaAccentDark.copy(alpha = 0.6f))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BolotaAccentDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Resumo da semana com IA",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = {
                                if (!isGeneratingReview) {
                                    isGeneratingReview = true
                                    weeklyReviewText = "O Bolota está lendo seus últimos 7 dias..."
                                    coroutineScope.launch {
                                        val review = viewModel.generateWeeklyReview()
                                        weeklyReviewText = review
                                        isGeneratingReview = false
                                    }
                                }
                            },
                            enabled = !isGeneratingReview,
                            colors = ButtonDefaults.buttonColors(containerColor = BolotaAccentDark),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("generate_weekly_review_button")
                        ) {
                            Text(
                                text = if (isGeneratingReview) "Gerando..." else "Gerar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = weeklyReviewText ?: "",
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // TOP TRIGGERS IN 30 DAYS
        item {
            val allTriggers = remember(uiState.recentDays) {
                val map = mutableMapOf<String, Int>()
                uiState.recentDays.forEach { d ->
                    try {
                        val arr = org.json.JSONArray(d.triggersJson)
                        for (i in 0 until arr.length()) {
                            val t = arr.getJSONObject(i).getString("trigger")
                            map[t] = (map[t] ?: 0) + 1
                        }
                    } catch (e: Exception) {}
                }
                map.entries.sortedByDescending { it.value }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Gatilhos mais comuns",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "o que vinha antes de cada vontade ou deslize",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (allTriggers.isEmpty()) {
                        Text(
                            text = "Quando você marcar o gatilho após uma vontade ou deslize, o ranking dos maiores inimigos da sua rotina aparecerá aqui.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    } else {
                        val maxCount = allTriggers.maxOf { it.value }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allTriggers.take(5).forEach { (trigger, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = trigger,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.width(110.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(fraction = (count.toFloat() / maxCount).coerceIn(0.05f, 1f))
                                                .clip(CircleShape)
                                                .background(BolotaDangerDark)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "$count",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CHARTS: SLEEP
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sono",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "meta: ${uiState.sleepGoal}h",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val sleepPoints = pastDays.map { d ->
                        val dayNum = d.date.takeLast(2)
                        ChartPoint(dayNum, d.sleepHours)
                    }

                    BarChart(
                        data = sleepPoints,
                        maxVal = 10f,
                        goalVal = uiState.sleepGoal,
                        unit = "h"
                    )
                }
            }
        }

        // CHARTS: SCREEN TIME
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tempo de tela",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "vermelho = acima da meta (${uiState.screenGoal}h)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val screenPoints = pastDays.map { d ->
                        val dayNum = d.date.takeLast(2)
                        ChartPoint(dayNum, d.screenHours)
                    }

                    BarChart(
                        data = screenPoints,
                        maxVal = 8f,
                        goalVal = uiState.screenGoal,
                        higherIsBad = true,
                        unit = "h"
                    )
                }
            }
        }

        // CHARTS: MOOD CURVE
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Curva de humor",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1=Mal, 3=Ok, 5=Ótimo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val moodPoints = pastDays.map { d ->
                        val dayNum = d.date.takeLast(2)
                        ChartPoint(dayNum, d.mood?.toFloat())
                    }

                    LineChart(
                        data = moodPoints,
                        maxVal = 5f
                    )
                }
            }
        }
    }
}
