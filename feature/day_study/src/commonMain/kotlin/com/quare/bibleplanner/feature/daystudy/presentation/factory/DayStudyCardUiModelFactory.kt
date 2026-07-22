package com.quare.bibleplanner.feature.daystudy.presentation.factory

import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel

internal class DayStudyCardUiModelFactory {
    fun create(
        isPro: Boolean,
        quota: DayStudyQuotaModel,
    ): DayStudyCardUiModel = DayStudyCardUiModel(
        mode = resolveMode(
            isPro = isPro,
            quota = quota,
        ),
        quota = Loadable.Loaded(
            DayStudyCardQuotaUiModel(
                remainingFree = quota.remainingFree,
                freeLimit = quota.freeLimit,
            ),
        ),
        isPro = isPro,
    )

    fun createFromCache(isPro: Boolean): DayStudyCardUiModel = DayStudyCardUiModel(
        mode = DayStudyCardMode.VIEW,
        quota = Loadable.Loading,
        isPro = isPro,
    )

    private fun resolveMode(
        isPro: Boolean,
        quota: DayStudyQuotaModel,
    ): DayStudyCardMode = when {
        quota.isUnlockedForDay -> DayStudyCardMode.VIEW
        isPro -> DayStudyCardMode.GENERATE
        quota.remainingFree > 0 -> DayStudyCardMode.GENERATE
        else -> DayStudyCardMode.LOCKED
    }
}
