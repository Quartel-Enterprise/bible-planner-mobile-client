package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.date.CurrentTimestampProvider
import com.quare.bibleplanner.core.date.LocalDateTimeProvider
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.fake.FakeBooksRepository
import com.quare.bibleplanner.core.plan.fake.FakePlanRepository
import com.quare.bibleplanner.core.plan.fake.book
import com.quare.bibleplanner.core.plan.fake.bookChapter
import com.quare.bibleplanner.core.plan.fake.chapterPlan
import com.quare.bibleplanner.core.plan.fake.day
import com.quare.bibleplanner.core.plan.fake.passage
import com.quare.bibleplanner.core.plan.fake.week
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetPlansByWeekUseCaseTest {
    private val today = LocalDate(
        year = 2026,
        month = 1,
        day = 10,
    )
    private val books = listOf(
        book(
            bookId = BookId.GEN,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 5,
                    isRead = true,
                ),
                bookChapter(
                    number = 2,
                    verseCount = 4,
                    readVerses = setOf(1, 2),
                ),
                bookChapter(
                    number = 3,
                    verseCount = 3,
                ),
            ),
        ),
        book(
            bookId = BookId.OBA,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 2,
                    isRead = true,
                ),
            ),
            isRead = true,
        ),
    )
    private val chronologicalWeek = week(
        number = 1,
        days = listOf(
            day(
                number = 1,
                passages = listOf(
                    passage(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            chapterPlan(
                                bookId = BookId.GEN,
                                number = 1,
                            ),
                        ),
                    ),
                ),
            ),
            day(
                number = 2,
                passages = listOf(
                    passage(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            chapterPlan(
                                bookId = BookId.GEN,
                                number = 2,
                                startVerse = 1,
                                endVerse = 2,
                            ),
                        ),
                    ),
                ),
            ),
            day(
                number = 3,
                passages = listOf(
                    passage(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            chapterPlan(
                                bookId = BookId.GEN,
                                number = 2,
                                startVerse = 2,
                            ),
                        ),
                    ),
                ),
            ),
            day(
                number = 4,
                passages = listOf(passage(bookId = BookId.OBA)),
            ),
            day(
                number = 5,
                passages = listOf(passage(bookId = BookId.EXO)),
            ),
            day(
                number = 6,
                passages = listOf(
                    passage(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            chapterPlan(
                                bookId = BookId.GEN,
                                number = 4,
                            ),
                        ),
                    ),
                ),
            ),
            day(
                number = 7,
                passages = listOf(
                    passage(
                        bookId = BookId.GEN,
                        chapters = listOf(
                            chapterPlan(
                                bookId = BookId.GEN,
                                number = 3,
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
    private val booksOrderWeek = week(
        number = 1,
        days = listOf(
            day(
                number = 1,
                passages = listOf(
                    passage(bookId = BookId.OBA),
                    passage(bookId = BookId.GEN),
                ),
            ),
        ),
    )

    private lateinit var useCase: GetPlansByWeekUseCase

    @Test
    fun `GIVEN reading progress WHEN scoring the plans THEN flags only the days whose passages are fully read`() =
        runTest {
            // Given
            prepareScenario(startDate = null)

            // When
            val days = useCase()
                .first()
                .chronologicalOrder
                .single()
                .days

            // Then
            assertEquals(
                listOf(true, true, false, true, false, false, false),
                days.map(DayModel::isRead),
            )
            assertEquals(
                listOf(true, true, false, true, false, false, false),
                days.map { day -> day.passages.single().isRead },
            )
        }

    @Test
    fun `GIVEN reading progress WHEN scoring the plans THEN counts the planned and read verses of each day`() =
        runTest {
            // Given
            prepareScenario(startDate = null)

            // When
            val days = useCase()
                .first()
                .chronologicalOrder
                .single()
                .days

            // Then
            assertEquals(listOf(5, 2, 3, 2, 0, 0, 3), days.map(DayModel::totalVerses))
            assertEquals(listOf(5, 2, 1, 2, 0, 0, 0), days.map(DayModel::readVerses))
        }

    @Test
    fun `GIVEN whole-book passages WHEN scoring the plans THEN counts every verse of those books`() = runTest {
        // Given
        prepareScenario(startDate = null)

        // When
        val day = useCase()
            .first()
            .booksOrder
            .single()
            .days
            .single()

        // Then
        assertEquals(14, day.totalVerses)
        assertEquals(9, day.readVerses)
        assertEquals(listOf(true, false), day.passages.map { it.isRead })
    }

    @Test
    fun `GIVEN no start date WHEN scoring the plans THEN leaves the days unscheduled`() = runTest {
        // Given
        prepareScenario(startDate = null)

        // When
        val days = useCase()
            .first()
            .chronologicalOrder
            .single()
            .days

        // Then
        assertEquals(List(7) { null }, days.map(DayModel::plannedReadDate))
        assertEquals(List(7) { false }, days.map(DayModel::isToday))
    }

    @Test
    fun `GIVEN a start date WHEN scoring one plan THEN schedules each day and flags the one due today`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(
                year = 2026,
                month = 1,
                day = 5,
            ),
        )

        // When
        val days = useCase(ReadingPlanType.CHRONOLOGICAL).first().single().days

        // Then
        assertEquals(
            (5..11).map { dayOfMonth ->
                LocalDate(
                    year = 2026,
                    month = 1,
                    day = dayOfMonth,
                )
            },
            days.map(DayModel::plannedReadDate),
        )
        assertEquals(6, days.single(DayModel::isToday).number)
    }

    @Test
    fun `GIVEN only the books order requested WHEN scoring one plan THEN returns only that plan`() = runTest {
        // Given
        prepareScenario(startDate = null)

        // When
        val weeks = useCase(ReadingPlanType.BOOKS).first()

        // Then
        assertEquals(1, weeks.single().days.size)
        assertNull(
            weeks
                .single()
                .days
                .single()
                .plannedReadDate,
        )
    }

    private fun prepareScenario(startDate: LocalDate?) {
        useCase = GetPlansByWeekUseCase(
            planRepository = FakePlanRepository(
                plans = mapOf(
                    ReadingPlanType.CHRONOLOGICAL to listOf(chronologicalWeek),
                    ReadingPlanType.BOOKS to listOf(booksOrderWeek),
                ),
                startDate = startDate,
                selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
            ),
            booksRepository = FakeBooksRepository(books),
            getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
            currentTimestampProvider = CurrentTimestampProvider { 0L },
            localDateTimeProvider = LocalDateTimeProvider {
                LocalDateTime(
                    date = today,
                    time = LocalTime(
                        hour = 8,
                        minute = 0,
                    ),
                )
            },
        )
    }
}
