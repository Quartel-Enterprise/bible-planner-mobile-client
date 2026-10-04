package com.quare.bibleplanner.feature.readingplan.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
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
    fun `GIVEN an empty plan WHEN resolving THEN returns null`() {
        // Given
        val days = emptyList<DayModel>()

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertNull(milestone)
    }

    @Test
    fun `GIVEN a crossing from OT to NT within 24h WHEN resolving THEN returns EnteredNewTestament`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.EnteredNewTestament, milestone)
    }

    @Test
    fun `GIVEN NT reads without prior OT activity WHEN resolving THEN returns FirstBookCompleted`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.FirstBookCompleted, milestone)
    }

    @Test
    fun `GIVEN an earlier read already in the NT WHEN resolving THEN returns BookCompleted`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.BookCompleted(BookId.MRK), milestone)
    }

    @Test
    fun `GIVEN exactly one unread book remaining WHEN resolving THEN returns OnlyOneBookLeft`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.OnlyOneBookLeft(BookId.EXO), milestone)
    }

    @Test
    fun `GIVEN a single-book plan WHEN resolving THEN returns null instead of OnlyOneBookLeft`() {
        // Given
        val days = listOf(
            day(
                number = 1,
                isRead = false,
                passages = listOf(passage(BookId.GEN, isRead = false)),
            ),
        )

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertNull(milestone)
    }

    @Test
    fun `GIVEN several books with unread passages WHEN resolving THEN returns null instead of OnlyOneBookLeft`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertNull(milestone)
    }

    @Test
    fun `GIVEN the first book closed within 24h WHEN resolving THEN returns FirstBookCompleted`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.FirstBookCompleted, milestone)
    }

    @Test
    fun `GIVEN a non-first book closed within 24h WHEN resolving THEN returns BookCompleted`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.BookCompleted(BookId.EXO), milestone)
    }

    @Test
    fun `GIVEN a book closed outside the 24h window WHEN resolving THEN returns null`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertNull(milestone)
    }

    @Test
    fun `GIVEN both an NT entry and one book left WHEN resolving THEN returns EnteredNewTestament`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.EnteredNewTestament, milestone)
    }

    @Test
    fun `GIVEN several books closed on one day WHEN resolving THEN returns BookCompleted for the canonical-last one`() {
        // Given
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

        // When
        val milestone = useCase(days, nowMillis)

        // Then
        assertEquals(Milestone.BookCompleted(BookId.EXO), milestone)
    }
}
