package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val labelKy: String) {
    ADMIN("Администратор"),
    STAROSTA("Староста")
}

enum class AccessStatus(val labelKy: String) {
    PENDING("Күтүүдө"),
    APPROVED("Уруксат берилди"),
    REVOKED("Жабылды")
}

enum class AttendanceStatus(val labelKy: String, val shortKy: String) {
    PRESENT("Келди", "К"),
    ABSENT("Келген жок", "Ж"),
    EXCUSED("Себептүү", "С")
}

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val status: AccessStatus,
    val assignedGroupIds: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun assignedGroupIdSet(): Set<Long> =
        assignedGroupIds.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .toSet()
}

@Entity(
    tableName = "college_groups",
    indices = [Index(value = ["name"], unique = true)]
)
data class CollegeGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val department: String,
    val courseYear: Int,
    val studentCount: Int,
    val starostaEmail: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = CollegeGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val fullName: String,
    val orderNumber: Int
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = CollegeGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["studentId", "weekStartIso", "dayOfWeek"], unique = true)
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val studentId: Long,
    val weekStartIso: String,
    val dayOfWeek: Int,
    val status: AttendanceStatus,
    val markedByEmail: String,
    val updatedAt: Long = System.currentTimeMillis()
)
