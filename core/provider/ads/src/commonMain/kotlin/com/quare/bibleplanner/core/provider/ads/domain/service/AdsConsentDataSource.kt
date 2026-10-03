package com.quare.bibleplanner.core.provider.ads.domain.service

interface AdsConsentDataSource {
    fun gatherConsent(onComplete: (isInfoUpdated: Boolean) -> Unit)

    fun canRequestAds(): Boolean

    fun isPrivacyOptionsRequired(): Boolean

    fun showPrivacyOptions(onComplete: () -> Unit)
}
