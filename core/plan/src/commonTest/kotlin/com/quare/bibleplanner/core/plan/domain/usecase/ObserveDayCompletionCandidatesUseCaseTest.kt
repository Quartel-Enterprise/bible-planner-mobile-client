package com.quare.bibleplanner.core.plan.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.book.ChapterLocationModel
import com.quare.bibleplanner.core.model.plan.PlanDayLocationModel
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ObserveDayCompletionCandidatesUseCaseTest {
    private val genesis1 = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 1,
    )
    private val genesis2 = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 2,
    )
    private val genesis3 = ChapterLocationModel(
        bookId = BookId.GEN,
        chapterNumber = 3,
    )
    private val obadiah1 = ChapterLocationModel(
        bookId = BookId.OBA,
        chapterNumber = 1,
    )
    private val jude1 = ChapterLocationModel(
        bookId = BookId.JUD,
        chapterNumber = 1,
    )
    private val revelation1 = ChapterLocationModel(
        bookId = BookId.REV,
        chapterNumber = 1,
    )
    private val week1Day1 = PlanDayLocationModel(
        weekNumber = 1,
        dayNumber = 1,
        readingPlanType = ReadingPlanType.CHRONOLOGICAL,
    )
    private val week1Day2 = PlanDayLocationModel(
        weekNumber = 1,
        dayNumber = 2,
        readingPlanType = ReadingPlanType.CHRONOLOGICAL,
    )
    private val chronologicalWeeks = listOf(
        week(
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
                                chapterPlan(
                                    bookId = BookId.GEN,
                                    number = 2,
                                ),
                            ),
                        ),
                    ),
                ),
                day(
                    number = 2,
                    passages = listOf(passage(bookId = BookId.OBA)),
                ),
                day(
                    number = 3,
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
                        passage(
                            bookId = BookId.EXO,
                            chapters = listOf(
                                chapterPlan(
                                    bookId = BookId.EXO,
                                    number = 1,
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        ),
        week(
            number = 2,
            days = listOf(
                day(
                    number = 1,
                    passages = listOf(passage(bookId = BookId.JUD)),
                ),
            ),
        ),
    )
    private val books = listOf(
        book(
            bookId = BookId.GEN,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 2,
                    isRead = true,
                ),
                bookChapter(
                    number = 2,
                    verseCount = 2,
                ),
                bookChapter(
                    number = 3,
                    verseCount = 2,
                ),
            ),
        ),
        book(
            bookId = BookId.OBA,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 2,
                ),
            ),
        ),
        book(
            bookId = BookId.JUD,
            chapters = listOf(
                bookChapter(
                    number = 1,
                    verseCount = 2,
                ),
                bookChapter(
                    number = 2,
                    verseCount = 2,
                ),
            ),
        ),
    )

    private lateinit var planRepository: FakePlanRepository
    private lateinit var booksRepository: FakeBooksRepository
    private lateinit var useCase: ObserveDayCompletionCandidatesUseCase

    @Test
    fun `GIVEN the last unread chapter of a day WHEN observing THEN reports the day it would complete`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(genesis2)).first()

        // Then
        assertEquals(mapOf(genesis2 to week1Day1), candidates)
    }

    @Test
    fun `GIVEN a chapter of a whole-book day WHEN it is the only unread one THEN reports that day`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(obadiah1)).first()

        // Then
        assertEquals(mapOf(obadiah1 to week1Day2), candidates)
    }

    @Test
    fun `GIVEN a whole-book day with other unread chapters WHEN observing THEN reports nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(jude1)).first()

        // Then
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `GIVEN a day with another unread chapter WHEN observing THEN reports nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(genesis1)).first()

        // Then
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `GIVEN a day whose other book is unknown WHEN observing THEN reports nothing`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(genesis3)).first()

        // Then
        assertTrue(candidates.isEmpty())
    }

    @Test
    fun `GIVEN only unscheduled chapters WHEN observing THEN reports nothing without observing any book`() = runTest {
        // Given
        prepareScenario()

        // When
        val candidates = useCase(listOf(revelation1)).first()

        // Then
        assertTrue(candidates.isEmpty())
        assertTrue(booksRepository.observedBookIds.isEmpty())
    }

    @Test
    fun `GIVEN several chapters WHEN observing THEN observes each scheduled book once`() = runTest {
        // Given
        prepareScenario()

        // When
        useCase(
            listOf(
                genesis1,
                genesis2,
                obadiah1,
            ),
        ).first()

        // Then
        assertEquals(listOf(BookId.GEN, BookId.OBA), booksRepository.observedBookIds)
    }

    @Test
    fun `GIVEN the books order plan is selected WHEN observing THEN scores that plan instead`() = runTest {
        // Given
        prepareScenario(selectedReadingPlan = ReadingPlanType.BOOKS)

        // When
        val candidates = useCase(listOf(genesis2)).first()

        // Then
        assertTrue(candidates.isEmpty())
    }

    private fun prepareScenario(selectedReadingPlan: ReadingPlanType = ReadingPlanType.CHRONOLOGICAL) {
        planRepository = FakePlanRepository(
            plans = mapOf(ReadingPlanType.CHRONOLOGICAL to chronologicalWeeks),
            startDate = null,
            selectedReadingPlan = selectedReadingPlan,
        )
        booksRepository = FakeBooksRepository(books)
        useCase = ObserveDayCompletionCandidatesUseCase(
            planRepository = planRepository,
            booksRepository = booksRepository,
        )
    }
}
