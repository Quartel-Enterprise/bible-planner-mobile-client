package com.quare.bibleplanner.feature.dayreadingcomplete.domain.usecase

import com.quare.bibleplanner.feature.dayreadingcomplete.domain.model.StudyCtaState
import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveStudyCtaStateUseCaseTest {
    private val resolveStudyCtaState = ResolveStudyCtaStateUseCase()

    @Test
    fun `a pro user always resolves to pro, regardless of quota`() {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
        )

        assertEquals(
            expected = StudyCtaState.Pro,
            actual = resolveStudyCtaState(isPro = true, quota = quota),
        )
    }

    @Test
    fun `a free user with remaining quota resolves to free with quota`() {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 2,
            isUnlockedForDay = false,
            hasLocalStudy = false,
        )

        assertEquals(
            expected = StudyCtaState.FreeWithQuota(remaining = 2, limit = 3),
            actual = resolveStudyCtaState(isPro = false, quota = quota),
        )
    }

    @Test
    fun `a free user with no remaining quota resolves to free exhausted`() {
        val quota = DayStudyQuotaModel(
            freeLimit = 3,
            remainingFree = 0,
            isUnlockedForDay = false,
            hasLocalStudy = false,
        )

        assertEquals(
            expected = StudyCtaState.FreeExhausted(limit = 3),
            actual = resolveStudyCtaState(isPro = false, quota = quota),
        )
    }
}
