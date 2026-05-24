package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.DaySituation
import com.quare.bibleplanner.feature.readingplan.domain.usecase.ResolveDaySituationMotivation
import kotlinx.datetime.LocalDate

internal class ResolveDaySituationMotivationUseCase : ResolveDaySituationMotivation {
    override fun invoke(
        days: List<DayModel>,
        today: LocalDate,
    ): DaySituation? {
        val todayDay = days.find { it.isToday }
        val overdueCount = days.count { day ->
            val planned = day.plannedReadDate
            planned != null && planned < today && !day.isRead && !day.isToday
        }

        if (todayDay != null) {
            if (todayDay.isRead) return DaySituation.Completed
            if (todayDay.readVerses in 1 until todayDay.totalVerses) return DaySituation.Started
        }

        if (overdueCount >= 2) return DaySituation.MultipleOverdue
        if (overdueCount == 1) return DaySituation.OneOverdue

        if (todayDay != null && todayDay.totalVerses > 0 && todayDay.readVerses == 0) {
            return DaySituation.NotStarted
        }
        return null
    }
}
