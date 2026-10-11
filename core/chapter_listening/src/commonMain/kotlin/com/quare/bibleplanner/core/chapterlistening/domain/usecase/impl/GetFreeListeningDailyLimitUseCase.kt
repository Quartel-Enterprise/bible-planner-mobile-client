package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.chapterlistening.domain.usecase.GetFreeListeningDailyLimit
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetIntRemoteConfig

internal class GetFreeListeningDailyLimitUseCase(
    private val getIntRemoteConfig: GetIntRemoteConfig,
) : GetFreeListeningDailyLimit {
    override suspend fun invoke(): Int = getIntRemoteConfig(
        key = FREE_DAILY_LIMIT_KEY,
        default = DEFAULT_FREE_DAILY_LIMIT,
    )

    private companion object {
        const val FREE_DAILY_LIMIT_KEY = "listening_free_daily_limit"
        const val DEFAULT_FREE_DAILY_LIMIT = 1
    }
}
