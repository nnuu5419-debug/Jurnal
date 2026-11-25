package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.CollegeDao
import com.example.data.local.CollegeDatabase
import com.example.data.local.SessionPreferences
import com.example.data.model.AccessStatus
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class CollegeRepository(
    private val database: CollegeDatabase,
    private val dao: CollegeDao,
    private val sessionPreferences: SessionPreferences
) {
    companion object {
        const val OWNER_ADMIN_EMAIL = "nnuu5419@gmail.com"
        private val GMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@gmail\\.com$", RegexOption.IGNORE_CASE)
    }

    val activeEmailFlow: Flow<String?> = sessionPreferences.activeEmailFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserFlow: Flow<UserAccount?> = sessionPreferences.activeEmailFlow.flatMapLatest { email ->
        if (email.isNullOrBlank()) flowOf(null) else dao.observeUserByEmail(email)
    }

    val allUsersFlow: Flow<List<UserAccount>> = dao.observeAllUsers()
    val allGroupsFlow: Flow<List<CollegeGroup>> = dao.observeAllGroups()
    val allStudentsFlow: Flow<List<Student>> = dao.observeAllStudents()
    val allAttendanceFlow: Flow<List<AttendanceRecord>> = dao.observeAllAttendance()

    suspend fun ensureDefaultAdmin() {
        val existingOwner = dao.getUserByEmail(OWNER_ADMIN_EMAIL)
        if (existingOwner == null && dao.countApprovedAdmins() == 0) {
            dao.insertUser(
                UserAccount(
                    email = OWNER_ADMIN_EMAIL,
                    fullName = "Башкы Администратор",
                    role = UserRole.ADMIN,
                    status = AccessStatus.APPROVED,
                    assignedGroupIds = ""
                )
            )
        }
    }

    suspend fun registerOrSignInWithGmail(
        emailInput: String,
        fullNameInput: String,
        requestedRole: UserRole
    ): Result<UserAccount> {
        val normalizedEmail = emailInput.trim().lowercase()
        if (!GMAIL_REGEX.matches(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("Жарактуу @gmail.com почтасын жазыңыз"))
        }
        val existing = dao.getUserByEmail(normalizedEmail)
        if (existing != null) {
            sessionPreferences.setActiveEmail(existing.email)
            return Result.success(existing)
        }
        val cleanName = fullNameInput.trim()
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Аты-жөнүңүздү толук жазыңыз"))
        }

        val approvedAdminCount = dao.countApprovedAdmins()
        val isAutoAdmin = normalizedEmail == OWNER_ADMIN_EMAIL ||
            (approvedAdminCount == 0 && requestedRole == UserRole.ADMIN)

        val newUser = UserAccount(
            email = normalizedEmail,
            fullName = cleanName,
            role = if (isAutoAdmin) UserRole.ADMIN else requestedRole,
            status = if (isAutoAdmin) AccessStatus.APPROVED else AccessStatus.PENDING,
            assignedGroupIds = ""
        )
        val id = dao.insertUser(newUser)
        val created = newUser.copy(id = id)
        sessionPreferences.setActiveEmail(created.email)
        return Result.success(created)
    }

    suspend fun signInExistingAccount(email: String) {
        sessionPreferences.setActiveEmail(email.trim().lowercase())
    }

    suspend fun signOut() {
        sessionPreferences.setActiveEmail(null)
    }

    suspend fun approveUser(userId: Long) {
        dao.updateUserStatus(userId, AccessStatus.APPROVED)
    }

    suspend fun revokeUser(userId: Long) {
        dao.updateUserStatus(userId, AccessStatus.REVOKED)
    }

    suspend fun updateUserRole(userId: Long, role: UserRole) {
        dao.updateUserRole(userId, role)
    }

    suspend fun assignGroupsToStarosta(userId: Long, groupIds: Set<Long>) {
        val serialized = groupIds.sorted().joinToString(",")
        dao.updateAssignedGroups(userId, serialized)
    }

    suspend fun deleteUser(userId: Long) {
        dao.deleteUser(userId)
    }

    suspend fun createGroupWithStudentCount(
        name: String,
        department: String,
        courseYear: Int,
        studentCount: Int,
        starostaEmail: String
    ): Result<Long> {
        val cleanName = name.trim()
        val cleanDept = department.trim().ifBlank { "Жалпы бөлүм" }
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Группанын аталышын жазыңыз"))
        }
        if (studentCount !in 1..200) {
            return Result.failure(IllegalArgumentException("Студенттердин саны 1ден 200гө чейин болушу керек"))
        }
        if (dao.getGroupByName(cleanName) != null) {
            return Result.failure(IllegalArgumentException("Мындай аталыштагы группа мурун кошулган"))
        }

        val groupId = database.withTransaction {
            val newId = dao.insertGroup(
                CollegeGroup(
                    name = cleanName,
                    department = cleanDept,
                    courseYear = courseYear.coerceIn(1, 6),
                    studentCount = studentCount,
                    starostaEmail = starostaEmail.trim().lowercase()
                )
            )
            val students = (1..studentCount).map { num ->
                Student(
                    groupId = newId,
                    fullName = "Студент $num",
                    orderNumber = num
                )
            }
            dao.insertStudents(students)
            newId
        }
        return Result.success(groupId)
    }

    suspend fun updateGroupAndCount(
        group: CollegeGroup,
        newName: String,
        newDepartment: String,
        newCourseYear: Int,
        newStudentCount: Int,
        newStarostaEmail: String
    ): Result<Unit> {
        val cleanName = newName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Группанын аталышын жазыңыз"))
        }
        if (newStudentCount !in 1..200) {
            return Result.failure(IllegalArgumentException("Студенттердин саны 1ден 200гө чейин болушу керек"))
        }
        val duplicate = dao.getGroupByName(cleanName)
        if (duplicate != null && duplicate.id != group.id) {
            return Result.failure(IllegalArgumentException("Мындай аталыштагы башка группа бар"))
        }

        database.withTransaction {
            dao.updateGroup(
                group.copy(
                    name = cleanName,
                    department = newDepartment.trim().ifBlank { "Жалпы бөлүм" },
                    courseYear = newCourseYear.coerceIn(1, 6),
                    studentCount = newStudentCount,
                    starostaEmail = newStarostaEmail.trim().lowercase()
                )
            )
            val currentStudents = dao.getStudentsForGroupOnce(group.id)
            if (newStudentCount > currentStudents.size) {
                val startOrder = (currentStudents.maxOfOrNull { it.orderNumber } ?: 0) + 1
                val extraCount = newStudentCount - currentStudents.size
                val additions = (0 until extraCount).map { idx ->
                    val num = startOrder + idx
                    Student(
                        groupId = group.id,
                        fullName = "Студент $num",
                        orderNumber = num
                    )
                }
                dao.insertStudents(additions)
            } else if (newStudentCount < currentStudents.size) {
                val toRemove = currentStudents.drop(newStudentCount)
                toRemove.forEach { dao.deleteStudentById(it.id) }
            }
        }
        return Result.success(Unit)
    }

    suspend fun deleteGroup(groupId: Long) {
        dao.deleteGroupById(groupId)
    }

    suspend fun addStudentToGroup(groupId: Long, fullName: String): Result<Unit> {
        val cleanName = fullName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Студенттин аты-жөнүн жазыңыз"))
        }
        database.withTransaction {
            val group = dao.getGroupById(groupId) ?: return@withTransaction
            val existing = dao.getStudentsForGroupOnce(groupId)
            val nextOrder = (existing.maxOfOrNull { it.orderNumber } ?: 0) + 1
            dao.insertStudent(
                Student(
                    groupId = groupId,
                    fullName = cleanName,
                    orderNumber = nextOrder
                )
            )
            dao.updateGroup(group.copy(studentCount = existing.size + 1))
        }
        return Result.success(Unit)
    }

    suspend fun renameStudent(studentId: Long, newName: String): Result<Unit> {
        val cleanName = newName.trim()
        if (cleanName.isBlank()) {
            return Result.failure(IllegalArgumentException("Аты-жөнү бош болбошу керек"))
        }
        dao.renameStudent(studentId, cleanName)
        return Result.success(Unit)
    }

    suspend fun removeStudentFromGroup(groupId: Long, studentId: Long) {
        database.withTransaction {
            dao.deleteStudentById(studentId)
            val group = dao.getGroupById(groupId) ?: return@withTransaction
            val remaining = dao.getStudentsForGroupOnce(groupId)
            dao.updateGroup(group.copy(studentCount = remaining.size))
        }
    }

    suspend fun markAttendance(
        groupId: Long,
        studentId: Long,
        weekStartIso: String,
        dayOfWeek: Int,
        status: AttendanceStatus,
        markedByEmail: String
    ) {
        dao.upsertAttendance(
            AttendanceRecord(
                groupId = groupId,
                studentId = studentId,
                weekStartIso = weekStartIso,
                dayOfWeek = dayOfWeek,
                status = status,
                markedByEmail = markedByEmail
            )
        )
    }

    suspend fun markAllDayAttendance(
        groupId: Long,
        weekStartIso: String,
        dayOfWeek: Int,
        status: AttendanceStatus,
        markedByEmail: String
    ) {
        val students = dao.getStudentsForGroupOnce(groupId)
        val records = students.map { student ->
            AttendanceRecord(
                groupId = groupId,
                studentId = student.id,
                weekStartIso = weekStartIso,
                dayOfWeek = dayOfWeek,
                status = status,
                markedByEmail = markedByEmail
            )
        }
        if (records.isNotEmpty()) {
            dao.upsertAttendanceList(records)
        }
    }

    suspend fun clearDayAttendance(groupId: Long, weekStartIso: String, dayOfWeek: Int) {
        dao.clearDayAttendance(groupId, weekStartIso, dayOfWeek)
    }
}
