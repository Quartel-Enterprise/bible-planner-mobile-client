package com.quare.bibleplanner.core.plan.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class GetPlannedReadDateForDayUseCase {
    operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        startDate: LocalDate,
    ): LocalDate = startDate.plus(
        period = DatePeriod(
            days = getDaysNumber(weekNumber, dayNumber),
        ),
    )

    private fun getDaysNumber(
        weekNumber: Int,
        dayNumber: Int,
    ): Int = (weekNumber - INDEX_OFFSET) * DAYS_PER_WEEK + (dayNumber - INDEX_OFFSET)

    private companion object {
        const val INDEX_OFFSET = 1
        const val DAYS_PER_WEEK = 7
    }
}
