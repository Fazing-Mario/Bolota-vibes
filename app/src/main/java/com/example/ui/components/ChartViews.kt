package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.max

data class ChartPoint(
    val label: String, // e.g., "15", "16"
    val value: Float?
)

@Composable
fun BarChart(
    data: List<ChartPoint>,
    maxVal: Float,
    goalVal: Float? = null,
    goalLabel: String = "meta",
    higherIsBad: Boolean = false,
    unit: String = "",
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val normalColor = BolotaAccentDark
    val warningColor = if (higherIsBad) BolotaDangerDark else BolotaWarnDark
    val goalColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val w = size.width
        val h = size.height
        val leftPad = 28.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 12.dp.toPx()
        val bottomPad = 22.dp.toPx()
        val chartW = w - leftPad - rightPad
        val chartH = h - topPad - bottomPad

        // Draw horizontal grid lines (0, max/2, max)
        val steps = listOf(0f, maxVal / 2f, maxVal)
        steps.forEach { stepVal ->
            val y = topPad + chartH - (stepVal / maxVal * chartH)
            drawLine(
                color = outlineColor,
                start = Offset(leftPad, y),
                end = Offset(w - rightPad, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw goal line if present
        if (goalVal != null && goalVal <= maxVal) {
            val goalY = topPad + chartH - (goalVal / maxVal * chartH)
            drawLine(
                color = goalColor,
                start = Offset(leftPad, goalY),
                end = Offset(w - rightPad, goalY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        if (data.isEmpty()) return@Canvas

        val n = data.size
        val barSlotW = chartW / n
        val barW = barSlotW * 0.6f

        data.forEachIndexed { i, point ->
            val v = point.value ?: return@forEachIndexed
            val clampedV = v.coerceIn(0f, maxVal)
            val barH = max(4f, (clampedV / maxVal) * chartH)
            val x = leftPad + (i * barSlotW) + (barSlotW - barW) / 2f
            val y = topPad + chartH - barH

            val isOverGoal = goalVal != null && if (higherIsBad) v > goalVal else v < goalVal
            val color = if (isOverGoal) warningColor else normalColor

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
fun LineChart(
    data: List<ChartPoint>,
    maxVal: Float = 5f,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val lineColor = BolotaPetOrange
    val dotColor = BolotaPetOrange

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val w = size.width
        val h = size.height
        val leftPad = 28.dp.toPx()
        val rightPad = 8.dp.toPx()
        val topPad = 12.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val chartW = w - leftPad - rightPad
        val chartH = h - topPad - bottomPad

        // Grid lines
        listOf(1f, 3f, 5f).forEach { stepVal ->
            val y = topPad + chartH - (stepVal / maxVal * chartH)
            drawLine(
                color = outlineColor,
                start = Offset(leftPad, y),
                end = Offset(w - rightPad, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (data.isEmpty()) return@Canvas

        val n = data.size
        val stepX = chartW / (n - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, p ->
            val v = p.value
            if (v != null) {
                val clamped = v.coerceIn(1f, maxVal)
                Offset(leftPad + i * stepX, topPad + chartH - (clamped / maxVal * chartH))
            } else null
        }

        val path = Path()
        var started = false
        points.forEach { pt ->
            if (pt != null) {
                if (!started) {
                    path.moveTo(pt.x, pt.y)
                    started = true
                } else {
                    path.lineTo(pt.x, pt.y)
                }
            } else {
                started = false
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEachIndexed { i, pt ->
            if (pt != null) {
                val radius = if (i == n - 1) 5.dp.toPx() else 3.5.dp.toPx()
                drawCircle(color = dotColor, radius = radius, center = pt)
                drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = pt)
            }
        }
    }
}

data class DayDotData(
    val dayLetter: String, // "D", "S", "T", "Q", "Q", "S", "S"
    val count: Int,
    val hasSlip: Boolean,
    val isToday: Boolean
)

@Composable
fun WeekStripView(
    days: List<DayDotData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.dayLetter,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bgColor = when {
                        item.count == 0 -> Color.Transparent
                        item.count == 1 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        item.count in 2..3 -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.primary
                    }
                    val textColor = when {
                        item.count >= 4 -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(bgColor)
                            .border(
                                width = if (item.isToday) 2.dp else 1.5.dp,
                                color = if (item.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.count > 0) {
                            Text(
                                text = item.count.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }

                    // Red slip badge
                    if (item.hasSlip) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(BolotaDangerDark)
                                .border(1.5.dp, MaterialTheme.colorScheme.background, CircleShape)
                        )
                    }
                }
            }
        }
    }
}
