package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BadHabit
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosModalBottomSheet(
    badHabits: List<BadHabit>,
    petName: String,
    onResisted: (BadHabit) -> Unit,
    onRelapsed: (BadHabit) -> Unit,
    onDismiss: () -> Unit,
    onAskAiAdvice: suspend (BadHabit) -> String
) {
    var selectedHabit by remember { mutableStateOf<BadHabit?>(null) }
    var secondsLeft by remember { mutableIntStateOf(600) } // 10 minutes
    var isTimerRunning by remember { mutableStateOf(false) }
    var aiAdviceText by remember { mutableStateOf<String?>(null) }
    var isAiLoading by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (secondsLeft > 0 && isTimerRunning) {
                delay(1000L)
                secondsLeft--
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.testTag("sos_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = BolotaDangerDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedHabit == null) "O que tá puxando agora?" else "Segura 10 minutos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedHabit == null) {
                // Step 1: Select Habit
                Text(
                    text = "Apertar aqui já é metade da vitória. Escolha o que você quer evitar:",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                badHabits.forEach { habit ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                selectedHabit = habit
                                isTimerRunning = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline)
                        )
                    ) {
                        Text(
                            text = habit.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                val habit = selectedHabit!!

                // Display user's personalized "Se... então..." plan
                if (habit.plan != null && habit.plan.se.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "SEU PLANO DE AÇÃO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Se ${habit.plan.se}, então ${habit.plan.entao}.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Bolota's immediate firm coaching
                val initialLine = when (habit.id) {
                    "doces" -> "Para. Você não tá com fome, tá com vontade. São coisas bem diferentes. Bebe água primeiro!"
                    "privado" -> "Levanta e sai do quarto agora. Não negocia com o impulso. Celular em outro cômodo!"
                    "procrast" -> "Você não precisa terminar tudo hoje, só começar. Dois minutos na tarefa mais chata."
                    else -> "Respira fundo. Essa vontade é uma onda de 10 minutos, não uma ordem. Sai desse ambiente agora."
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.5.dp, BolotaDangerDark, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = petName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BolotaDangerDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = aiAdviceText ?: initialLine,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Ask AI for personalized advice
                OutlinedButton(
                    onClick = {
                        if (!isAiLoading) {
                            isAiLoading = true
                            aiAdviceText = "Pensando em algo firme pra você..."
                            // Launch coroutine handled in view model or block
                        }
                    },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text("$petName, fala mais comigo")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Circular countdown timer
                val progress = (secondsLeft / 600f).coerceIn(0f, 1f)
                val minutes = secondsLeft / 60
                val seconds = secondsLeft % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Ring background
                            drawCircle(
                                color = Color.Gray.copy(alpha = 0.2f),
                                radius = size.minDimension / 2f - 4.dp.toPx(),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            // Progress ring
                            drawArc(
                                color = BolotaDangerDark,
                                startAngle = -90f,
                                sweepAngle = 360f * progress,
                                useCenter = false,
                                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Text(
                            text = if (secondsLeft > 0) timeFormatted else "Passou!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "A vontade atinge o pico e desce.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Faça uma das ações abaixo para quebrar o ciclo neuroquímico.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Actions list
                val actions = when (habit.id) {
                    "doces" -> listOf(
                        "1. Bebe um copo cheio de água gelada",
                        "2. Escova os dentes (corta a vontade imediatamente)",
                        "3. Se for fome de verdade: fruta ou proteína",
                        "4. Afasta-se da cozinha por 10 minutos"
                    )
                    "privado" -> listOf(
                        "1. Levanta e sai do quarto imediatamente",
                        "2. Celular carregando em outro cômodo",
                        "3. Faz 10 flexões ou agachamentos agora",
                        "4. Manda mensagem de texto para alguém"
                    )
                    else -> listOf(
                        "1. Levanta e muda de ambiente agora",
                        "2. Bebe um copo de água fresca",
                        "3. 10 respirações lentas soltando o ar devagar",
                        "4. Ocupa as mãos com uma tarefa rápida de 2 minutos"
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    actions.forEach { act ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = act,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Outcome buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onResisted(habit) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sos_resisted_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = BolotaAccentDark),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Resisti! (+20 XP)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = { onRelapsed(habit) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sos_relapsed_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BolotaDangerDark),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(BolotaDangerDark)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Cedi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
