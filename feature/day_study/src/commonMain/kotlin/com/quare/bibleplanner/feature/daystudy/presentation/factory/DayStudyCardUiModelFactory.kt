package com.quare.bibleplanner.feature.daystudy.presentation.factory

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.core.studyunlock.domain.usecase.PrepareRewardedUnlockOffer
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardQuotaUiModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardUiModel

internal class DayStudyCardUiModelFactory(
    private val prepareRewardedUnlockOffer: PrepareRewardedUnlockOffer,
) {
    suspend fun create(
        isPro: Boolean,
        quota: DayStudyQuotaModel,
    ): DayStudyCardUiModel {
        val mode = resolveMode(
            isPro = isPro,
            quota = quota,
        )
        return DayStudyCardUiModel(
            mode = mode,
            quota = Loadable.Loaded(
                DayStudyCardQuotaUiModel(
                    remainingFree = quota.remainingFree,
                    freeLimit = quota.freeLimit,
                ),
            ),
            isPro = isPro,
            isRewardedUnlockOffered = mode == DayStudyCardMode.LOCKED &&
                prepareRewardedUnlockOffer(quota.rewardedRemainingToday),
            rewardedRemainingToday = quota.rewardedRemainingToday,
        )
    }

    suspend fun createLocked(
        card: DayStudyCardUiModel,
        rewardedRemainingToday: Int,
    ): DayStudyCardUiModel = card.copy(
        mode = DayStudyCardMode.LOCKED,
        quota = Loadable.Loaded(
            DayStudyCardQuotaUiModel(
                remainingFree = 0,
                freeLimit = card.quota.valueOrNull()?.freeLimit ?: 0,
            ),
        ),
        isRewardedUnlockOffered = prepareRewardedUnlockOffer(rewardedRemainingToday),
        rewardedRemainingToday = rewardedRemainingToday,
    )

    fun createFromCache(isPro: Boolean): DayStudyCardUiModel = DayStudyCardUiModel(
        mode = DayStudyCardMode.VIEW,
        quota = Loadable.Loading,
        isPro = isPro,
        isRewardedUnlockOffered = false,
        rewardedRemainingToday = 0,
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
