package com.quare.bibleplanner.feature.day.domain.usecase

import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.VerseModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.DayModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.WeekPlanModel
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.day.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.day.fake.FakeDayRepository
import com.quare.bibleplanner.feature.day.fake.FakePlanRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetDayDetailsUseCaseTest {
    private val startDate = LocalDate(2026, 1, 1)
    private val planDay = DayModel(
        number = 2,
        passages = listOf(
            PassageModel(
                bookId = BookId.GEN,
                chapters = listOf(
                    ChapterModel(
                        number = 1,
                        startVerse = null,
                        endVerse = null,
                        bookId = BookId.GEN,
                    ),
                ),
                isRead = false,
                chapterRanges = "1",
            ),
        ),
        isRead = false,
        totalVerses = 0,
        readVerses = 0,
        readTimestamp = 42L,
        plannedReadDate = null,
        notes = "plan notes",
        isToday = false,
    )
    private val readBook = BookDataModel(
        id = BookId.GEN,
        chapters = listOf(
            BookChapterModel(
                number = 1,
                verses = listOf(
                    VerseModel(
                        number = 1,
                        isRead = true,
                    ),
                    VerseModel(
                        number = 2,
                        isRead = true,
                    ),
                ),
                isRead = true,
                readUpdatedAt = 10L,
            ),
        ),
        isRead = false,
    )
    private lateinit var useCase: GetDayDetailsUseCase

    @Test
    fun `GIVEN no stored day WHEN observing THEN returns the plan day without timestamp or notes`() = runTest {
        // Given
        prepareScenario(storedDay = null)

        // When
        val day = useCase(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        ).first()

        // Then
        assertEquals(
            planDay.copy(
                passages = listOf(planDay.passages.single().copy(isRead = true)),
                isRead = true,
                totalVerses = 2,
                readVerses = 2,
                readTimestamp = null,
                plannedReadDate = LocalDate(2026, 1, 2),
                notes = null,
            ),
            day,
        )
    }

    @Test
    fun `GIVEN a stored day WHEN observing THEN keeps its timestamp and notes with the plan progress`() = runTest {
        // Given
        val storedDay = planDay.copy(
            readTimestamp = 99L,
            notes = "my notes",
            passages = emptyList(),
        )
        prepareScenario(storedDay = storedDay)

        // When
        val day = useCase(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        ).first()

        // Then
        assertEquals(99L, day?.readTimestamp)
        assertEquals("my notes", day?.notes)
        assertEquals(true, day?.isRead)
        assertEquals(LocalDate(2026, 1, 2), day?.plannedReadDate)
        assertEquals(planDay.passages.single().copy(isRead = true), day?.passages?.single())
    }

    @Test
    fun `GIVEN the chronological plan WHEN observing THEN reads the day from that plan`() = runTest {
        // Given
        prepareScenario(
            storedDay = null,
            planType = ReadingPlanType.CHRONOLOGICAL,
        )

        // When
        val day = useCase(
            weekNumber = 1,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.CHRONOLOGICAL,
        ).first()

        // Then
        assertEquals(2, day?.number)
    }

    @Test
    fun `GIVEN a week missing from the plan WHEN observing THEN returns null`() = runTest {
        // Given
        prepareScenario(storedDay = planDay)

        // When
        val day = useCase(
            weekNumber = 7,
            dayNumber = 2,
            readingPlanType = ReadingPlanType.BOOKS,
        ).first()

        // Then
        assertNull(day)
    }

    @Test
    fun `GIVEN a day missing from the week WHEN observing THEN returns null`() = runTest {
        // Given
        prepareScenario(storedDay = planDay)

        // When
        val day = useCase(
            weekNumber = 1,
            dayNumber = 6,
            readingPlanType = ReadingPlanType.BOOKS,
        ).first()

        // Then
        assertNull(day)
    }

    private fun prepareScenario(
        storedDay: DayModel?,
        planType: ReadingPlanType = ReadingPlanType.BOOKS,
    ) {
        val booksRepository = FakeBooksRepository(listOf(readBook))
        useCase = GetDayDetailsUseCase(
            getPlansByWeekUseCase = GetPlansByWeekUseCase(
                planRepository = FakePlanRepository(
                    plans = mapOf(
                        planType to listOf(
                            WeekPlanModel(
                                number = 1,
                                days = listOf(planDay),
                            ),
                        ),
                    ),
                    startDate = startDate,
                ),
                booksRepository = booksRepository,
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(startDate, LocalTime(9, 0)) },
            ),
            dayRepository = FakeDayRepository(
                day = storedDay,
                daysWithNotesCount = 0,
            ),
        )
    }
}
