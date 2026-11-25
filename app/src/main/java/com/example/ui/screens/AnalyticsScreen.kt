package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.CollegeWeeklySummary
import com.example.domain.GroupWeeklyStats
import com.example.domain.WeekCalendarUtils
import com.example.ui.components.AttendanceBarChart
import com.example.ui.components.BarChartEntry
import com.example.ui.components.CircularPercentRing
import com.example.ui.components.StatusPill
import com.example.ui.components.WeekNavigatorBar
import com.example.ui.components.formatPercent
import com.example.ui.components.percentageColor
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusExcusedContainer
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentContainer

private enum class AnalyticsTab(val labelKy: String) {
    OVERVIEW("Жалпы обзор"),
    GROUPS("Группалар %"),
    DAILY("Күндүк график"),
    LOW_ATTENDANCE("Төмөн (<75%)")
}

@Composable
fun AnalyticsScreen(
    weeklySummary: CollegeWeeklySummary,
    onShiftWeek: (Int) -> Unit,
    onResetToCurrentWeek: () -> Unit,
    onOpenGroupAttendance: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(AnalyticsTab.OVERVIEW) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeekNavigatorBar(
                    weekStartIso = weeklySummary.weekStartIso,
                    onPreviousWeek = { onShiftWeek(-1) },
                    onNextWeek = { onShiftWeek(1) },
                    onCurrentWeek = onResetToCurrentWeek
                )

                // Interactive Analytics Mode Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == AnalyticsTab.OVERVIEW,
                        onClick = { selectedTab = AnalyticsTab.OVERVIEW },
                        label = { Text(AnalyticsTab.OVERVIEW.labelKy) },
                        leadingIcon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("analytics_btn_overview")
                    )
                    FilterChip(
                        selected = selectedTab == AnalyticsTab.GROUPS,
                        onClick = { selectedTab = AnalyticsTab.GROUPS },
                        label = { Text(AnalyticsTab.GROUPS.labelKy) },
                        leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("analytics_btn_groups")
                    )
                    FilterChip(
                        selected = selectedTab == AnalyticsTab.DAILY,
                        onClick = { selectedTab = AnalyticsTab.DAILY },
                        label = { Text(AnalyticsTab.DAILY.labelKy) },
                        leadingIcon = { Icon(Icons.Default.CalendarViewWeek, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("analytics_btn_daily")
                    )
                    FilterChip(
                        selected = selectedTab == AnalyticsTab.LOW_ATTENDANCE,
                        onClick = { selectedTab = AnalyticsTab.LOW_ATTENDANCE },
                        label = { Text(AnalyticsTab.LOW_ATTENDANCE.labelKy) },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("analytics_btn_low")
                    )
                }

                // Overall Weekly KPI Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Жалал-Абад колледжи",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Жумалык жалпы катышуу",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${weeklySummary.totalGroups} группа • ${weeklySummary.totalStudents} студент",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusPill(
                                    text = "Келди: ${weeklySummary.totalPresentMarks}",
                                    containerColor = StatusPresentContainer,
                                    contentColor = StatusPresent
                                )
                                StatusPill(
                                    text = "Жок: ${weeklySummary.totalAbsentMarks}",
                                    containerColor = StatusAbsentContainer,
                                    contentColor = StatusAbsent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularPercentRing(
                            percentage = weeklySummary.overallPercent,
                            size = 96.dp,
                            strokeWidth = 10.dp
                        )
                    }
                }

                // Share Report Button
                OutlinedButton(
                    onClick = {
                        val reportText = buildShareReport(weeklySummary)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Жалал-Абад колледжи — Жумалык отчёт")
                            putExtra(Intent.EXTRA_TEXT, reportText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Отчётту бөлүшүү"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_analytics_report_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Жумалык отчётту бөлүшүү")
                }
            }
        }

        if (weeklySummary.groupStats.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "Аналитика үчүн маалымат жок",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Группаларды кошуп, жумалык жоктоо белгилегенден кийин проценттер жана графиктер ушул жерден көрүнөт.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            return@LazyColumn
        }

        when (selectedTab) {
            AnalyticsTab.OVERVIEW -> {
                item {
                    Card(
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Группалар боюнча катышуу (%)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AttendanceBarChart(
                                entries = weeklySummary.groupStats.map { stats ->
                                    BarChartEntry(
                                        label = "${stats.group.name} (${stats.totalStudents} ст.)",
                                        percentage = stats.attendancePercent,
                                        subtitle = "Белгиленди: ${stats.markedDaysCount}/6 күн • Келди: ${stats.presentMarks}/${stats.expectedMarksOnMarkedDays}"
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    Card(
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Жуманын күндөрү боюнча",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AttendanceBarChart(
                                entries = weeklySummary.dailyBreakdown.map { day ->
                                    BarChartEntry(
                                        label = day.shortNameKy,
                                        percentage = day.percentage,
                                        subtitle = if (day.expectedCount > 0) "${day.presentCount}/${day.expectedCount} студент" else "Белгиленген эмес"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AnalyticsTab.GROUPS -> {
                items(weeklySummary.groupStats, key = { it.group.id }) { stats ->
                    GroupAnalyticsCard(
                        stats = stats,
                        onOpenAttendance = { onOpenGroupAttendance(stats.group.id) }
                    )
                }
            }

            AnalyticsTab.DAILY -> {
                item {
                    Card(
                        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Дүйшөмбү – Ишемби катышуу графиги",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            AttendanceBarChart(
                                entries = weeklySummary.dailyBreakdown.map { day ->
                                    BarChartEntry(
                                        label = day.shortNameKy,
                                        percentage = day.percentage,
                                        subtitle = if (day.expectedCount > 0) "${day.presentCount}/${day.expectedCount} келди" else "Белгиленген эмес"
                                    )
                                }
                            )
                        }
                    }
                }
            }

            AnalyticsTab.LOW_ATTENDANCE -> {
                val lowGroups = weeklySummary.groupStats.filter {
                    it.markedDaysCount > 0 && it.attendancePercent < 75f
                }
                if (lowGroups.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = StatusPresentContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "75%дан төмөн группа жок!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StatusPresent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Белгиленген группалардын катышуусу нормада.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusPresent
                                )
                            }
                        }
                    }
                } else {
                    items(lowGroups, key = { "low_${it.group.id}" }) { stats ->
                        GroupAnalyticsCard(
                            stats = stats,
                            onOpenAttendance = { onOpenGroupAttendance(stats.group.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupAnalyticsCard(
    stats: GroupWeeklyStats,
    onOpenAttendance: () -> Unit
) {
    Card(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stats.group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stats.group.department} • ${stats.group.courseYear}-курс • ${stats.totalStudents} студент",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatPercent(stats.attendancePercent),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = percentageColor(stats.attendancePercent)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    text = "Келди: ${stats.presentMarks}",
                    containerColor = StatusPresentContainer,
                    contentColor = StatusPresent
                )
                StatusPill(
                    text = "Себептүү: ${stats.excusedMarks}",
                    containerColor = StatusExcusedContainer,
                    contentColor = StatusExcused
                )
                StatusPill(
                    text = "Жок: ${stats.absentMarks}",
                    containerColor = StatusAbsentContainer,
                    contentColor = StatusAbsent
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Белгиленген күндөр: ${stats.markedDaysCount}/6",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onOpenAttendance,
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("Жоктоого өтүү")
                }
            }
        }
    }
}

private fun buildShareReport(summary: CollegeWeeklySummary): String {
    val sb = StringBuilder()
    sb.appendLine("Жалал-Абад колледжи — Жумалык катышуу отчёту")
    sb.appendLine("Жума: ${WeekCalendarUtils.formatWeekRangeKy(summary.weekStartIso)}")
    sb.appendLine("Жалпы катышуу: ${formatPercent(summary.overallPercent)} (${summary.totalGroups} группа, ${summary.totalStudents} студент)")
    sb.appendLine()
    summary.groupStats.forEachIndexed { idx, g ->
        sb.appendLine("${idx + 1}. ${g.group.name} (${g.totalStudents} ст.): ${formatPercent(g.attendancePercent)} — Келди: ${g.presentMarks}, Жок: ${g.absentMarks}")
    }
    return sb.toString()
}
