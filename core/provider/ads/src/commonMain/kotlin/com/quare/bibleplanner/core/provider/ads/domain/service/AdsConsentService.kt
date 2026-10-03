package com.quare.bibleplanner.core.provider.ads.domain.service

import kotlinx.coroutines.flow.StateFlow

interface AdsConsentService {
    val isPrivacyOptionsRequired: StateFlow<Boolean>

    suspend fun gatherConsent()

    fun canRequestAds(): Boolean

    suspend fun showPrivacyOptions()
}
