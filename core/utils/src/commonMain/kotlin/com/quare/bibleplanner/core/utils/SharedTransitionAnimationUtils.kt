package com.quare.bibleplanner.core.utils

object SharedTransitionAnimationUtils {
    fun buildWeekSeparatorId(weekNumber: Int) = buildWeekNumberId(weekNumber) + "_separator"

    fun buildDayNumberId(
        weekNumber: Int,
        dayNumebr: Int,
    ) = buildWeekNumberId(
        weekNumber,
    ) + "_day_$dayNumebr"

    fun buildWeekNumberId(weekNumber: Int) = "week_number_$weekNumber"
}
