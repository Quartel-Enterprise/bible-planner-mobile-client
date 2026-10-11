package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetRewardedListeningDailyLimit
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class GetRewardedListeningDailyLimitUseCase(
    private val getIntRemoteConfig: GetIntRemoteConfig,
) : GetRewardedListeningDailyLimit {
    override suspend fun invoke(): Int = getIntRemoteConfig(
        key = REWARDED_DAILY_LIMIT_KEY,
        default = DEFAULT_REWARDED_DAILY_LIMIT,
    )

    private companion object {
        const val REWARDED_DAILY_LIMIT_KEY = "listening_rewarded_daily_limit"
        const val DEFAULT_REWARDED_DAILY_LIMIT = 1
    }
}
