package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccessStatus
import com.example.data.model.AttendanceRecord
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface CollegeDao {
    // Users
    @Query("SELECT * FROM users ORDER BY createdAt ASC")
    fun observeAllUsers(): Flow<List<UserAccount>>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    fun observeUserByEmail(email: String): Flow<UserAccount?>

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND status = 'APPROVED'")
    suspend fun countApprovedAdmins(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserAccount): Long

    @Update
    suspend fun updateUser(user: UserAccount)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Long, status: AccessStatus)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateUserRole(userId: Long, role: UserRole)

    @Query("UPDATE users SET assignedGroupIds = :assignedGroupIds WHERE id = :userId")
    suspend fun updateAssignedGroups(userId: Long, assignedGroupIds: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Long)

    // Groups
    @Query("SELECT * FROM college_groups ORDER BY courseYear ASC, name ASC")
    fun observeAllGroups(): Flow<List<CollegeGroup>>

    @Query("SELECT * FROM college_groups WHERE id = :groupId LIMIT 1")
    suspend fun getGroupById(groupId: Long): CollegeGroup?

    @Query("SELECT * FROM college_groups WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getGroupByName(name: String): CollegeGroup?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGroup(group: CollegeGroup): Long

    @Update
    suspend fun updateGroup(group: CollegeGroup)

    @Query("DELETE FROM college_groups WHERE id = :groupId")
    suspend fun deleteGroupById(groupId: Long)

    // Students
    @Query("SELECT * FROM students ORDER BY groupId ASC, orderNumber ASC")
    fun observeAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupId = :groupId ORDER BY orderNumber ASC")
    suspend fun getStudentsForGroupOnce(groupId: Long): List<Student>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("UPDATE students SET fullName = :fullName WHERE id = :studentId")
    suspend fun renameStudent(studentId: Long, fullName: String)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Long)

    // Attendance
    @Query("SELECT * FROM attendance_records ORDER BY weekStartIso DESC, dayOfWeek ASC")
    fun observeAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendance(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttendanceList(records: List<AttendanceRecord>)

    @Query("DELETE FROM attendance_records WHERE groupId = :groupId AND weekStartIso = :weekStartIso AND dayOfWeek = :dayOfWeek")
    suspend fun clearDayAttendance(groupId: Long, weekStartIso: String, dayOfWeek: Int)
}
