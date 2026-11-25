package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccessStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.domain.CollegeWeeklySummary
import com.example.ui.components.StatusPill
import com.example.ui.components.formatPercent
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusAbsentContainer
import com.example.ui.theme.StatusExcused
import com.example.ui.theme.StatusExcusedContainer
import com.example.ui.theme.StatusPresent
import com.example.ui.theme.StatusPresentContainer

private enum class UserFilter(val labelKy: String) {
    ALL("Баары"),
    PENDING("Күтүүдө"),
    APPROVED("Уруксат берилген")
}

@Composable
fun AdminPanelScreen(
    currentUser: UserAccount,
    allUsers: List<UserAccount>,
    allGroups: List<CollegeGroup>,
    weeklySummary: CollegeWeeklySummary,
    onApproveUser: (UserAccount) -> Unit,
    onRevokeUser: (UserAccount) -> Unit,
    onSetUserRole: (UserAccount, UserRole) -> Unit,
    onAssignGroups: (UserAccount, Set<Long>) -> Unit,
    onDeleteUser: (UserAccount) -> Unit,
    onSignOut: () -> Unit
) {
    val isAdmin = currentUser.role == UserRole.ADMIN
    var selectedFilter by remember { mutableStateOf(UserFilter.ALL) }
    var assigningUser by remember { mutableStateOf<UserAccount?>(null) }

    val filteredUsers = remember(allUsers, selectedFilter) {
        when (selectedFilter) {
            UserFilter.ALL -> allUsers
            UserFilter.PENDING -> allUsers.filter { it.status == AccessStatus.PENDING }
            UserFilter.APPROVED -> allUsers.filter { it.status == AccessStatus.APPROVED }
        }
    }

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
                // Active Account Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(
                                text = currentUser.role.labelKy,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (!isAdmin) {
                            val assignedNames = allGroups
                                .filter {
                                    it.id in currentUser.assignedGroupIdSet() ||
                                        it.starostaEmail.equals(currentUser.email, ignoreCase = true)
                                }
                                .joinToString(", ") { it.name }
                                .ifBlank { "Азырынча группа бекитиле элек" }
                            Text(
                                text = "Бекитилген группалар: $assignedNames",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_sign_out_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Аккаунттан чыгуу / Алмаштыруу")
                        }
                    }
                }

                if (isAdmin) {
                    // College Admin Overview Strip
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Жалал-Абад колледжи", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Жалпы: ${allGroups.size} группа • ${weeklySummary.totalStudents} студент",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "Жума: ${formatPercent(weeklySummary.overallPercent)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text(
                        text = "Админ панель — Gmail доступ башкаруу",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserFilter.entries.forEach { filter ->
                            val count = when (filter) {
                                UserFilter.ALL -> allUsers.size
                                UserFilter.PENDING -> allUsers.count { it.status == AccessStatus.PENDING }
                                UserFilter.APPROVED -> allUsers.count { it.status == AccessStatus.APPROVED }
                            }
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text("${filter.labelKy} ($count)") },
                                modifier = Modifier.testTag("admin_filter_${filter.name}")
                            )
                        }
                    }
                }
            }
        }

        if (isAdmin) {
            items(filteredUsers, key = { it.id }) { user ->
                val isSelf = user.email.equals(currentUser.email, ignoreCase = true)
                val assignedGroupNames = allGroups
                    .filter {
                        it.id in user.assignedGroupIdSet() ||
                            it.starostaEmail.equals(user.email, ignoreCase = true)
                    }
                    .joinToString(", ") { it.name }

                Card(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .testTag("admin_user_card_${user.id}"),
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
                                    text = user.fullName + if (isSelf) " (Сиз)" else "",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val (bg, fg) = when (user.status) {
                                AccessStatus.APPROVED -> StatusPresentContainer to StatusPresent
                                AccessStatus.PENDING -> StatusExcusedContainer to StatusExcused
                                AccessStatus.REVOKED -> StatusAbsentContainer to StatusAbsent
                            }
                            StatusPill(
                                text = user.status.labelKy,
                                containerColor = bg,
                                contentColor = fg
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusPill(
                                text = user.role.labelKy,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            if (user.role == UserRole.STAROSTA) {
                                Text(
                                    text = if (assignedGroupNames.isNotBlank()) {
                                        "Группалар: $assignedGroupNames"
                                    } else {
                                        "Группа бекитилген эмес"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isSelf) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (user.status != AccessStatus.APPROVED) {
                                    Button(
                                        onClick = { onApproveUser(user) },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusPresent),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("approve_user_${user.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Доступ берүү")
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onRevokeUser(user) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("revoke_user_${user.id}")
                                    ) {
                                        Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp), tint = StatusAbsent)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Жабуу", color = StatusAbsent)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { assigningUser = user },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .testTag("assign_groups_${user.id}")
                                ) {
                                    Icon(Icons.Default.GroupWork, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Группа")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = {
                                    val nextRole = if (user.role == UserRole.ADMIN) UserRole.STAROSTA else UserRole.ADMIN
                                    onSetUserRole(user, nextRole)
                                }) {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (user.role == UserRole.ADMIN) "Староста кылуу" else "Админ кылуу")
                                }
                                IconButton(onClick = { onDeleteUser(user) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Өчүрүү", tint = StatusAbsent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val targetAssignUser = assigningUser
    if (targetAssignUser != null) {
        var selectedIds by remember(targetAssignUser.id) {
            mutableStateOf(targetAssignUser.assignedGroupIdSet())
        }
        AlertDialog(
            onDismissRequest = { assigningUser = null },
            title = { Text("${targetAssignUser.fullName}: Группаларды бекитүү") },
            text = {
                if (allGroups.isEmpty()) {
                    Text("Алгач «Группалар» бөлүмүнөн группа кошуңуз.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        allGroups.forEach { group ->
                            val checked = group.id in selectedIds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIds = if (checked) selectedIds - group.id else selectedIds + group.id
                                    }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        selectedIds = if (isChecked) selectedIds + group.id else selectedIds - group.id
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${group.name} (${group.studentCount} ст.)")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onAssignGroups(targetAssignUser, selectedIds)
                    assigningUser = null
                }) { Text("Сактоо") }
            },
            dismissButton = {
                TextButton(onClick = { assigningUser = null }) { Text("Жабуу") }
            }
        )
    }
}
