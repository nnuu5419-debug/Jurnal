package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccessStatus
import com.example.data.model.AttendanceStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole =
        UserRole.entries.firstOrNull { it.name == value } ?: UserRole.STAROSTA

    @TypeConverter
    fun fromAccessStatus(value: AccessStatus): String = value.name

    @TypeConverter
    fun toAccessStatus(value: String): AccessStatus =
        AccessStatus.entries.firstOrNull { it.name == value } ?: AccessStatus.PENDING

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus =
        AttendanceStatus.entries.firstOrNull { it.name == value } ?: AttendanceStatus.PRESENT
}
