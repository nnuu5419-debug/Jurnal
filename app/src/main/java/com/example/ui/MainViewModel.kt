package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CollegeDatabase
import com.example.data.local.SessionPreferences
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.repository.CollegeRepository
import com.example.domain.AnalyticsCalculator
import com.example.domain.CollegeWeeklySummary
import com.example.domain.WeekCalendarUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val isInitialized: Boolean = false,
    val currentUser: UserAccount? = null,
    val allUsers: List<UserAccount> = emptyList(),
    val allGroups: List<CollegeGroup> = emptyList(),
    val accessibleGroups: List<CollegeGroup> = emptyList(),
    val allStudents: List<Student> = emptyList(),
    val allAttendance: List<AttendanceRecord> = emptyList(),
    val selectedWeekStartIso: String = WeekCalendarUtils.currentWeekStartIso(),
    val selectedDayOfWeek: Int = WeekCalendarUtils.todayCollegeDayIndex(),
    val selectedGroupId: Long? = null,
    val weeklySummary: CollegeWeeklySummary = AnalyticsCalculator.calculateWeeklySummary(
        weekStartIso = WeekCalendarUtils.currentWeekStartIso(),
        groups = emptyList(),
        students = emptyList(),
        allAttendance = emptyList()
    ),
    val authError: String? = null,
    val snackbarMessage: String? = null
)

private data class SelectionState(
    val weekStartIso: String,
    val dayOfWeek: Int,
    val groupId: Long?,
    val authError: String?,
    val snackbarMessage: String?,
    val isInitialized: Boolean
)

private data class DatabaseSnapshot(
    val currentUser: UserAccount?,
    val users: List<UserAccount>,
    val groups: List<CollegeGroup>,
    val students: List<Student>,
    val attendance: List<AttendanceRecord>
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CollegeDatabase.getInstance(application)
    private val sessionPreferences = SessionPreferences(application)
    private val repository = CollegeRepository(
        database = database,
        dao = database.collegeDao(),
        sessionPreferences = sessionPreferences
    )

    private val selectedWeekFlow = MutableStateFlow(WeekCalendarUtils.currentWeekStartIso())
    private val selectedDayFlow = MutableStateFlow(WeekCalendarUtils.todayCollegeDayIndex())
    private val selectedGroupIdFlow = MutableStateFlow<Long?>(null)
    private val authErrorFlow = MutableStateFlow<String?>(null)
    private val snackbarFlow = MutableStateFlow<String?>(null)
    private val initializedFlow = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.ensureDefaultAdmin()
            initializedFlow.value = true
        }
    }

    private val timeAndGroupFlow = combine(
        selectedWeekFlow,
        selectedDayFlow,
        selectedGroupIdFlow
    ) { week, day, groupId ->
        Triple(week, day, groupId)
    }

    private val selectionFlow = combine(
        timeAndGroupFlow,
        authErrorFlow,
        snackbarFlow,
        initializedFlow
    ) { timeGroup, authErr, snackbar, initialized ->
        SelectionState(
            weekStartIso = timeGroup.first,
            dayOfWeek = timeGroup.second,
            groupId = timeGroup.third,
            authError = authErr,
            snackbarMessage = snackbar,
            isInitialized = initialized
        )
    }

    private val databaseDataFlow = combine(
        repository.currentUserFlow,
        repository.allUsersFlow,
        repository.allGroupsFlow,
        repository.allStudentsFlow,
        repository.allAttendanceFlow
    ) { currentUser, users, groups, students, attendance ->
        DatabaseSnapshot(
            currentUser = currentUser,
            users = users,
            groups = groups,
            students = students,
            attendance = attendance
        )
    }

    val uiState: StateFlow<MainUiState> = combine(
        databaseDataFlow,
        selectionFlow
    ) { db, selection ->
        val currentUser = db.currentUser
        val users = db.users
        val groups = db.groups
        val students = db.students
        val attendance = db.attendance

        val accessibleGroups = when {
            currentUser == null -> emptyList()
            currentUser.role == UserRole.ADMIN -> groups
            else -> {
                val assignedIds = currentUser.assignedGroupIdSet()
                groups.filter { group ->
                    group.id in assignedIds ||
                        group.starostaEmail.equals(currentUser.email, ignoreCase = true)
                }
            }
        }

        val resolvedGroupId = when {
            accessibleGroups.isEmpty() -> null
            selection.groupId != null && accessibleGroups.any { it.id == selection.groupId } -> selection.groupId
            else -> accessibleGroups.first().id
        }

        val analyticsGroups = if (currentUser?.role == UserRole.ADMIN) groups else accessibleGroups
        val summary = AnalyticsCalculator.calculateWeeklySummary(
            weekStartIso = selection.weekStartIso,
            groups = analyticsGroups,
            students = students,
            allAttendance = attendance
        )

        MainUiState(
            isInitialized = selection.isInitialized,
            currentUser = currentUser,
            allUsers = users,
            allGroups = groups,
            accessibleGroups = accessibleGroups,
            allStudents = students,
            allAttendance = attendance,
            selectedWeekStartIso = selection.weekStartIso,
            selectedDayOfWeek = selection.dayOfWeek,
            selectedGroupId = resolvedGroupId,
            weeklySummary = summary,
            authError = selection.authError,
            snackbarMessage = selection.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )

    fun clearAuthError() {
        authErrorFlow.value = null
    }

    fun clearSnackbar() {
        snackbarFlow.value = null
    }

    fun registerOrSignIn(email: String, fullName: String, role: UserRole) {
        viewModelScope.launch {
            authErrorFlow.value = null
            val result = repository.registerOrSignInWithGmail(email, fullName, role)
            result.onFailure { err ->
                authErrorFlow.value = err.message ?: "Кирүүдө ката кетти"
            }
        }
    }

    fun switchAccount(email: String) {
        viewModelScope.launch {
            authErrorFlow.value = null
            repository.signInExistingAccount(email)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
        }
    }

    fun shiftWeek(delta: Int) {
        selectedWeekFlow.value = WeekCalendarUtils.shiftWeek(selectedWeekFlow.value, delta)
    }

    fun resetToCurrentWeek() {
        selectedWeekFlow.value = WeekCalendarUtils.currentWeekStartIso()
        selectedDayFlow.value = WeekCalendarUtils.todayCollegeDayIndex()
    }

    fun selectWeek(weekStartIso: String) {
        selectedWeekFlow.value = weekStartIso
    }

    fun selectDay(dayOfWeek: Int) {
        selectedDayFlow.value = dayOfWeek.coerceIn(1, 6)
    }

    fun selectGroup(groupId: Long) {
        selectedGroupIdFlow.value = groupId
    }

    fun createGroup(
        name: String,
        department: String,
        courseYear: Int,
        studentCount: Int,
        starostaEmail: String
    ) {
        viewModelScope.launch {
            val result = repository.createGroupWithStudentCount(
                name = name,
                department = department,
                courseYear = courseYear,
                studentCount = studentCount,
                starostaEmail = starostaEmail
            )
            result.fold(
                onSuccess = { newId ->
                    selectedGroupIdFlow.value = newId
                    snackbarFlow.value = "«$name» группасы ($studentCount студент) кошулду"
                },
                onFailure = { err ->
                    snackbarFlow.value = err.message ?: "Группа кошууда ката кетти"
                }
            )
        }
    }

    fun updateGroup(
        group: CollegeGroup,
        newName: String,
        newDepartment: String,
        newCourseYear: Int,
        newStudentCount: Int,
        newStarostaEmail: String
    ) {
        viewModelScope.launch {
            val result = repository.updateGroupAndCount(
                group = group,
                newName = newName,
                newDepartment = newDepartment,
                newCourseYear = newCourseYear,
                newStudentCount = newStudentCount,
                newStarostaEmail = newStarostaEmail
            )
            result.fold(
                onSuccess = {
                    snackbarFlow.value = "«$newName» жаңыртылды ($newStudentCount студент)"
                },
                onFailure = { err ->
                    snackbarFlow.value = err.message ?: "Жаңыртууда ката кетти"
                }
            )
        }
    }

    fun deleteGroup(group: CollegeGroup) {
        viewModelScope.launch {
            repository.deleteGroup(group.id)
            snackbarFlow.value = "«${group.name}» группасы өчүрүлдү"
        }
    }

    fun addStudent(groupId: Long, fullName: String) {
        viewModelScope.launch {
            val result = repository.addStudentToGroup(groupId, fullName)
            result.fold(
                onSuccess = { snackbarFlow.value = "Студент тизмеге кошулду" },
                onFailure = { err -> snackbarFlow.value = err.message }
            )
        }
    }

    fun renameStudent(studentId: Long, newName: String) {
        viewModelScope.launch {
            val result = repository.renameStudent(studentId, newName)
            result.onFailure { err -> snackbarFlow.value = err.message }
        }
    }

    fun removeStudent(groupId: Long, studentId: Long) {
        viewModelScope.launch {
            repository.removeStudentFromGroup(groupId, studentId)
            snackbarFlow.value = "Студент тизмеден алынды"
        }
    }

    fun markStudentAttendance(
        groupId: Long,
        studentId: Long,
        status: AttendanceStatus
    ) {
        val markerEmail = uiState.value.currentUser?.email ?: return
        val weekIso = uiState.value.selectedWeekStartIso
        val day = uiState.value.selectedDayOfWeek
        viewModelScope.launch {
            repository.markAttendance(
                groupId = groupId,
                studentId = studentId,
                weekStartIso = weekIso,
                dayOfWeek = day,
                status = status,
                markedByEmail = markerEmail
            )
        }
    }

    fun markAllStudentsForDay(groupId: Long, status: AttendanceStatus) {
        val markerEmail = uiState.value.currentUser?.email ?: return
        val weekIso = uiState.value.selectedWeekStartIso
        val day = uiState.value.selectedDayOfWeek
        viewModelScope.launch {
            repository.markAllDayAttendance(
                groupId = groupId,
                weekStartIso = weekIso,
                dayOfWeek = day,
                status = status,
                markedByEmail = markerEmail
            )
            snackbarFlow.value = "Бардык студенттер «${status.labelKy}» деп белгиленди"
        }
    }

    fun clearDayAttendance(groupId: Long) {
        val weekIso = uiState.value.selectedWeekStartIso
        val day = uiState.value.selectedDayOfWeek
        viewModelScope.launch {
            repository.clearDayAttendance(groupId, weekIso, day)
            snackbarFlow.value = "Бул күндүн белгилери тазаланды"
        }
    }

    fun approveUser(user: UserAccount) {
        viewModelScope.launch {
            repository.approveUser(user.id)
            snackbarFlow.value = "${user.fullName} үчүн доступ ачылды"
        }
    }

    fun revokeUser(user: UserAccount) {
        if (user.email.equals(uiState.value.currentUser?.email, ignoreCase = true)) {
            snackbarFlow.value = "Өзүңүздүн доступуңузду жаба албайсыз"
            return
        }
        viewModelScope.launch {
            repository.revokeUser(user.id)
            snackbarFlow.value = "${user.fullName} доступу жабылды"
        }
    }

    fun setUserRole(user: UserAccount, role: UserRole) {
        if (user.email.equals(uiState.value.currentUser?.email, ignoreCase = true) && role != UserRole.ADMIN) {
            snackbarFlow.value = "Өзүңүздүн админ ролуңузду өчүрө албайсыз"
            return
        }
        viewModelScope.launch {
            repository.updateUserRole(user.id, role)
            snackbarFlow.value = "${user.fullName}: ${role.labelKy}"
        }
    }

    fun assignGroupsToStarosta(user: UserAccount, groupIds: Set<Long>) {
        viewModelScope.launch {
            repository.assignGroupsToStarosta(user.id, groupIds)
            snackbarFlow.value = "${user.fullName} үчүн ${groupIds.size} группа бекитилди"
        }
    }

    fun deleteUser(user: UserAccount) {
        if (user.email.equals(uiState.value.currentUser?.email, ignoreCase = true)) {
            snackbarFlow.value = "Активдүү аккаунтту өчүрүүгө болбойт"
            return
        }
        viewModelScope.launch {
            repository.deleteUser(user.id)
            snackbarFlow.value = "${user.email} өчүрүлдү"
        }
    }
}
