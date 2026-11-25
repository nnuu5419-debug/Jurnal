package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AccessStatus
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.domain.CollegeWeeklySummary
import com.example.domain.WeekCalendarUtils
import com.example.ui.components.CircularPercentRing
import com.example.ui.components.StatusPill
import com.example.ui.components.formatPercent
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusExcusedContainer

@Composable
fun HomeHubScreen(
    currentUser: UserAccount,
    pendingUsersCount: Int,
    weeklySummary: CollegeWeeklySummary,
    onOpenAttendance: () -> Unit,
    onOpenAnalytics: () -> Unit,
    onOpenGroups: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    val isAdmin = currentUser.role == UserRole.ADMIN

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero College Card with Weekly Percentage Ring
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_college_banner),
                            contentDescription = "Жалал-Абад колледжи",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF063228).copy(alpha = 0.94f),
                                            Color(0xFF0D5C4B).copy(alpha = 0.78f)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(205.dp)
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                StatusPill(
                                    text = currentUser.role.labelKy,
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                )
                                Text(
                                    text = "Жалал-Абад колледжи",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Салам, ${currentUser.fullName}!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = WeekCalendarUtils.formatWeekRangeKy(weeklySummary.weekStartIso),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFFBEEC9)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularPercentRing(
                                    percentage = weeklySummary.overallPercent,
                                    size = 92.dp,
                                    strokeWidth = 9.dp,
                                    ringColor = Color(0xFFF0BE59),
                                    trackColor = Color.White.copy(alpha = 0.22f),
                                    textColor = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Жумалык %",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }

                // Quick Metric Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricChipCard(
                        title = "Группалар",
                        value = "${weeklySummary.totalGroups}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChipCard(
                        title = "Студенттер",
                        value = "${weeklySummary.totalStudents}",
                        modifier = Modifier.weight(1f)
                    )
                    MetricChipCard(
                        title = "Келди",
                        value = "${weeklySummary.totalPresentMarks}",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pending Access Alert for Admin
                if (isAdmin && pendingUsersCount > 0) {
                    Surface(
                        color = StatusExcusedContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = onOpenAdminPanel)
                            .testTag("pending_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = StatusExcused
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$pendingUsersCount жаңы Gmail өтүнүчү күтүүдө",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = StatusExcused,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Старосталарга доступ берүү үчүн басыңыз",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusExcused
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = StatusExcused
                            )
                        }
                    }
                }

                Text(
                    text = "Бөлүмдөр жана тез баскычтар",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Dedicated Button for Weekly Attendance Page
                DedicatedActionCard(
                    title = "Жумалык жоктоо",
                    subtitle = "Студенттердин колледжге келүүсүн күн сайын белгилөө",
                    icon = Icons.AutoMirrored.Filled.FactCheck,
                    accentColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                    testTag = "hub_btn_attendance",
                    onClick = onOpenAttendance
                )

                // Dedicated Button for Analytics Page
                DedicatedActionCard(
                    title = "Аналитика жана проценттер",
                    subtitle = "Ар бир жуманын жана ар бир группанын катышуу проценти",
                    icon = Icons.Default.Analytics,
                    accentColor = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    testTag = "hub_btn_analytics",
                    onClick = onOpenAnalytics
                )

                // Dedicated Button for Groups & Students Page
                DedicatedActionCard(
                    title = "Группалар жана студент саны",
                    subtitle = if (isAdmin) {
                        "Группа кошуу, студенттердин санын көрсөтүү жана тизмесин башкаруу"
                    } else {
                        "Бекитилген группалар жана студенттердин тизмеси"
                    },
                    icon = Icons.Default.Groups,
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f),
                    testTag = "hub_btn_groups",
                    onClick = onOpenGroups
                )

                // Dedicated Button for Weekly History Page
                DedicatedActionCard(
                    title = "Жумалык отчёттор",
                    subtitle = "Өткөн жумалардын статистикасы жана салыштыруу",
                    icon = Icons.Default.History,
                    accentColor = Color(0xFF2E6B5E),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    testTag = "hub_btn_history",
                    onClick = onOpenHistory
                )

                // Dedicated Button for Admin Panel
                DedicatedActionCard(
                    title = if (isAdmin) "Админ панель (Доступ берүү)" else "Менин профилим & Доступ",
                    subtitle = if (isAdmin) {
                        "Gmail аккаунттарга доступ берүү, старосталарды бекитүү"
                    } else {
                        "Аккаунт маалыматы жана системадан чыгуу"
                    },
                    icon = Icons.Default.AdminPanelSettings,
                    accentColor = Color(0xFF7B4E1D),
                    containerColor = MaterialTheme.colorScheme.surface,
                    badgeText = if (isAdmin && pendingUsersCount > 0) "$pendingUsersCount" else null,
                    testTag = "hub_btn_admin",
                    onClick = onOpenAdminPanel
                )

                // Top / Lowest Group Quick Highlight
                if (weeklySummary.groupStats.isNotEmpty()) {
                    val best = weeklySummary.groupStats.first()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Жуманын алдыңкы группасы",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = best.group.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${best.totalStudents} студент • ${best.markedDaysCount}/6 күн белгиленди",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = formatPercent(best.attendancePercent),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = JetBrainsMonoFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChipCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DedicatedActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    testTag: String,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = accentColor,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!badgeText.isNullOrBlank()) {
                        Surface(
                            color = StatusExcused,
                            shape = CircleShape
                        ) {
                            Text(
                                text = badgeText,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accentColor
            )
        }
    }
}
