package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveStudyCtaStateUseCaseTest {
    private val resolveStudyCtaState = ResolveStudyCtaStateUseCase(prepareRewardedUnlockOffer = { false })

    @Test
    fun `GIVEN a pro user with no quota left WHEN resolving THEN always resolves to pro`() = runTest {
        // Given
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        // When
        val ctaState = resolveStudyCtaState(
            isPro = true,
            quota = quota,
        )

        // Then
        assertEquals(
            expected = StudyCtaState.Pro,
            actual = ctaState,
        )
    }

    @Test
    fun `GIVEN a free user with remaining quota WHEN resolving THEN resolves to free with quota`() = runTest {
        // Given
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 2,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        // When
        val ctaState = resolveStudyCtaState(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(
            expected = StudyCtaState.FreeWithQuota(remaining = 2, limit = 3),
            actual = ctaState,
        )
    }

    @Test
    fun `GIVEN a free user with no remaining quota WHEN resolving THEN resolves to free exhausted`() = runTest {
        // Given
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        // When
        val ctaState = resolveStudyCtaState(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(
            expected = StudyCtaState.FreeExhausted(
                limit = 3,
                isRewardedUnlockOffered = false,
                rewardedRemainingToday = 0,
            ),
            actual = ctaState,
        )
    }

    @Test
    fun `GIVEN a free user out of quota with a video on offer WHEN resolving THEN resolves to an offered unlock`() =
        runTest {
            // Given
            val quota = DayStudyQuotaModel(
                freeLimit = 3,
                remainingFree = 0,
                isUnlockedForDay = false,
                hasLocalStudy = false,
                rewardedRemainingToday = 2,
            )
            val resolveWithOffer = ResolveStudyCtaStateUseCase(prepareRewardedUnlockOffer = { remaining ->
                remaining > 0
            })

            // When
            val ctaState = resolveWithOffer(
                isPro = false,
                quota = quota,
            )

            // Then
            assertEquals(
                expected = StudyCtaState.FreeExhausted(
                    limit = 3,
                    isRewardedUnlockOffered = true,
                    rewardedRemainingToday = 2,
                ),
                actual = ctaState,
            )
        }
}
