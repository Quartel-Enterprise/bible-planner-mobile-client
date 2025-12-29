package com.quare.bibleplanner.core.utils

object SharedTransitionAnimationUtils {
    fun buildWeekSeparatorId(weekNumber: Int): String = buildWeekNumberId(weekNumber) + "_separator"

    fun buildDayNumberId(
        weekNumber: Int,
        dayNumebr: Int,
    ): String = buildWeekNumberId(
        weekNumber,
    ) + "_day_$dayNumebr"

    fun buildWeekNumberId(weekNumber: Int): String = "week_number_$weekNumber"

    fun buildPlannedDay(
        weekNumber: Int,
        dayNumber: Int,
    ): String = "planned_day_${weekNumber}_$dayNumber"

    fun buildPlannedMonth(
        weekNumber: Int,
        dayNumber: Int,
    ): String = "planned_month_${weekNumber}_$dayNumber"

    fun buildPlannedYear(
        weekNumber: Int,
        dayNumber: Int,
    ): String = "planned_year_${weekNumber}_$dayNumber"
}
