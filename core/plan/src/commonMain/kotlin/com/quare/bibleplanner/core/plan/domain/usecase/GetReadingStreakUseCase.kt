package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.core.plan.domain.shownReadingStreak
import kotlinx.coroutines.flow.first

// Why: only the selected plan is considered, matching the streak the plan screen shows.
class GetReadingStreakUseCase(
    private val getPlansByWeekUseCase: GetPlansByWeekUseCase,
    private val planRepository: PlanRepository,
) : GetReadingStreak {
    override suspend fun invoke(): Int {
        val readingPlanType = planRepository.getSelectedReadingPlanFlow().first()
        return getPlansByWeekUseCase(readingPlanType)
            .first()
            .flatMap { it.days }
            .shownReadingStreak
    }
}
