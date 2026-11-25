package com.example.domain

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class WeekDayInfo(
    val dayOfWeek: Int, // 1 = Monday .. 6 = Saturday
    val shortNameKy: String,
    val fullNameKy: String,
    val dateLabel: String
)

object WeekCalendarUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val shortDays = listOf("Дш", "Шш", "Шр", "Бш", "Жм", "Иш")
    private val fullDays = listOf(
        "Дүйшөмбү",
        "Шейшемби",
        "Шаршемби",
        "Бейшемби",
        "Жума",
        "Ишемби"
    )
    private val monthsKy = listOf(
        "январь", "февраль", "март", "апрель", "май", "июнь",
        "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"
    )

    fun currentWeekStartIso(): String {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        val day = cal.get(Calendar.DAY_OF_WEEK)
        val diffToMonday = when (day) {
            Calendar.SUNDAY -> -6
            else -> Calendar.MONDAY - day
        }
        cal.add(Calendar.DAY_OF_MONTH, diffToMonday)
        zeroTime(cal)
        return isoFormat.format(cal.time)
    }

    fun todayCollegeDayIndex(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
    }

    fun shiftWeek(weekStartIso: String, deltaWeeks: Int): String {
        val cal = parseIsoCalendar(weekStartIso)
        cal.add(Calendar.WEEK_OF_YEAR, deltaWeeks)
        return isoFormat.format(cal.time)
    }

    fun getWeekDays(weekStartIso: String): List<WeekDayInfo> {
        val cal = parseIsoCalendar(weekStartIso)
        return (1..6).map { index ->
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val m = cal.get(Calendar.MONTH) + 1
            val info = WeekDayInfo(
                dayOfWeek = index,
                shortNameKy = shortDays[index - 1],
                fullNameKy = fullDays[index - 1],
                dateLabel = String.format(Locale.US, "%02d.%02d", d, m)
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
            info
        }
    }

    fun formatWeekRangeKy(weekStartIso: String): String {
        val startCal = parseIsoCalendar(weekStartIso)
        val endCal = parseIsoCalendar(weekStartIso).apply { add(Calendar.DAY_OF_MONTH, 5) }

        val startDay = startCal.get(Calendar.DAY_OF_MONTH)
        val startMonth = monthsKy[startCal.get(Calendar.MONTH)]
        val endDay = endCal.get(Calendar.DAY_OF_MONTH)
        val endMonth = monthsKy[endCal.get(Calendar.MONTH)]
        val year = endCal.get(Calendar.YEAR)

        return if (startCal.get(Calendar.MONTH) == endCal.get(Calendar.MONTH)) {
            "$startDay–$endDay $endMonth, $year"
        } else {
            "$startDay $startMonth – $endDay $endMonth, $year"
        }
    }

    fun formatWeekShortKy(weekStartIso: String): String {
        val startCal = parseIsoCalendar(weekStartIso)
        val endCal = parseIsoCalendar(weekStartIso).apply { add(Calendar.DAY_OF_MONTH, 5) }
        return String.format(
            Locale.US,
            "%02d.%02d–%02d.%02d",
            startCal.get(Calendar.DAY_OF_MONTH),
            startCal.get(Calendar.MONTH) + 1,
            endCal.get(Calendar.DAY_OF_MONTH),
            endCal.get(Calendar.MONTH) + 1
        )
    }

    private fun parseIsoCalendar(iso: String): Calendar {
        val cal = Calendar.getInstance()
        val parsed = runCatching { isoFormat.parse(iso) }.getOrNull()
        if (parsed != null) {
            cal.time = parsed
        }
        zeroTime(cal)
        return cal
    }

    private fun zeroTime(cal: Calendar) {
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
    }
}
