package com.quare.bibleplanner.core.provider.ads.data.service

import com.quare.bibleplanner.core.provider.ads.domain.service.AdsConsentDataSource

internal class UnsupportedAdsConsentDataSource : AdsConsentDataSource {
    override fun gatherConsent(onComplete: (isInfoUpdated: Boolean) -> Unit) {
        onComplete(true)
    }

    override fun canRequestAds(): Boolean = false

    override fun isPrivacyOptionsRequired(): Boolean = false

    override fun showPrivacyOptions(onComplete: () -> Unit) {
        onComplete()
    }
}
