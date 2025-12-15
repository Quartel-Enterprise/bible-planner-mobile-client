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
}
