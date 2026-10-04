package com.quare.bibleplanner.core.chapterlistening.domain.usecase.impl

import com.quare.bibleplanner.core.books.testing.FakeBooksRepository
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningDayModel
import com.quare.bibleplanner.core.chapterlistening.domain.model.ListeningSegmentModel
import com.quare.bibleplanner.core.chapterlistening.domain.usecase.ListeningDateProvider
import com.quare.bibleplanner.core.model.book.BookChapterModel
import com.quare.bibleplanner.core.model.book.BookDataModel
import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.ChapterModel
import com.quare.bibleplanner.core.model.plan.PassageModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.model.plan.ScheduledDayModel
import com.quare.bibleplanner.core.plan.testing.FakePlanRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GetTodayListeningDayUseCaseTest {
    private val today = LocalDate(2026, 10, 10)
    private val genesisOne = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 1)
    private val genesisTwo = ChapterLocationModel(bookId = BookId.GEN, chapterNumber = 2)
    private val obadiahOne = ChapterLocationModel(bookId = BookId.OBA, chapterNumber = 1)
    private val scheduledDays = mutableListOf<Triple<Int, Int, ReadingPlanType>>()
    private lateinit var useCase: GetTodayListeningDayUseCase

    @Test
    fun `GIVEN the plan day of today has the chapter WHEN getting it THEN lists every chapter of the day`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(2026, 10, 1),
            passages = listOf(
                passage(BookId.GEN, chapter(1), chapter(2, startVerse = 1, endVerse = 9)),
                passage(BookId.OBA),
            ),
        )

        // When
        val day = useCase(genesisOne)

        // Then
        assertEquals(
            ListeningDayModel(
                location = PlanDayLocationModel(
                    weekNumber = 2,
                    dayNumber = 3,
                    readingPlanType = ReadingPlanType.CHRONOLOGICAL,
                ),
                segments = listOf(
                    ListeningSegmentModel(chapter = genesisOne, startVerse = null, endVerse = null),
                    ListeningSegmentModel(chapter = genesisTwo, startVerse = 1, endVerse = 9),
                    ListeningSegmentModel(chapter = obadiahOne, startVerse = null, endVerse = null),
                ),
            ),
            day,
        )
        assertEquals(Triple(2, 3, ReadingPlanType.CHRONOLOGICAL), scheduledDays.single())
    }

    @Test
    fun `GIVEN a chapter outside the reading of today WHEN getting it THEN has no day`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(2026, 10, 1),
            passages = listOf(passage(BookId.GEN, chapter(1), chapter(2))),
        )

        // When
        val day = useCase(obadiahOne)

        // Then
        assertNull(day)
    }

    @Test
    fun `GIVEN a day of a single chapter WHEN getting it THEN has no day to play in sequence`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(2026, 10, 1),
            passages = listOf(passage(BookId.GEN, chapter(1))),
        )

        // When
        val day = useCase(genesisOne)

        // Then
        assertNull(day)
    }

    @Test
    fun `GIVEN a plan that starts in the future WHEN getting the day THEN has none`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(2026, 10, 20),
            passages = listOf(passage(BookId.GEN, chapter(1), chapter(2))),
        )

        // When
        val day = useCase(genesisOne)

        // Then
        assertNull(day)
    }

    @Test
    fun `GIVEN a plan without a start date WHEN getting the day THEN has none`() = runTest {
        // Given
        prepareScenario(
            startDate = null,
            passages = listOf(passage(BookId.GEN, chapter(1), chapter(2))),
        )

        // When
        val day = useCase(genesisOne)

        // Then
        assertNull(day)
    }

    @Test
    fun `GIVEN no scheduled day for today WHEN getting it THEN has none`() = runTest {
        // Given
        prepareScenario(
            startDate = LocalDate(2026, 10, 1),
            passages = null,
        )

        // When
        val day = useCase(genesisOne)

        // Then
        assertNull(day)
    }

    private fun passage(
        bookId: BookId,
        vararg chapters: ChapterModel,
    ): PassageModel = PassageModel(
        bookId = bookId,
        chapters = chapters.map { it.copy(bookId = bookId) },
        isRead = false,
        chapterRanges = null,
    )

    private fun chapter(
        number: Int,
        startVerse: Int? = null,
        endVerse: Int? = null,
    ): ChapterModel = ChapterModel(
        number = number,
        startVerse = startVerse,
        endVerse = endVerse,
        bookId = BookId.GEN,
    )

    private fun prepareScenario(
        startDate: LocalDate?,
        passages: List<PassageModel>?,
    ) {
        useCase = GetTodayListeningDayUseCase(
            planRepository = FakePlanRepository(
                plans = emptyMap(),
                startDate = startDate,
                selectedReadingPlan = ReadingPlanType.CHRONOLOGICAL,
            ),
            booksRepository = FakeBooksRepository(
                listOf(
                    BookDataModel(
                        id = BookId.OBA,
                        chapters = listOf(
                            BookChapterModel(
                                number = 1,
                                verses = emptyList(),
                                isRead = false,
                                readUpdatedAt = null,
                            ),
                        ),
                        isRead = false,
                        isFavorite = false,
                    ),
                ),
            ),
            getScheduledDay = { weekNumber, dayNumber, readingPlanType ->
                scheduledDays += Triple(weekNumber, dayNumber, readingPlanType)
                passages?.let {
                    ScheduledDayModel(
                        number = dayNumber,
                        passages = it,
                        plannedReadDate = today,
                    )
                }
            },
            dateProvider = ListeningDateProvider(
                currentTimestampProvider = { 0L },
                localDateTimeProvider = { LocalDateTime(today, LocalTime(8, 0)) },
            ),
        )
    }
}
