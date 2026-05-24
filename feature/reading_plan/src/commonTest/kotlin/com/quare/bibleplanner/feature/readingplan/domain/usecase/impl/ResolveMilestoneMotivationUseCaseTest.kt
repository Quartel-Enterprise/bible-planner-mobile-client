package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.feature.readingplan.domain.model.PlanMotivationMessage.Milestone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ResolveMilestoneMotivationUseCaseTest {
    private val useCase = ResolveMilestoneMotivationUseCase()
    private val nowMillis = 1_700_000_000_000L
    private val withinWindow = nowMillis - 60_000L
    private val olderWithinWindow = nowMillis - 600_000L
    private val outsideWindow = nowMillis - 25L * 60 * 60 * 1000L

    @Test
    fun `empty plan returns null`() {
        assertNull(useCase(emptyList(), nowMillis))
    }

    @Test
    fun `EnteredNewTestament triggers when crossing from OT to NT within 24h`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = olderWithinWindow,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.MAT, isRead = true)),
            ),
        )
        assertEquals(Milestone.EnteredNewTestament, useCase(days, nowMillis))
    }

    @Test
    fun `EnteredNewTestament does not trigger without prior OT activity`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.MAT, isRead = true)),
            ),
            day(
                number = 2,
                isRead = false,
                passages = listOf(passage(BookId.MRK, isRead = false)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.LUK, isRead = false)),
            ),
        )
        assertEquals(Milestone.FirstBookCompleted, useCase(days, nowMillis))
    }

    @Test
    fun `EnteredNewTestament does not trigger when earlier read already had NT`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = olderWithinWindow,
                passages = listOf(passage(BookId.MAT, isRead = true)),
            ),
            day(
                number = 2,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.MRK, isRead = true)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.GEN, isRead = false)),
            ),
            day(
                number = 4,
                isRead = false,
                passages = listOf(passage(BookId.EXO, isRead = false)),
            ),
        )
        assertEquals(Milestone.BookCompleted(BookId.MRK), useCase(days, nowMillis))
    }

    @Test
    fun `OnlyOneBookLeft triggers when exactly one unread book remains`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = outsideWindow,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = false,
                passages = listOf(passage(BookId.EXO, isRead = false)),
            ),
        )
        assertEquals(Milestone.OnlyOneBookLeft(BookId.EXO), useCase(days, nowMillis))
    }

    @Test
    fun `OnlyOneBookLeft does not trigger in single-book plan`() {
        val days = listOf(
            day(
                number = 1,
                isRead = false,
                passages = listOf(passage(BookId.GEN, isRead = false)),
            ),
        )
        assertNull(useCase(days, nowMillis))
    }

    @Test
    fun `OnlyOneBookLeft does not trigger when multiple books still have unread passages`() {
        val days = listOf(
            day(
                number = 1,
                isRead = false,
                passages = listOf(
                    passage(BookId.GEN, isRead = false),
                    passage(BookId.EXO, isRead = false),
                ),
            ),
        )
        assertNull(useCase(days, nowMillis))
    }

    @Test
    fun `FirstBookCompleted triggers when first book just closed within 24h`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = false,
                passages = listOf(passage(BookId.EXO, isRead = false)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.LEV, isRead = false)),
            ),
        )
        assertEquals(Milestone.FirstBookCompleted, useCase(days, nowMillis))
    }

    @Test
    fun `BookCompleted triggers when a non-first book closes within 24h`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = olderWithinWindow,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.EXO, isRead = true)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.LEV, isRead = false)),
            ),
            day(
                number = 4,
                isRead = false,
                passages = listOf(passage(BookId.NUM, isRead = false)),
            ),
        )
        assertEquals(Milestone.BookCompleted(BookId.EXO), useCase(days, nowMillis))
    }

    @Test
    fun `BookCompleted does not trigger outside 24h window`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = outsideWindow - 1000L,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = true,
                readTimestamp = outsideWindow,
                passages = listOf(passage(BookId.EXO, isRead = true)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.LEV, isRead = false)),
            ),
            day(
                number = 4,
                isRead = false,
                passages = listOf(passage(BookId.NUM, isRead = false)),
            ),
        )
        assertNull(useCase(days, nowMillis))
    }

    @Test
    fun `NT entry beats OnlyOneBookLeft when both apply`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = olderWithinWindow,
                passages = listOf(passage(BookId.GEN, isRead = true)),
            ),
            day(
                number = 2,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(passage(BookId.MAT, isRead = true)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.EXO, isRead = false)),
            ),
        )
        assertEquals(Milestone.EnteredNewTestament, useCase(days, nowMillis))
    }

    @Test
    fun `BookCompleted picks the canonical-last book when multiple close on the same day`() {
        val days = listOf(
            day(
                number = 1,
                isRead = true,
                readTimestamp = withinWindow,
                passages = listOf(
                    passage(BookId.GEN, isRead = true),
                    passage(BookId.EXO, isRead = true),
                ),
            ),
            day(
                number = 2,
                isRead = false,
                passages = listOf(passage(BookId.LEV, isRead = false)),
            ),
            day(
                number = 3,
                isRead = false,
                passages = listOf(passage(BookId.NUM, isRead = false)),
            ),
        )
        assertEquals(Milestone.BookCompleted(BookId.EXO), useCase(days, nowMillis))
    }
}
