package com.quare.bibleplanner.core.provider.ads.testing

import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeAdsConsentService(
    var canRequestAds: Boolean,
    isPrivacyOptionsRequired: Boolean,
) : AdsConsentService {
    val isPrivacyOptionsRequiredFlow = MutableStateFlow(isPrivacyOptionsRequired)
    var gatherCount = 0
    var privacyOptionsShownCount = 0

    // Why: set it to keep the privacy options form open until the test completes it, as the real form stays up.
    var privacyOptionsDismissal: CompletableDeferred<Unit>? = null

    override val isPrivacyOptionsRequired: StateFlow<Boolean> = isPrivacyOptionsRequiredFlow

    override suspend fun gatherConsent() {
        gatherCount++
    }

    override fun canRequestAds(): Boolean = canRequestAds

    override suspend fun showPrivacyOptions() {
        privacyOptionsShownCount++
        privacyOptionsDismissal?.await()
    }
}
