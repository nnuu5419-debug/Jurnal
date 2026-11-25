package com.example

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.Student
import com.example.domain.AnalyticsCalculator
import com.example.domain.WeekCalendarUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollegeAttendanceUnitTest {

    @Test
    fun `weekly summary calculates accurate group percentage`() {
        val weekIso = "2026-11-23"
        val group = CollegeGroup(
            id = 1L,
            name = "ИТ-1-24",
            department = "Маалыматтык технологиялар",
            courseYear = 1,
            studentCount = 4
        )
        val students = (1..4).map { idx ->
            Student(id = idx.toLong(), groupId = 1L, fullName = "Студент $idx", orderNumber = idx)
        }
        val attendance = listOf(
            AttendanceRecord(1, 1L, 1L, weekIso, 1, AttendanceStatus.PRESENT, "a@gmail.com"),
            AttendanceRecord(2, 1L, 2L, weekIso, 1, AttendanceStatus.PRESENT, "a@gmail.com"),
            AttendanceRecord(3, 1L, 3L, weekIso, 1, AttendanceStatus.PRESENT, "a@gmail.com"),
            AttendanceRecord(4, 1L, 4L, weekIso, 1, AttendanceStatus.ABSENT, "a@gmail.com")
        )

        val summary = AnalyticsCalculator.calculateWeeklySummary(
            weekStartIso = weekIso,
            groups = listOf(group),
            students = students,
            allAttendance = attendance
        )

        assertEquals(75f, summary.overallPercent, 0.01f)
        assertEquals(1, summary.groupStats.size)
        assertEquals(75f, summary.groupStats.first().attendancePercent, 0.01f)
        assertEquals(3, summary.totalPresentMarks)
        assertEquals(1, summary.totalAbsentMarks)
    }

    @Test
    fun `week calendar returns 6 college days starting monday`() {
        val days = WeekCalendarUtils.getWeekDays("2026-11-23")
        assertEquals(6, days.size)
        assertEquals("Дш", days.first().shortNameKy)
        assertEquals("Иш", days.last().shortNameKy)
        assertTrue(WeekCalendarUtils.formatWeekRangeKy("2026-11-23").contains("2026"))
    }
}
