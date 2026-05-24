package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.OverallProgress
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ResolveOverallProgressMotivationUseCaseTest {
    private val useCase = ResolveOverallProgressMotivationUseCase()

    @Test
    fun `progress 0 returns Zero`() {
        assertEquals(OverallProgress.Zero, useCase(0f))
    }

    @Test
    fun `progress 1 returns EarlyStart`() {
        assertEquals(OverallProgress.EarlyStart, useCase(1f))
    }

    @Test
    fun `progress 5 returns EarlyStart`() {
        assertEquals(OverallProgress.EarlyStart, useCase(5f))
    }

    @Test
    fun `progress 6 returns BuildingSolid`() {
        assertEquals(OverallProgress.BuildingSolid, useCase(6f))
    }

    @Test
    fun `progress 15 returns BuildingSolid`() {
        assertEquals(OverallProgress.BuildingSolid, useCase(15f))
    }

    @Test
    fun `progress 16 returns ApproachingThird`() {
        assertEquals(OverallProgress.ApproachingThird, useCase(16f))
    }

    @Test
    fun `progress 30 returns ApproachingThird`() {
        assertEquals(OverallProgress.ApproachingThird, useCase(30f))
    }

    @Test
    fun `progress 31 returns PastThirty`() {
        assertEquals(OverallProgress.PastThirty, useCase(31f))
    }

    @Test
    fun `progress 49 returns PastThirty`() {
        assertEquals(OverallProgress.PastThirty, useCase(49f))
    }

    @Test
    fun `progress 50 returns Halfway`() {
        assertEquals(OverallProgress.Halfway, useCase(50f))
    }

    @Test
    fun `progress 51 returns MoreThanHalf`() {
        assertEquals(OverallProgress.MoreThanHalf, useCase(51f))
    }

    @Test
    fun `progress 74 returns MoreThanHalf`() {
        assertEquals(OverallProgress.MoreThanHalf, useCase(74f))
    }

    @Test
    fun `progress 75 returns ThreeQuarters`() {
        assertEquals(OverallProgress.ThreeQuarters, useCase(75f))
    }

    @Test
    fun `progress 76 returns FinalStretch`() {
        assertEquals(OverallProgress.FinalStretch, useCase(76f))
    }

    @Test
    fun `progress 90 returns FinalStretch`() {
        assertEquals(OverallProgress.FinalStretch, useCase(90f))
    }

    @Test
    fun `progress 91 returns AlmostThere`() {
        assertEquals(OverallProgress.AlmostThere, useCase(91f))
    }

    @Test
    fun `progress 99 returns AlmostThere`() {
        assertEquals(OverallProgress.AlmostThere, useCase(99f))
    }

    @Test
    fun `progress 100 returns Completed`() {
        assertEquals(OverallProgress.Completed, useCase(100f))
    }

    @Test
    fun `negative progress clamps to Zero`() {
        assertEquals(OverallProgress.Zero, useCase(-5f))
    }

    @Test
    fun `over 100 progress clamps to Completed`() {
        assertEquals(OverallProgress.Completed, useCase(150f))
    }
}
