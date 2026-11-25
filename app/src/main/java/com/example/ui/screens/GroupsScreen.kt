package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserRole
import com.example.domain.CollegeWeeklySummary
import com.example.ui.components.StatusPill
import com.example.ui.components.formatPercent
import com.example.ui.components.percentageColor
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.StatusAbsent

@Composable
fun GroupsScreen(
    accessibleGroups: List<CollegeGroup>,
    weeklySummary: CollegeWeeklySummary,
    userRole: UserRole,
    onCreateGroup: (name: String, dept: String, courseYear: Int, studentCount: Int, starostaEmail: String) -> Unit,
    onOpenGroupDetail: (Long) -> Unit,
    onOpenGroupAttendance: (Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val isAdmin = userRole == UserRole.ADMIN

    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Группа кошуу") },
                    modifier = Modifier.testTag("add_group_fab")
                )
            }
        }
    ) { innerPadding ->
        if (accessibleGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                            text = "Группалар азырынча жок",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdmin) {
                                "Төмөнкү «Группа кошуу» баскычы аркылуу группаны жана анда канча студент бар экенин киргизиңиз."
                            } else {
                                "Администратор сизге группа бекитип бергенден кийин бул жерден көрүнөт."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (isAdmin) {
                            Button(
                                onClick = { showCreateDialog = true },
                                modifier = Modifier.testTag("empty_add_group_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Группа кошуу")
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(accessibleGroups, key = { it.id }) { group ->
                    val stats = weeklySummary.groupStats.firstOrNull { it.group.id == group.id }
                    val weekPct = stats?.attendancePercent ?: 0f

                    Card(
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { onOpenGroupDetail(group.id) }
                            .testTag("group_card_${group.id}"),
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
                                        text = group.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${group.department} • ${group.courseYear}-курс",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatPercent(weekPct),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = percentageColor(weekPct)
                                    )
                                    Text(
                                        text = "Жумалык %",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusPill(
                                    text = "${group.studentCount} студент",
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (group.starostaEmail.isNotBlank()) {
                                    Text(
                                        text = "Староста: ${group.starostaEmail}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onOpenGroupDetail(group.id) },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Text("Студенттер")
                                }
                                Button(
                                    onClick = { onOpenGroupAttendance(group.id) },
                                    modifier = Modifier.weight(1f).height(44.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Жоктоо")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        GroupFormDialog(
            title = "Жаңы группа кошуу",
            initialName = "",
            initialDept = "",
            initialCourse = 1,
            initialStudentCount = "20",
            initialStarostaEmail = "",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, dept, course, count, starosta ->
                onCreateGroup(name, dept, course, count, starosta)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun GroupDetailScreen(
    group: CollegeGroup?,
    students: List<Student>,
    userRole: UserRole,
    onBack: () -> Unit,
    onUpdateGroup: (CollegeGroup, String, String, Int, Int, String) -> Unit,
    onDeleteGroup: (CollegeGroup) -> Unit,
    onAddStudent: (Long, String) -> Unit,
    onRenameStudent: (Long, String) -> Unit,
    onRemoveStudent: (Long, Long) -> Unit,
    onOpenAttendance: (Long) -> Unit
) {
    if (group == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Группа табылган жок")
        }
        return
    }

    val isAdmin = userRole == UserRole.ADMIN
    var showEditGroupDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier.widthIn(max = 720.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Артка")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Группаларга кайтуу")
                    }
                    if (isAdmin) {
                        Row {
                            IconButton(onClick = { showEditGroupDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Группаны оңдоо")
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Группаны өчүрүү", tint = StatusAbsent)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${group.department} • ${group.courseYear}-курс • ${students.size} студент",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (group.starostaEmail.isNotBlank()) {
                            Text(
                                text = "Бекитилген староста: ${group.starostaEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onOpenAttendance(group.id) },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Жоктоо белгилөө")
                            }
                            OutlinedButton(
                                onClick = { showAddStudentDialog = true },
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Студент кошуу")
                            }
                        }
                    }
                }

                Text(
                    text = "Студенттердин тизмеси (${students.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(students, key = { it.id }) { student ->
            Card(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${student.orderNumber}. ${student.fullName}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        IconButton(onClick = { editingStudent = student }) {
                            Icon(Icons.Default.Edit, contentDescription = "Атын өзгөртүү")
                        }
                        if (isAdmin && students.size > 1) {
                            IconButton(onClick = { onRemoveStudent(group.id, student.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Өчүрүү", tint = StatusAbsent)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditGroupDialog) {
        GroupFormDialog(
            title = "Группаны жана санын оңдоо",
            initialName = group.name,
            initialDept = group.department,
            initialCourse = group.courseYear,
            initialStudentCount = "${group.studentCount}",
            initialStarostaEmail = group.starostaEmail,
            onDismiss = { showEditGroupDialog = false },
            onConfirm = { name, dept, course, count, starosta ->
                onUpdateGroup(group, name, dept, course, count, starosta)
                showEditGroupDialog = false
            }
        )
    }

    if (showAddStudentDialog) {
        var newStudentName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Жаңы студент кошуу") },
            text = {
                OutlinedTextField(
                    value = newStudentName,
                    onValueChange = { newStudentName = it },
                    label = { Text("Студенттин аты-жөнү") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newStudentName.isNotBlank()) {
                        onAddStudent(group.id, newStudentName)
                        showAddStudentDialog = false
                    }
                }) { Text("Кошуу") }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Жокко чыгаруу") }
            }
        )
    }

    val targetStudent = editingStudent
    if (targetStudent != null) {
        var updatedName by remember(targetStudent.id) { mutableStateOf(targetStudent.fullName) }
        AlertDialog(
            onDismissRequest = { editingStudent = null },
            title = { Text("Студенттин атын оңдоо") },
            text = {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it },
                    label = { Text("Аты-жөнү") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (updatedName.isNotBlank()) {
                        onRenameStudent(targetStudent.id, updatedName)
                        editingStudent = null
                    }
                }) { Text("Сактоо") }
            },
            dismissButton = {
                TextButton(onClick = { editingStudent = null }) { Text("Жабуу") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Группаны өчүрүү") },
            text = { Text("«${group.name}» группасын жана анын бардык жоктоо маалыматтарын өчүрөсүзбү?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteConfirm = false
                    onDeleteGroup(group)
                    onBack()
                }) { Text("Өчүрүү") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Жок") }
            }
        )
    }
}

@Composable
private fun GroupFormDialog(
    title: String,
    initialName: String,
    initialDept: String,
    initialCourse: Int,
    initialStudentCount: String,
    initialStarostaEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, dept: String, courseYear: Int, studentCount: Int, starostaEmail: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var department by remember { mutableStateOf(initialDept) }
    var courseYear by remember { mutableIntStateOf(initialCourse) }
    var studentCountText by remember { mutableStateOf(initialStudentCount) }
    var starostaEmail by remember { mutableStateOf(initialStarostaEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Группанын аты (мис. ИТ-1-24)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_group_name_input")
                )
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Бөлүм / Адистик") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_group_dept_input")
                )
                OutlinedTextField(
                    value = studentCountText,
                    onValueChange = { input ->
                        studentCountText = input.filter { it.isDigit() }.take(3)
                    },
                    label = { Text("Канча студент бар (саны)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_student_count_input")
                )
                Column {
                    Text("Курс:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..4).forEach { c ->
                            FilterChip(
                                selected = courseYear == c,
                                onClick = { courseYear = c },
                                label = { Text("$c") }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = starostaEmail,
                    onValueChange = { starostaEmail = it },
                    label = { Text("Старостанын Gmail (милдеттүү эмес)") },
                    placeholder = { Text("starosta@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val count = studentCountText.toIntOrNull() ?: 0
            Button(
                onClick = { onConfirm(name, department, courseYear, count, starostaEmail) },
                enabled = name.isNotBlank() && count in 1..200,
                modifier = Modifier.testTag("dialog_save_group_btn")
            ) {
                Text("Сактоо")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Жокко чыгаруу") }
        }
    )
}
