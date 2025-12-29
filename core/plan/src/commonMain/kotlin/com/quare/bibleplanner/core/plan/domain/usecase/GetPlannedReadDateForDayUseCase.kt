package com.quare.bibleplanner.core.plan.domain.usecase

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class GetPlannedReadDateForDayUseCase {
    operator fun invoke(
        weekNumber: Int,
        dayNumber: Int,
        startDate: LocalDate,
    ): LocalDate {
        val datePeriodToAdd = DatePeriod(
            days = (weekNumber - 1) * 7 + (dayNumber - 1)
        )
        return startDate.plus(datePeriodToAdd)
    }
}
