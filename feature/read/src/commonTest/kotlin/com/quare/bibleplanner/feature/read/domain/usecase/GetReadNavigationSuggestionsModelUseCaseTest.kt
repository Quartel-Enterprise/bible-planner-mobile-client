package com.quare.bibleplanner.feature.read.domain.usecase

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionsModel
import com.quare.bibleplanner.feature.read.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.read.fake.FakePlanRepository
import com.quare.bibleplanner.feature.read.fake.book
import com.quare.bibleplanner.feature.read.fake.passage
import com.quare.bibleplanner.feature.read.fake.singleWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetReadNavigationSuggestionsModelUseCaseTest {
    private lateinit var useCase: GetReadNavigationSuggestionsModelUseCase

    @Test
    fun `GIVEN the chronological plan selected WHEN in the middle of it THEN suggests its neighbours in that plan`() =
        runTest {
            // Given
            prepareScenario(selectedPlan = ReadingPlanType.CHRONOLOGICAL)

            // When
            val suggestions = suggestionsFor(
                bookId = BookId.GEN,
                chapterNumber = 2,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(
                expected = ReadNavigationSuggestionsModel(
                    previous = ReadNavigationSuggestionModel(
                        bookId = BookId.GEN,
                        chapterNumber = 1,
                    ),
                    next = ReadNavigationSuggestionModel(
                        bookId = BookId.JOB,
                        chapterNumber = 1,
                    ),
                ),
                actual = suggestions,
            )
        }

    @Test
    fun `GIVEN the chronological plan WHEN forcing the canon order THEN suggests the books order neighbours`() =
        runTest {
            // Given
            prepareScenario(selectedPlan = ReadingPlanType.CHRONOLOGICAL)

            // When
            val suggestions = suggestionsFor(
                bookId = BookId.GEN,
                chapterNumber = 2,
                shouldForceCanonOrder = true,
            )

            // Then
            assertEquals(
                expected = ReadNavigationSuggestionModel(
                    bookId = BookId.EXO,
                    chapterNumber = 1,
                ),
                actual = suggestions.next,
            )
        }

    @Test
    fun `GIVEN a passage covering a whole book WHEN inside it THEN walks the chapters of that book`() = runTest {
        // Given
        prepareScenario(selectedPlan = ReadingPlanType.BOOKS)

        // When
        val suggestions = suggestionsFor(
            bookId = BookId.JOB,
            chapterNumber = 1,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(
            expected = ReadNavigationSuggestionsModel(
                previous = ReadNavigationSuggestionModel(
                    bookId = BookId.EXO,
                    chapterNumber = 1,
                ),
                next = ReadNavigationSuggestionModel(
                    bookId = BookId.JOB,
                    chapterNumber = 2,
                ),
            ),
            actual = suggestions,
        )
    }

    @Test
    fun `GIVEN the first chapter of the plan WHEN looking around THEN has nothing before it`() = runTest {
        // Given
        prepareScenario(selectedPlan = ReadingPlanType.CHRONOLOGICAL)

        // When
        val suggestions = suggestionsFor(
            bookId = BookId.GEN,
            chapterNumber = 1,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(
            expected = null,
            actual = suggestions.previous,
        )
    }

    @Test
    fun `GIVEN a chapter outside the plan WHEN looking around THEN suggests nothing`() = runTest {
        // Given
        prepareScenario(selectedPlan = ReadingPlanType.CHRONOLOGICAL)

        // When
        val suggestions = suggestionsFor(
            bookId = BookId.REV,
            chapterNumber = 22,
            shouldForceCanonOrder = false,
        )

        // Then
        assertEquals(
            expected = ReadNavigationSuggestionsModel(
                previous = null,
                next = null,
            ),
            actual = suggestions,
        )
    }

    private suspend fun suggestionsFor(
        bookId: BookId,
        chapterNumber: Int,
        shouldForceCanonOrder: Boolean,
    ): ReadNavigationSuggestionsModel = useCase(
        shouldForceCanonOrder = shouldForceCanonOrder,
        currentBookId = bookId,
        currentChapterNumber = chapterNumber,
    ).first()

    private fun prepareScenario(selectedPlan: ReadingPlanType) {
        val planRepository = FakePlanRepository(
            weeksByPlan = mapOf(
                ReadingPlanType.CHRONOLOGICAL to singleWeek(
                    listOf(passage(BookId.GEN, 1, 2)),
                    listOf(passage(BookId.JOB, 1), passage(BookId.EXO, 1)),
                ),
                ReadingPlanType.BOOKS to singleWeek(
                    listOf(passage(BookId.GEN, 1, 2)),
                    listOf(passage(BookId.EXO, 1), passage(BookId.JOB)),
                ),
            ),
            selectedPlan = selectedPlan,
        )
        val booksRepository = FakeBooksRepository(
            flowOf(
                listOf(
                    book(
                        bookId = BookId.JOB,
                        chapterCount = 2,
                    ),
                ),
            ),
        )
        useCase = GetReadNavigationSuggestionsModelUseCase(
            getPlansByWeek = GetPlansByWeekUseCase(
                planRepository = planRepository,
                booksRepository = booksRepository,
                getPlannedReadDateForDayUseCase = GetPlannedReadDateForDayUseCase(),
                currentTimestampProvider = { 0L },
                localDateTimeProvider = {
                    LocalDateTime(
                        year = 2026,
                        month = 1,
                        day = 1,
                        hour = 0,
                        minute = 0,
                    )
                },
            ),
            planRepository = planRepository,
            booksRepository = booksRepository,
        )
    }
}
