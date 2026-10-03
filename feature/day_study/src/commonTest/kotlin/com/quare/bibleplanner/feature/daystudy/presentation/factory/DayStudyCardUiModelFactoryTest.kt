package com.quare.bibleplanner.feature.daystudy.presentation.factory

import com.quare.bibleplanner.core.daystudy.domain.model.DayStudyQuotaModel
import com.quare.bibleplanner.core.model.loadable.Loadable
import com.quare.bibleplanner.core.model.loadable.valueOrNull
import com.quare.bibleplanner.feature.daystudy.presentation.model.DayStudyCardMode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DayStudyCardUiModelFactoryTest {
    private val factory = DayStudyCardUiModelFactory(prepareRewardedUnlockOffer = { true })

    @Test
    fun `GIVEN a free user with remaining quota and no unlocked study WHEN creating THEN mode is generate`() = runTest {
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
        assertEquals(3, card.quota.valueOrNull()?.remainingFree)
    }

    @Test
    fun `GIVEN a locally cached study WHEN creating from cache THEN mode is view with loading quota`() = runTest {
        // When
        val card = factory.createFromCache(isPro = false)

        // Then
        assertEquals(DayStudyCardMode.VIEW, card.mode)
        assertEquals(Loadable.Loading, card.quota)
        assertEquals(false, card.isPro)
    }

    @Test
    fun `GIVEN a pro user with a locally cached study WHEN creating from cache THEN pro flag is kept`() = runTest {
        // When
        val card = factory.createFromCache(isPro = true)

        // Then
        assertEquals(DayStudyCardMode.VIEW, card.mode)
        assertEquals(true, card.isPro)
    }

    @Test
    fun `GIVEN a free user with exhausted quota and no unlocked study WHEN creating THEN mode is locked`() = runTest {
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
        assertEquals(true, card.isRewardedUnlockOffered)
    }

    @Test
    fun `GIVEN a free user with quota left WHEN creating THEN never offers a rewarded unlock`() = runTest {
        // Given
        val quota = quota(
            remainingFree = 1,
            isUnlockedForDay = false,
        )

        // When
        val card = factory.create(
            isPro = false,
            quota = quota,
        )

        // Then
        assertEquals(false, card.isRewardedUnlockOffered)
    }

    @Test
    fun `GIVEN a card WHEN locking it THEN spends the free quota and resolves the rewarded offer`() = runTest {
        // Given
        val card = factory.create(
            isPro = false,
            quota = quota(
                remainingFree = 2,
                isUnlockedForDay = false,
            ),
        )

        // When
        val locked = factory.createLocked(
            card = card,
            rewardedRemainingToday = 1,
        )

        // Then
        assertEquals(DayStudyCardMode.LOCKED, locked.mode)
        assertEquals(0, locked.quota.valueOrNull()?.remainingFree)
        assertEquals(3, locked.quota.valueOrNull()?.freeLimit)
        assertEquals(true, locked.isRewardedUnlockOffered)
        assertEquals(1, locked.rewardedRemainingToday)
    }

    @Test
    fun `GIVEN a free user with exhausted quota but an unlocked study WHEN creating THEN mode is view`() = runTest {
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
    fun `GIVEN a pro user without an unlocked study WHEN creating THEN mode is generate with pro flag`() = runTest {
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
    fun `GIVEN a pro user with an unlocked study WHEN creating THEN mode is view`() = runTest {
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
        hasLocalStudy = isUnlockedForDay,
        rewardedRemainingToday = 0,
    )
}
