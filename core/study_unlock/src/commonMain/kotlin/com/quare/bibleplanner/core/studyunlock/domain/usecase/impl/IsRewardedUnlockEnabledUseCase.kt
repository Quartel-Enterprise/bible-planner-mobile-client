package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import com.quare.bibleplanner.core.studyunlock.domain.usecase.IsRewardedUnlockEnabled

internal class IsRewardedUnlockEnabledUseCase(
    private val rewardedAdService: RewardedAdService,
    private val getBooleanRemoteConfig: GetBooleanRemoteConfig,
) : IsRewardedUnlockEnabled {
    override suspend fun invoke(): Boolean = rewardedAdService.isSupported &&
        getBooleanRemoteConfig(
            key = REWARDED_ADS_ENABLED_KEY,
            default = false,
        )

    private companion object {
        const val REWARDED_ADS_ENABLED_KEY = "rewarded_ads_enabled"
    }
}
