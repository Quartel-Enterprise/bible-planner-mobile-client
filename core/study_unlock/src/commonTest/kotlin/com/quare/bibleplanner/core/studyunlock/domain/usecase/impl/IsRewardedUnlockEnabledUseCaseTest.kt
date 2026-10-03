package com.quare.bibleplanner.core.studyunlock.domain.usecase.impl

import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdAvailability
import com.quare.bibleplanner.core.provider.ads.domain.model.RewardedAdResult
import com.quare.bibleplanner.core.provider.ads.testing.FakeRewardedAdService
import com.quare.bibleplanner.core.remoteconfig.domain.usecase.base.GetBooleanRemoteConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class IsRewardedUnlockEnabledUseCaseTest {
    private lateinit var useCase: IsRewardedUnlockEnabledUseCase
    private lateinit var remoteConfig: RecordingBooleanRemoteConfig

    @Test
    fun `GIVEN a supported platform and the flag on WHEN checking THEN it is enabled`() = runTest {
        // Given
        prepareScenario(
            isPlatformSupported = true,
            isFlagOn = true,
        )

        // When
        val isEnabled = useCase()

        // Then
        assertTrue(isEnabled)
        assertEquals(listOf("rewarded_ads_enabled" to false), remoteConfig.requests)
    }

    @Test
    fun `GIVEN the flag off WHEN checking THEN it is disabled`() = runTest {
        // Given
        prepareScenario(
            isPlatformSupported = true,
            isFlagOn = false,
        )

        // When
        val isEnabled = useCase()

        // Then
        assertFalse(isEnabled)
    }

    @Test
    fun `GIVEN an unsupported platform WHEN checking THEN it is disabled without reading the flag`() = runTest {
        // Given
        prepareScenario(
            isPlatformSupported = false,
            isFlagOn = true,
        )

        // When
        val isEnabled = useCase()

        // Then
        assertFalse(isEnabled)
        assertTrue(remoteConfig.requests.isEmpty())
    }

    private fun prepareScenario(
        isPlatformSupported: Boolean,
        isFlagOn: Boolean,
    ) {
        remoteConfig = RecordingBooleanRemoteConfig(value = isFlagOn)
        useCase = IsRewardedUnlockEnabledUseCase(
            rewardedAdService = FakeRewardedAdService(
                isSupported = isPlatformSupported,
                availability = RewardedAdAvailability.IDLE,
                showResult = RewardedAdResult.Dismissed,
            ),
            getBooleanRemoteConfig = remoteConfig,
        )
    }
}

private class RecordingBooleanRemoteConfig(
    private val value: Boolean,
) : GetBooleanRemoteConfig {
    val requests = mutableListOf<Pair<String, Boolean>>()

    override suspend fun invoke(
        key: String,
        default: Boolean,
    ): Boolean {
        requests += key to default
        return value
    }
}
