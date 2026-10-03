package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import com.quare.bibleplanner.core.studyunlock.domain.usecase.IsRewardedUnlockEnabled
import com.quare.bibleplanner.core.studyunlock.domain.usecase.PrepareRewardedUnlockOffer

internal class PrepareRewardedUnlockOfferUseCase(
    private val isRewardedUnlockEnabled: IsRewardedUnlockEnabled,
    private val rewardedAdService: RewardedAdService,
) : PrepareRewardedUnlockOffer {
    override suspend fun invoke(rewardedRemainingToday: Int): Boolean {
        if (rewardedRemainingToday <= 0 || !isRewardedUnlockEnabled()) return false
        rewardedAdService.preload()
        return true
    }
}
