package com.quare.bibleplanner.feature.readingplan.domain.usecase

import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage
import kotlinx.datetime.LocalDate

internal fun interface ResolveDaySituationMotivation {
    operator fun invoke(
        days: List<DayModel>,
        today: LocalDate,
    ): PlanMotivationMessage.DaySituation?
}
