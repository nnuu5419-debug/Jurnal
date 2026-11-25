package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.WeekCalendarUtils
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusPresent
import java.util.Locale

@Composable
fun CircularPercentRing(
    percentage: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 9.dp,
    ringColor: Color = percentageColor(percentage),
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val clamped = percentage.coerceIn(0f, 100f)
    val animatedSweep by animateFloatAsState(
        targetValue = (clamped / 100f) * 360f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "ringSweep"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animatedSweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = formatPercent(clamped),
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold
            ),
            color = textColor
        )
    }
}

data class BarChartEntry(
    val label: String,
    val percentage: Float,
    val subtitle: String = ""
)

@Composable
fun AttendanceBarChart(
    entries: List<BarChartEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val thresholdColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        entries.forEach { entry ->
            val pct = entry.percentage.coerceIn(0f, 100f)
            val animatedFraction by animateFloatAsState(
                targetValue = pct / 100f,
                animationSpec = tween(550, easing = FastOutSlowInEasing),
                label = "barFraction"
            )
            val barColor = percentageColor(pct)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.subtitle.isNotBlank()) {
                            Text(
                                text = entry.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatPercent(pct),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = barColor
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        size = size
                    )
                    val filledWidth = (size.width * animatedFraction).coerceAtLeast(
                        if (pct > 0f) 6.dp.toPx() else 0f
                    )
                    if (filledWidth > 0f) {
                        drawRoundRect(
                            color = barColor,
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                            size = Size(filledWidth, size.height)
                        )
                    }
                    // 75% benchmark tick
                    val markX = size.width * 0.75f
                    drawLine(
                        color = thresholdColor,
                        start = Offset(markX, 0f),
                        end = Offset(markX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
    }
}

@Composable
fun WeekNavigatorBar(
    weekStartIso: String,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onCurrentWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrent = weekStartIso == WeekCalendarUtils.currentWeekStartIso()
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPreviousWeek,
                modifier = Modifier.testTag("prev_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Мурунку жума"
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = WeekCalendarUtils.formatWeekRangeKy(weekStartIso),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!isCurrent) {
                    TextButton(
                        onClick = onCurrentWeek,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text(
                            text = "Учурдагы жумага кайтуу",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                } else {
                    Text(
                        text = "Учурдагы окуу жумасы",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = onNextWeek,
                modifier = Modifier.testTag("next_week_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Кийинки жума"
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun percentageColor(percent: Float): Color = when {
    percent >= 80f -> StatusPresent
    percent >= 60f -> StatusExcused
    else -> StatusAbsent
}

fun formatPercent(percent: Float): String {
    return if (percent % 1f == 0f) {
        "${percent.toInt()}%"
    } else {
        String.format(Locale.US, "%.1f%%", percent)
    }
}
