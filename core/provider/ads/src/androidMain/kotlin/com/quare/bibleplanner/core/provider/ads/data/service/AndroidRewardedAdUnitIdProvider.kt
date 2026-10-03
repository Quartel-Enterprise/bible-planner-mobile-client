package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.service.RewardedAdUnitIdProvider
import com.quare.bibleplanner.core.provider.platform.domain.usecase.IsDebugBuild

internal class AndroidRewardedAdUnitIdProvider(
    private val isDebugBuild: IsDebugBuild,
) : RewardedAdUnitIdProvider {
    override fun invoke(): String = if (isDebugBuild()) {
        TEST_AD_UNIT_ID
    } else {
        RELEASE_AD_UNIT_ID
    }

    private companion object {
        const val RELEASE_AD_UNIT_ID = "ca-app-pub-9748272108340789/2517958834"
        const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }
}
