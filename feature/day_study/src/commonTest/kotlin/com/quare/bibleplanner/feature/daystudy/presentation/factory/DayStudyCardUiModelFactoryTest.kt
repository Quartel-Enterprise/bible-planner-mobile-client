package com.quare.bibleplanner.feature.daystudy.presentation.factory

import com.quare.bibleplanner.feature.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyCardUiModelFactoryTest {
    private val factory = DayStudyCardUiModelFactory()

    @Test
    fun `GIVEN a free user with remaining quota and no unlocked study WHEN creating THEN mode is generate`() {
        // Given
        val quota = quota(
            remainingFree = 3,
            isUnlockedForDay = false,
        )

        // When
        val card = factory.create(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(DayStudyCardMode.GENERATE, card.mode)
        assertEquals(3, card.remainingFree)
    }

    @Test
    fun `GIVEN a free user with exhausted quota and no unlocked study WHEN creating THEN mode is locked`() {
        // Given
        val quota = quota(
            remainingFree = 0,
            isUnlockedForDay = false,
        )

        // When
        val card = factory.create(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(DayStudyCardMode.LOCKED, card.mode)
    }

    @Test
    fun `GIVEN a free user with exhausted quota but an unlocked study WHEN creating THEN mode is view`() {
        // Given
        val quota = quota(
            remainingFree = 0,
            isUnlockedForDay = true,
        )

        // When
        val card = factory.create(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(DayStudyCardMode.VIEW, card.mode)
    }

    @Test
    fun `GIVEN a pro user without an unlocked study WHEN creating THEN mode is generate with pro flag`() {
        // Given
        val quota = quota(
            remainingFree = 0,
            isUnlockedForDay = false,
        )

        // When
        val card = factory.create(
            isPro = true,
            quota = quota,
        )

        // Then
        assertEquals(DayStudyCardMode.GENERATE, card.mode)
        assertEquals(true, card.isPro)
    }

    @Test
    fun `GIVEN a pro user with an unlocked study WHEN creating THEN mode is view`() {
        // Given
        val quota = quota(
            remainingFree = 3,
            isUnlockedForDay = true,
        )

        // When
        val card = factory.create(
            isPro = true,
            quota = quota,
        )

        // Then
        assertEquals(DayStudyCardMode.VIEW, card.mode)
    }

    private fun quota(
        remainingFree: Int,
        isUnlockedForDay: Boolean,
    ): DayStudyQuotaModel = DayStudyQuotaModel(
        freeLimit = 3,
        remainingFree = remainingFree,
        isUnlockedForDay = isUnlockedForDay,
    )
}
