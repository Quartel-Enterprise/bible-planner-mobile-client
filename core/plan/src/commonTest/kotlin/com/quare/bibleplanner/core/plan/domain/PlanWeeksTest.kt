package com.quare.bibleplanner.core.plan.domain

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PlanWeeksTest {
    @Test
    fun `GIVEN a read day scheduling the chapter WHEN searching THEN returns that day`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = true, bookId = BookId.GEN, chapterNumbers = listOf(1, 2)),
                    day(number = 2, isRead = true, bookId = BookId.GEN, chapterNumbers = listOf(3, 4)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.GEN,
            chapterNumber = 3,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertEquals(
            expected = PlanDayLocationModel(
                weekNumber = 1,
                dayNumber = 2,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the day scheduling the chapter is unread WHEN searching THEN returns null`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = false, bookId = BookId.GEN, chapterNumbers = listOf(3)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.GEN,
            chapterNumber = 3,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `GIVEN a read day covering the whole book WHEN searching THEN returns that day`() {
        // Given
        val weeks = listOf(
            week(
                number = 4,
                days = listOf(
                    day(number = 2, isRead = true, bookId = BookId.OBA, chapterNumbers = emptyList()),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.OBA,
            chapterNumber = 1,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertEquals(
            expected = PlanDayLocationModel(
                weekNumber = 4,
                dayNumber = 2,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN a read day of another book WHEN searching THEN returns null`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = true, bookId = BookId.EXO, chapterNumbers = listOf(3)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.GEN,
            chapterNumber = 3,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertNull(result)
    }

    @Test
    fun `GIVEN the chapter is scheduled on two read days of one week WHEN searching THEN returns the earlier day`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = true, bookId = BookId.EXO, chapterNumbers = listOf(1)),
                    day(number = 2, isRead = true, bookId = BookId.GEN, chapterNumbers = listOf(3)),
                    day(number = 3, isRead = true, bookId = BookId.GEN, chapterNumbers = listOf(3, 4)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.GEN,
            chapterNumber = 3,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertEquals(
            expected = PlanDayLocationModel(
                weekNumber = 1,
                dayNumber = 2,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the chapter is read on days of two weeks WHEN searching THEN returns the day of the earlier week`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 7, isRead = true, bookId = BookId.PSA, chapterNumbers = listOf(23)),
                ),
            ),
            week(
                number = 2,
                days = listOf(
                    day(number = 1, isRead = true, bookId = BookId.PSA, chapterNumbers = listOf(23)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.PSA,
            chapterNumber = 23,
            readingPlanType = ReadingPlanType.BOOKS,
        )

        // Then
        assertEquals(
            expected = PlanDayLocationModel(
                weekNumber = 1,
                dayNumber = 7,
                readingPlanType = ReadingPlanType.BOOKS,
            ),
            actual = result,
        )
    }

    @Test
    fun `GIVEN the earlier day scheduling the chapter is unread WHEN searching THEN returns the later read day`() {
        // Given
        val weeks = listOf(
            week(
                number = 1,
                days = listOf(
                    day(number = 1, isRead = false, bookId = BookId.GEN, chapterNumbers = listOf(3)),
                ),
            ),
            week(
                number = 2,
                days = listOf(
                    day(number = 1, isRead = true, bookId = BookId.GEN, chapterNumbers = listOf(3)),
                ),
            ),
        )

        // When
        val result = weeks.findCompletedDayFor(
            bookId = BookId.GEN,
            chapterNumber = 3,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        )

        // Then
        assertEquals(
            expected = PlanDayLocationModel(
                weekNumber = 2,
                dayNumber = 1,
                readingPlanType = ReadingPlanType.CHRONOLOGICAL,
            ),
            actual = result,
        )
    }

    private fun week(
        number: Int,
        days: List<DayModel>,
    ): WeekPlanModel = WeekPlanModel(
        number = number,
        days = days,
    )

    private fun day(
        number: Int,
        isRead: Boolean,
        bookId: BookId,
        chapterNumbers: List<Int>,
    ): DayModel = DayModel(
        number = number,
        passages = listOf(
            PassageModel(
                bookId = bookId,
                chapters = chapterNumbers.map { chapterNumber ->
                    ChapterModel(
                        number = chapterNumber,
                        startVerse = null,
                        endVerse = null,
                        bookId = bookId,
                    )
                },
                isRead = isRead,
                chapterRanges = null,
            ),
        ),
        isRead = isRead,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = null,
        plannedReadDate = null,
        notes = null,
        isToday = false,
    )
}
