package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.testing.FakeRewardedAdService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PrepareRewardedUnlockOfferUseCaseTest {
    private lateinit var useCase: PrepareRewardedUnlockOfferUseCase
    private lateinit var rewardedAdService: FakeRewardedAdService

    @Test
    fun `GIVEN videos left and the feature enabled WHEN preparing THEN offers the video and preloads it`() = runTest {
        // Given
        prepareScenario(isEnabled = true)

        // When
        val isOffered = useCase(rewardedRemainingToday = 1)

        // Then
        assertTrue(isOffered)
        assertEquals(1, rewardedAdService.preloadCount)
    }

    @Test
    fun `GIVEN no video left today WHEN preparing THEN neither offers nor preloads`() = runTest {
        // Given
        prepareScenario(isEnabled = true)

        // When
        val isOffered = useCase(rewardedRemainingToday = 0)

        // Then
        assertFalse(isOffered)
        assertEquals(0, rewardedAdService.preloadCount)
    }

    @Test
    fun `GIVEN the feature disabled WHEN preparing THEN neither offers nor preloads`() = runTest {
        // Given
        prepareScenario(isEnabled = false)

        // When
        val isOffered = useCase(rewardedRemainingToday = 2)

        // Then
        assertFalse(isOffered)
        assertEquals(0, rewardedAdService.preloadCount)
    }

    private fun prepareScenario(isEnabled: Boolean) {
        rewardedAdService = FakeRewardedAdService(
            isSupported = true,
            availability = RewardedAdAvailability.IDLE,
            showResult = RewardedAdResult.Dismissed,
        )
        useCase = PrepareRewardedUnlockOfferUseCase(
            isRewardedUnlockEnabled = { isEnabled },
            rewardedAdService = rewardedAdService,
        )
    }
}
