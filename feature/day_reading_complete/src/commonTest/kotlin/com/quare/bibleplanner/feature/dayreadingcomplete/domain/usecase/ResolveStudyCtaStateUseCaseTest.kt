package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveStudyCtaStateUseCaseTest {
    private val resolveStudyCtaState = ResolveStudyCtaStateUseCase(prepareRewardedUnlockOffer = { false })

    @Test
    fun `a pro user always resolves to pro, regardless of quota`() = runTest {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        assertEquals(
            expected = StudyCtaState.Pro,
            actual = resolveStudyCtaState(isPro = true, quota = quota),
        )
    }

    @Test
    fun `a free user with remaining quota resolves to free with quota`() = runTest {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 2,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        assertEquals(
            expected = StudyCtaState.FreeWithQuota(remaining = 2, limit = 3),
            actual = resolveStudyCtaState(isPro = false, quota = quota),
        )
    }

    @Test
    fun `a free user with no remaining quota resolves to free exhausted`() = runTest {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 0,
        )

        assertEquals(
            expected = StudyCtaState.FreeExhausted(
                limit = 3,
                isRewardedUnlockOffered = false,
                rewardedRemainingToday = 0,
            ),
            actual = resolveStudyCtaState(isPro = false, quota = quota),
        )
    }

    @Test
    fun `a free user with no remaining quota and a video on offer resolves to an offered unlock`() = runTest {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
            rewardedRemainingToday = 2,
        )
        val resolveWithOffer = ResolveStudyCtaStateUseCase(prepareRewardedUnlockOffer = { remaining -> remaining > 0 })

        assertEquals(
            expected = StudyCtaState.FreeExhausted(
                limit = 3,
                isRewardedUnlockOffered = true,
                rewardedRemainingToday = 2,
            ),
            actual = resolveWithOffer(isPro = false, quota = quota),
        )
    }
}
