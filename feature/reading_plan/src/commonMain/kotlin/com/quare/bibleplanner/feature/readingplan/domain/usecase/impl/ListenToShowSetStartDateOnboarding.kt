package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.plan.domain.repository.PlanRepository
import com.quare.bibleplanner.feature.onboardingstartdate.domain.repository.OnboardingStartDateRepository
import kotlinx.coroutines.flow.first

class ListenToShowSetStartDateOnboarding(
    private val planRepository: PlanRepository,
    private val onboardingRepository: OnboardingStartDateRepository
) {

    suspend operator fun invoke(callback: suspend () -> Unit) {
        planRepository.getStartPlanTimestamp().collect { startDate ->
            if (!onboardingRepository.getDontShowAgainFlow().first() && startDate == null) {
                callback()
            }
        }
    }
}
