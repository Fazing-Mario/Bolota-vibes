package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayLogEntity
import com.example.data.model.GoodHabit
import com.example.ui.components.BolotaPetView
import com.example.ui.theme.BolotaAccentDark
import com.example.ui.theme.BolotaPetOrange
import com.example.ui.theme.BolotaWarnDark
import com.example.ui.viewmodel.BolotaUiState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@Composable
fun EstatisticasScreen(
    uiState: BolotaUiState,
    modifier: Modifier = Modifier
) {
    var selectedDaysRange by remember { mutableIntStateOf(14) } // 7, 14, 30
    var selectedHabitFilterId by remember { mutableStateOf<String?>(null) } // null = Todos

    // Chart model producers for Vico
    val trendModelProducer = remember { CartesianChartModelProducer.build() }
    val weekdayModelProducer = remember { CartesianChartModelProducer.build() }
    val habitCountModelProducer = remember { CartesianChartModelProducer.build() }

    // Parse and prepare statistics data
    val daysData = remember(uiState.recentDays, selectedDaysRange) {
        uiState.recentDays.take(selectedDaysRange).reversed()
    }

    // Daily completion percentages
    val dailyPercentages = remember(daysData, selectedHabitFilterId, uiState.goodHabits) {
        daysData.map { dayLog ->
            val goodDone = try {
                val o = JSONObject(dayLog.goodDoneJson)
                val map = mutableMapOf<String, Boolean>()
                val keys = o.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = o.optBoolean(k, false)
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }

            if (selectedHabitFilterId != null) {
                if (goodDone[selectedHabitFilterId] == true) 100f else 0f
            } else {
                if (uiState.goodHabits.isEmpty()) 0f
                else {
                    val doneCount = uiState.goodHabits.count { goodDone[it.id] == true }
                    (doneCount.toFloat() / uiState.goodHabits.size) * 100f
                }
            }
        }
    }

    // Weekday completion rates (Dom, Seg, Ter, Qua, Qui, Sex, Sáb)
    val weekdayRates = remember(daysData, uiState.goodHabits) {
        val totalPerDay = FloatArray(7) { 0f }
        val countPerDay = IntArray(7) { 0 }
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        daysData.forEach { dayLog ->
            val date = try { sdf.parse(dayLog.date) } catch (e: Exception) { null }
            if (date != null) {
                cal.time = date
                val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon...
                val goodDone = try {
                    val o = JSONObject(dayLog.goodDoneJson)
                    val map = mutableMapOf<String, Boolean>()
                    val keys = o.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        map[k] = o.optBoolean(k, false)
                    }
                    map
                } catch (e: Exception) {
                    emptyMap()
                }

                if (uiState.goodHabits.isNotEmpty()) {
                    val rate = (uiState.goodHabits.count { goodDone[it.id] == true }.toFloat() / uiState.goodHabits.size) * 100f
                    totalPerDay[dayOfWeek] += rate
                    countPerDay[dayOfWeek]++
                }
            }
        }

        List(7) { i ->
            if (countPerDay[i] > 0) totalPerDay[i] / countPerDay[i] else 0f
        }
    }

    // Total counts per habit
    val countsPerHabit = remember(daysData, uiState.goodHabits) {
        uiState.goodHabits.map { habit ->
            val count = daysData.count { dayLog ->
                try {
                    val o = JSONObject(dayLog.goodDoneJson)
                    o.optBoolean(habit.id, false)
                } catch (e: Exception) {
                    false
                }
            }
            Pair(habit.label, count)
        }
    }

    // Average completion percentage
    val avgCompletion = remember(dailyPercentages) {
        if (dailyPercentages.isNotEmpty()) dailyPercentages.average().roundToInt() else 0
    }

    val totalHabitsDone = remember(daysData) {
        daysData.sumOf { d ->
            try {
                val o = JSONObject(d.goodDoneJson)
                var cnt = 0
                val keys = o.keys()
                while (keys.hasNext()) if (o.optBoolean(keys.next())) cnt++
                cnt
            } catch (e: Exception) { 0 }
        }
    }

    // Best weekday calculation
    val weekdayNames = listOf("Domingo", "Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")
    val bestWeekdayIdx = remember(weekdayRates) {
        weekdayRates.indices.maxByOrNull { weekdayRates[it] } ?: 1
    }

    // Run transactions to update Vico models
    LaunchedEffect(dailyPercentages) {
        trendModelProducer.tryRunTransaction {
            lineSeries {
                series(if (dailyPercentages.isNotEmpty()) dailyPercentages else listOf(0f))
            }
        }
    }

    LaunchedEffect(weekdayRates) {
        weekdayModelProducer.tryRunTransaction {
            columnSeries {
                series(weekdayRates)
            }
        }
    }

    LaunchedEffect(countsPerHabit) {
        habitCountModelProducer.tryRunTransaction {
            columnSeries {
                series(countsPerHabit.map { it.second.toFloat() })
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
    ) {
        // SCREEN TITLE & SUBTITLE
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = BolotaAccentDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tendências de Hábitos",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Acompanhe a sua evolução e consistência no tempo",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // TIME RANGE FILTER CHIPS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(7 to "7 dias", 14 to "14 dias", 30 to "30 dias").forEach { (days, label) ->
                    FilterChip(
                        selected = selectedDaysRange == days,
                        onClick = { selectedDaysRange = days },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }

        // SUMMARY METRICS CARDS (KPIS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                            text = "$avgCompletion%",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaAccentDark
                        )
                        Text(
                            text = "conclusão média",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

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
                            text = "$totalHabitsDone",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaPetOrange
                        )
                        Text(
                            text = "hábitos feitos no período",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Dia mais consistente",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${weekdayNames[bestWeekdayIdx]} (${weekdayRates[bestWeekdayIdx].roundToInt()}%)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BolotaAccentDark.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔥 Pico semanal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaAccentDark
                        )
                    }
                }
            }
        }

        // HABIT FILTER CHIPS FOR LINE CHART
        item {
            Column {
                Text(
                    text = "Filtrar por Hábito na Tendência",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedHabitFilterId == null,
                            onClick = { selectedHabitFilterId = null },
                            label = { Text("Geral (Todos)", fontSize = 12.sp) }
                        )
                    }
                    items(uiState.goodHabits) { habit ->
                        FilterChip(
                            selected = selectedHabitFilterId == habit.id,
                            onClick = { selectedHabitFilterId = habit.id },
                            label = { Text(habit.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // PRIMARY VICO CHART: TREND OF DAILY COMPLETION RATE (%)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Taxa de Conclusão Diária (%)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedHabitFilterId != null) "Individual" else "Geral",
                            fontSize = 11.5.sp,
                            color = BolotaAccentDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Variação dia a dia nos últimos $selectedDaysRange dias (Vico Line Chart)",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = trendModelProducer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            }
        }

        // VICO CHART 2: COMPLETION RATE BY WEEKDAY
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Média por Dia da Semana (%)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Dom (0) a Sáb (6) · Descubra seus dias mais fortes e fracos",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = weekdayModelProducer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                }
            }
        }

        // VICO CHART 3: TOTAL COMPLETIONS PER HABIT
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Volume Total por Hábito",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Quantas vezes cada hábito bom foi concluído no período",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = habitCountModelProducer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        countsPerHabit.forEachIndexed { i, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• [${i + 1}] ${pair.first}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${pair.second}x feito", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BolotaAccentDark)
                            }
                        }
                    }
                }
            }
        }

        // BOLOTA'S STATISTICAL INSIGHT CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(BolotaAccentDark.copy(alpha = 0.6f))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BolotaPetView(
                        mood = "feliz",
                        level = uiState.level,
                        wearAccessories = uiState.wornAccessories,
                        size = 72.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BolotaAccentDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Insight do ${uiState.petName}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BolotaAccentDark
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val insightText = when {
                            avgCompletion >= 75 -> "Sua taxa de conclusão de $avgCompletion% está excelente! Você está com alta consistência nos hábitos principais."
                            avgCompletion >= 50 -> "Você está mantendo $avgCompletion% de consistência. Seu melhor dia é ${weekdayNames[bestWeekdayIdx]}. Que tal focar em manter o ritmo nos demais dias?"
                            else -> "Sua consistência está em $avgCompletion%. Lembre-se: o segredo não é perfeição, é recomeçar logo após um dia corrido!"
                        }

                        Text(
                            text = insightText,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
