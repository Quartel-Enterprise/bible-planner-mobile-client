package com.quare.bibleplanner.feature.readingplan.domain

private const val DAYS_PER_WEEK = 7

internal fun getGlobalDayIndex(
    weekNumber: Int,
    dayNumber: Int,
): Int = (weekNumber - 1) * DAYS_PER_WEEK + dayNumber
