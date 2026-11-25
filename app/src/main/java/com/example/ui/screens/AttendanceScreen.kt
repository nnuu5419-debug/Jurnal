package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.domain.CollegeWeeklySummary
import com.example.domain.WeekCalendarUtils
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

@Composable
fun AttendanceScreen(
    accessibleGroups: List<CollegeGroup>,
    selectedGroupId: Long?,
    allStudents: List<Student>,
    allAttendance: List<AttendanceRecord>,
    selectedWeekStartIso: String,
    selectedDayOfWeek: Int,
    weeklySummary: CollegeWeeklySummary,
    userRole: UserRole,
    onSelectGroup: (Long) -> Unit,
    onShiftWeek: (Int) -> Unit,
    onResetToCurrentWeek: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onMarkAttendance: (groupId: Long, studentId: Long, status: AttendanceStatus) -> Unit,
    onMarkAllDay: (groupId: Long, status: AttendanceStatus) -> Unit,
    onClearDay: (groupId: Long) -> Unit,
    onGoToGroups: () -> Unit
) {
    var showWeeklyMatrix by remember { mutableStateOf(false) }

    if (accessibleGroups.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.widthIn(max = 440.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (userRole == UserRole.ADMIN) {
                            "Азырынча группа кошула элек"
                        } else {
                            "Сизге группа бекитиле элек"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (userRole == UserRole.ADMIN) {
                            "Жоктоо жүргүзүү үчүн алгач «Группалар» бөлүмүнөн группа киргизип, студенттердин санын көрсөтүңүз."
                        } else {
                            "Администратор сиздин Gmail аккаунтуңузга группа бекиткенден кийин жумалык жоктоо ачылат."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (userRole == UserRole.ADMIN) {
                        Button(
                            onClick = onGoToGroups,
                            modifier = Modifier.testTag("empty_attendance_add_group_btn")
                        ) {
                            Text("Группа кошууга өтүү")
                        }
                    }
                }
            }
        }
        return
    }

    val activeGroup = accessibleGroups.firstOrNull { it.id == selectedGroupId } ?: accessibleGroups.first()
    val groupStudents = remember(allStudents, activeGroup.id) {
        allStudents.filter { it.groupId == activeGroup.id }
    }
    val weekDays = remember(selectedWeekStartIso) {
        WeekCalendarUtils.getWeekDays(selectedWeekStartIso)
    }
    val weekRecords = remember(allAttendance, activeGroup.id, selectedWeekStartIso) {
        allAttendance.filter {
            it.groupId == activeGroup.id && it.weekStartIso == selectedWeekStartIso
        }
    }
    val dayRecordsMap = remember(weekRecords, selectedDayOfWeek) {
        weekRecords.filter { it.dayOfWeek == selectedDayOfWeek }
            .associateBy { it.studentId }
    }
    val groupWeekStats = weeklySummary.groupStats.firstOrNull { it.group.id == activeGroup.id }
    val dayPercent = groupWeekStats?.dailyPercentages?.get(selectedDayOfWeek) ?: 0f
    val weekPercent = groupWeekStats?.attendancePercent ?: 0f

    val dayPresentCount = dayRecordsMap.values.count { it.status == AttendanceStatus.PRESENT }
    val dayAbsentCount = dayRecordsMap.values.count { it.status == AttendanceStatus.ABSENT }
    val dayExcusedCount = dayRecordsMap.values.count { it.status == AttendanceStatus.EXCUSED }

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
                    weekStartIso = selectedWeekStartIso,
                    onPreviousWeek = { onShiftWeek(-1) },
                    onNextWeek = { onShiftWeek(1) },
                    onCurrentWeek = onResetToCurrentWeek
                )

                // Group Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accessibleGroups.forEach { group ->
                        FilterChip(
                            selected = group.id == activeGroup.id,
                            onClick = { onSelectGroup(group.id) },
                            label = { Text("${group.name} (${group.studentCount})") },
                            modifier = Modifier.testTag("attendance_group_chip_${group.id}")
                        )
                    }
                }

                // Live Daily & Weekly Percentage Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    text = "${activeGroup.name} • ${activeGroup.studentCount} студент",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${weekDays[selectedDayOfWeek - 1].fullNameKy} (${weekDays[selectedDayOfWeek - 1].dateLabel})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Бүгүн",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatPercent(dayPercent),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = percentageColor(dayPercent)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Жумалык",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatPercent(weekPercent),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = percentageColor(weekPercent)
                                    )
                                }
                            }
                        }

                        // Counters Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CounterBadge("Келди: $dayPresentCount", StatusPresentContainer, StatusPresent, Modifier.weight(1f))
                            CounterBadge("Себептүү: $dayExcusedCount", StatusExcusedContainer, StatusExcused, Modifier.weight(1f))
                            CounterBadge("Жок: $dayAbsentCount", StatusAbsentContainer, StatusAbsent, Modifier.weight(1f))
                        }
                    }
                }

                // Day of Week Selector Row (Mon - Sat)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weekDays.forEach { dayInfo ->
                        val isSelected = dayInfo.dayOfWeek == selectedDayOfWeek
                        val hasRecords = weekRecords.any { it.dayOfWeek == dayInfo.dayOfWeek }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(MaterialTheme.shapes.small)
                                .clickable { onSelectDay(dayInfo.dayOfWeek) }
                                .testTag("day_tab_${dayInfo.dayOfWeek}"),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (isSelected) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            tonalElevation = if (isSelected) 4.dp else 1.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = dayInfo.shortNameKy,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = dayInfo.dateLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                hasRecords && isSelected -> Color(0xFFF0BE59)
                                                hasRecords -> StatusPresent
                                                else -> Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                // Quick Bulk Action Buttons + View Mode Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onMarkAllDay(activeGroup.id, AttendanceStatus.PRESENT) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusPresent),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("mark_all_present_btn")
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Баары келди", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { onClearDay(activeGroup.id) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("clear_day_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Тазалоо", modifier = Modifier.size(18.dp))
                    }

                    OutlinedButton(
                        onClick = { showWeeklyMatrix = !showWeeklyMatrix },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("toggle_weekly_matrix_btn")
                    ) {
                        Icon(
                            imageVector = if (showWeeklyMatrix) Icons.Default.ViewAgenda else Icons.Default.GridView,
                            contentDescription = "Жумалык таблица",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showWeeklyMatrix) "Тизме" else "Жума")
                    }
                }
            }
        }

        if (showWeeklyMatrix) {
            // Compact 6-Day Weekly Matrix Card per Student
            items(groupStudents, key = { "matrix_${it.id}" }) { student ->
                val studentWeekMap = weekRecords
                    .filter { it.studentId == student.id }
                    .associateBy { it.dayOfWeek }

                Card(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${student.orderNumber}. ${student.fullName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..6).forEach { day ->
                                val st = studentWeekMap[day]?.status
                                val (bg, fg, label) = when (st) {
                                    AttendanceStatus.PRESENT -> Triple(StatusPresentContainer, StatusPresent, "К")
                                    AttendanceStatus.ABSENT -> Triple(StatusAbsentContainer, StatusAbsent, "Ж")
                                    AttendanceStatus.EXCUSED -> Triple(StatusExcusedContainer, StatusExcused, "С")
                                    null -> Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "—")
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bg)
                                        .clickable { onSelectDay(day); showWeeklyMatrix = false },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = fg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Student-by-Student Interactive Attendance List
            items(groupStudents, key = { it.id }) { student ->
                val currentStatus = dayRecordsMap[student.id]?.status
                StudentAttendanceRow(
                    student = student,
                    currentStatus = currentStatus,
                    onMark = { status ->
                        onMarkAttendance(activeGroup.id, student.id, status)
                    }
                )
            }
        }
    }
}

@Composable
private fun CounterBadge(
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp)
        )
    }
}

@Composable
private fun StudentAttendanceRow(
    student: Student,
    currentStatus: AttendanceStatus?,
    onMark: (AttendanceStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .widthIn(max = 720.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${student.orderNumber}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = student.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (currentStatus != null) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = currentStatus.labelKy,
                        tint = when (currentStatus) {
                            AttendanceStatus.PRESENT -> StatusPresent
                            AttendanceStatus.ABSENT -> StatusAbsent
                            AttendanceStatus.EXCUSED -> StatusExcused
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AttendanceOptionButton(
                    label = "Келди",
                    selected = currentStatus == AttendanceStatus.PRESENT,
                    activeColor = StatusPresent,
                    activeContainer = StatusPresentContainer,
                    testTag = "mark_present_${student.id}",
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.PRESENT) }
                )
                AttendanceOptionButton(
                    label = "Себептүү",
                    selected = currentStatus == AttendanceStatus.EXCUSED,
                    activeColor = StatusExcused,
                    activeContainer = StatusExcusedContainer,
                    testTag = "mark_excused_${student.id}",
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.EXCUSED) }
                )
                AttendanceOptionButton(
                    label = "Жок",
                    selected = currentStatus == AttendanceStatus.ABSENT,
                    activeColor = StatusAbsent,
                    activeContainer = StatusAbsentContainer,
                    testTag = "mark_absent_${student.id}",
                    modifier = Modifier.weight(1f),
                    onClick = { onMark(AttendanceStatus.ABSENT) }
                )
            }
        }
    }
}

@Composable
private fun AttendanceOptionButton(
    label: String,
    selected: Boolean,
    activeColor: Color,
    activeContainer: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bg = if (selected) activeColor else activeContainer.copy(alpha = 0.45f)
    val fg = if (selected) Color.White else activeColor
    val borderColor = if (selected) activeColor else activeColor.copy(alpha = 0.28f)

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}
