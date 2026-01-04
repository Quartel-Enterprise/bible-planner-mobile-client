package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.plan.domain.usecase.GetDaysWithNotesCountUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetMaxFreeNotesAmountUseCase
import com.quare.bibleplanner.core.provider.billing.domain.usecase.IsFreeUserUseCase

class ShouldBlockAddNotesUseCase(
    private val getDaysWithNotesCount: GetDaysWithNotesCountUseCase,
    private val getMaxFreeNotesAmount: GetMaxFreeNotesAmountUseCase,
    private val isFreeUser: IsFreeUserUseCase,
) {
    suspend operator fun invoke(): Boolean = isFreeUser() && isMaxFreeNotesReached()

    private suspend fun isMaxFreeNotesReached(): Boolean = getDaysWithNotesCount() >= getMaxFreeNotesAmount()
}
