package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.plan.domain.repository.DayRepository

class GetDaysWithNotesCountUseCase(
    private val dayRepository: DayRepository,
) {
    suspend operator fun invoke(): Int = dayRepository.getDaysWithNotesCount()
}

