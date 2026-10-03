package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.testing.FakeAdsConsentService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GatherAdsConsentIfEnabledUseCaseTest {
    private lateinit var useCase: GatherAdsConsentIfEnabledUseCase
    private lateinit var consentService: FakeAdsConsentService

    @Test
    fun `GIVEN rewarded unlocks enabled WHEN starting THEN gathers consent`() = runTest {
        // Given
        prepareScenario(isEnabled = true)

        // When
        useCase()

        // Then
        assertEquals(1, consentService.gatherCount)
    }

    @Test
    fun `GIVEN rewarded unlocks disabled WHEN starting THEN never asks for consent`() = runTest {
        // Given
        prepareScenario(isEnabled = false)

        // When
        useCase()

        // Then
        assertEquals(0, consentService.gatherCount)
    }

    private fun prepareScenario(isEnabled: Boolean) {
        consentService = FakeAdsConsentService(
            isPrivacyOptionsRequired = false,
            canRequestAds = true,
        )
        useCase = GatherAdsConsentIfEnabledUseCase(
            isRewardedUnlockEnabled = { isEnabled },
            adsConsentService = consentService,
        )
    }
}
