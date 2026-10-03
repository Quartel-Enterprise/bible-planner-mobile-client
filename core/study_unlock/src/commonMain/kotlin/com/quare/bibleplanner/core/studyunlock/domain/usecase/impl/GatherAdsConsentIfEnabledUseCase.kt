package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import com.quare.bibleplanner.core.studyunlock.domain.usecase.GatherAdsConsentIfEnabled
import com.quare.bibleplanner.core.studyunlock.domain.usecase.IsRewardedUnlockEnabled

internal class GatherAdsConsentIfEnabledUseCase(
    private val isRewardedUnlockEnabled: IsRewardedUnlockEnabled,
    private val adsConsentService: AdsConsentService,
) : GatherAdsConsentIfEnabled {
    override suspend fun invoke() {
        if (isRewardedUnlockEnabled()) adsConsentService.gatherConsent()
    }
}
