package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState

class ResolveStudyCtaStateUseCase {
    operator fun invoke(
        isPro: Boolean,
        quota: DayStudyQuotaModel,
    ): StudyCtaState = when {
        isPro -> StudyCtaState.Pro

        quota.remainingFree > 0 -> StudyCtaState.FreeWithQuota(
            remaining = quota.remainingFree,
            limit = quota.freeLimit,
        )

        else -> StudyCtaState.FreeExhausted(limit = quota.freeLimit)
    }
}
