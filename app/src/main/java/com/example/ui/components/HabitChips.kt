package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.*

@Composable
fun GoodHabitChip(
    habit: GoodHabit,
    isDone: Boolean,
    isTodayScheduled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        label = "chip_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "chip_border"
    )

    Row(
        modifier = modifier
            .testTag("good_habit_${habit.id}")
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox square
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent)
                .border(2.dp, if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Concluído",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val subText = when {
                habit.id == "treino" && isTodayScheduled -> "dia de treino"
                habit.id == "treino" && !isTodayScheduled -> "opcional hoje"
                habit.days.isNotEmpty() && isTodayScheduled -> "programado para hoje"
                habit.id == "foco" -> "25+ min sem celular"
                habit.id == "dormir" -> "desligar telas antes"
                habit.id == "agua" -> "ao longo do dia"
                else -> ""
            }
            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    fontSize = 11.5.sp,
                    color = if (isTodayScheduled && habit.days.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BadHabitCard(
    habit: BadHabit,
    cleanDays: Int,
    isArmed: Boolean,
    onSlipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val best = maxOf(habit.best, cleanDays)
    val subtitle = if (cleanDays == 0) "recomeço hoje. Bora!" else "recorde: $best ${if (best == 1) "dia" else "dias"}"

    Row(
        modifier = modifier
            .testTag("bad_habit_${habit.id}")
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Counter
        Column(
            modifier = Modifier.width(62.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = cleanDays.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = if (cleanDays > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                text = if (cleanDays == 1) "dia" else "dias",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (habit.plan != null && habit.plan.se.isNotBlank()) {
                Text(
                    text = "Se ${habit.plan.se} → ${habit.plan.entao}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Armed Slip Button (requires 2 taps to prevent accidents)
        Button(
            onClick = onSlipClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isArmed) BolotaDangerSoftDark else Color.Transparent,
                contentColor = if (isArmed) BolotaDangerDark else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isArmed) BolotaDangerDark else MaterialTheme.colorScheme.outline
                )
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("slip_button_${habit.id}")
        ) {
            Text(
                text = if (isArmed) "Confirmar?" else "Escorreguei",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
