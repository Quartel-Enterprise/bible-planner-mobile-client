package com.quare.bibleplanner.core.plan.domain

private const val DAYS_PER_WEEK = 7

fun getGlobalDayIndex(
    weekNumber: Int,
    dayNumber: Int,
): Int = (weekNumber - 1) * DAYS_PER_WEEK + dayNumber
