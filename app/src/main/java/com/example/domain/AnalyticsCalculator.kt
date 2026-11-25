package com.example.domain

import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.CollegeGroup
import com.example.data.model.Student

data class GroupWeeklyStats(
    val group: CollegeGroup,
    val totalStudents: Int,
    val markedDaysCount: Int,
    val presentMarks: Int,
    val absentMarks: Int,
    val excusedMarks: Int,
    val expectedMarksOnMarkedDays: Int,
    val attendancePercent: Float,
    val dailyPercentages: Map<Int, Float>
)

data class DailyAttendancePoint(
    val dayOfWeek: Int,
    val shortNameKy: String,
    val presentCount: Int,
    val expectedCount: Int,
    val percentage: Float
)

data class WeeklyTrendPoint(
    val weekStartIso: String,
    val shortLabel: String,
    val percentage: Float,
    val presentMarks: Int,
    val expectedMarks: Int
)

data class CollegeWeeklySummary(
    val weekStartIso: String,
    val totalGroups: Int,
    val totalStudents: Int,
    val overallPercent: Float,
    val totalPresentMarks: Int,
    val totalAbsentMarks: Int,
    val totalExcusedMarks: Int,
    val groupStats: List<GroupWeeklyStats>,
    val dailyBreakdown: List<DailyAttendancePoint>,
    val multiWeekTrend: List<WeeklyTrendPoint>
)

object AnalyticsCalculator {

    fun calculateWeeklySummary(
        weekStartIso: String,
        groups: List<CollegeGroup>,
        students: List<Student>,
        allAttendance: List<AttendanceRecord>
    ): CollegeWeeklySummary {
        val studentsByGroup = students.groupBy { it.groupId }
        val weekRecords = allAttendance.filter { it.weekStartIso == weekStartIso }
        val recordsByGroup = weekRecords.groupBy { it.groupId }

        val groupStatsList = groups.map { group ->
            val groupStudents = studentsByGroup[group.id].orEmpty()
            val effectiveStudentCount = maxOf(group.studentCount, groupStudents.size)
            val groupRecords = recordsByGroup[group.id].orEmpty()
            val byDay = groupRecords.groupBy { it.dayOfWeek }
            val markedDays = byDay.keys.size

            val presentCount = groupRecords.count { it.status == AttendanceStatus.PRESENT }
            val absentCount = groupRecords.count { it.status == AttendanceStatus.ABSENT }
            val excusedCount = groupRecords.count { it.status == AttendanceStatus.EXCUSED }

            val expectedMarks = if (markedDays > 0 && effectiveStudentCount > 0) {
                byDay.values.sumOf { dayRecords -> maxOf(dayRecords.size, effectiveStudentCount) }
            } else {
                0
            }

            val percent = if (expectedMarks > 0) {
                (presentCount.toFloat() / expectedMarks.toFloat()) * 100f
            } else {
                0f
            }

            val dailyMap = (1..6).associateWith { day ->
                val dayRecs = byDay[day].orEmpty()
                if (dayRecs.isEmpty() || effectiveStudentCount == 0) {
                    0f
                } else {
                    val dayPresent = dayRecs.count { it.status == AttendanceStatus.PRESENT }
                    val dayExpected = maxOf(dayRecs.size, effectiveStudentCount)
                    (dayPresent.toFloat() / dayExpected.toFloat()) * 100f
                }
            }

            GroupWeeklyStats(
                group = group,
                totalStudents = effectiveStudentCount,
                markedDaysCount = markedDays,
                presentMarks = presentCount,
                absentMarks = absentCount,
                excusedMarks = excusedCount,
                expectedMarksOnMarkedDays = expectedMarks,
                attendancePercent = percent,
                dailyPercentages = dailyMap
            )
        }.sortedByDescending { it.attendancePercent }

        val totalStudents = groupStatsList.sumOf { it.totalStudents }
        val totalPresent = groupStatsList.sumOf { it.presentMarks }
        val totalAbsent = groupStatsList.sumOf { it.absentMarks }
        val totalExcused = groupStatsList.sumOf { it.excusedMarks }
        val totalExpected = groupStatsList.sumOf { it.expectedMarksOnMarkedDays }
        val overallPercent = if (totalExpected > 0) {
            (totalPresent.toFloat() / totalExpected.toFloat()) * 100f
        } else {
            0f
        }

        val weekDays = WeekCalendarUtils.getWeekDays(weekStartIso)
        val dailyBreakdown = weekDays.map { dayInfo ->
            var dayPresent = 0
            var dayExpected = 0
            groups.forEach { group ->
                val groupStudents = studentsByGroup[group.id].orEmpty()
                val effectiveCount = maxOf(group.studentCount, groupStudents.size)
                val dayRecs = recordsByGroup[group.id].orEmpty().filter { it.dayOfWeek == dayInfo.dayOfWeek }
                if (dayRecs.isNotEmpty() && effectiveCount > 0) {
                    dayPresent += dayRecs.count { it.status == AttendanceStatus.PRESENT }
                    dayExpected += maxOf(dayRecs.size, effectiveCount)
                }
            }
            val pct = if (dayExpected > 0) (dayPresent.toFloat() / dayExpected.toFloat()) * 100f else 0f
            DailyAttendancePoint(
                dayOfWeek = dayInfo.dayOfWeek,
                shortNameKy = dayInfo.shortNameKy,
                presentCount = dayPresent,
                expectedCount = dayExpected,
                percentage = pct
            )
        }

        val multiWeekTrend = (-4..0).map { offset ->
            val wIso = WeekCalendarUtils.shiftWeek(weekStartIso, offset)
            val wRecs = allAttendance.filter { it.weekStartIso == wIso }.groupBy { it.groupId }
            var wPresent = 0
            var wExpected = 0
            groups.forEach { group ->
                val groupStudents = studentsByGroup[group.id].orEmpty()
                val effectiveCount = maxOf(group.studentCount, groupStudents.size)
                val gByDay = wRecs[group.id].orEmpty().groupBy { it.dayOfWeek }
                gByDay.values.forEach { dayRecs ->
                    if (effectiveCount > 0) {
                        wPresent += dayRecs.count { it.status == AttendanceStatus.PRESENT }
                        wExpected += maxOf(dayRecs.size, effectiveCount)
                    }
                }
            }
            val pct = if (wExpected > 0) (wPresent.toFloat() / wExpected.toFloat()) * 100f else 0f
            WeeklyTrendPoint(
                weekStartIso = wIso,
                shortLabel = WeekCalendarUtils.formatWeekShortKy(wIso),
                percentage = pct,
                presentMarks = wPresent,
                expectedMarks = wExpected
            )
        }

        return CollegeWeeklySummary(
            weekStartIso = weekStartIso,
            totalGroups = groups.size,
            totalStudents = totalStudents,
            overallPercent = overallPercent,
            totalPresentMarks = totalPresent,
            totalAbsentMarks = totalAbsent,
            totalExcusedMarks = totalExcused,
            groupStats = groupStatsList,
            dailyBreakdown = dailyBreakdown,
            multiWeekTrend = multiWeekTrend
        )
    }
}
