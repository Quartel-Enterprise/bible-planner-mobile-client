package com.quare.bibleplanner.feature.read.domain.usecase.impl

import com.quare.bibleplanner.core.model.book.BookId
import com.quare.bibleplanner.core.model.plan.ReadingPlanType
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlannedReadDateForDayUseCase
import com.quare.bibleplanner.core.plan.domain.usecase.GetPlansByWeekUseCase
import com.quare.bibleplanner.feature.read.domain.model.ReadNavigationSuggestionModel
import com.quare.bibleplanner.feature.read.domain.usecase.GetReadNavigationSuggestionsModelUseCase
import com.quare.bibleplanner.feature.read.fake.FakeBooksRepository
import com.quare.bibleplanner.feature.read.fake.FakePlanRepository
import com.quare.bibleplanner.feature.read.fake.passage
import com.quare.bibleplanner.feature.read.fake.singleWeek
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GetAdjacentChapterUseCasesTest {
    private lateinit var getNextChapter: GetNextChapterUseCase
    private lateinit var getPreviousChapter: GetPreviousChapterUseCase

    @BeforeTest
    fun setUp() {
        val planRepository = FakePlanRepository(
            weeksByPlan = mapOf(
                ReadingPlanType.CHRONOLOGICAL to singleWeek(listOf(passage(BookId.GEN, 1, 2, 3))),
                ReadingPlanType.BOOKS to singleWeek(listOf(passage(BookId.EXO, 1, 2, 3))),
            ),
            selectedPlan = ReadingPlanType.CHRONOLOGICAL,
        )
        val booksRepository = FakeBooksRepository(flowOf(emptyList()))
        val getReadNavigationSuggestionsModel = GetReadNavigationSuggestionsModelUseCase(
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
        getNextChapter = GetNextChapterUseCase(getReadNavigationSuggestionsModel)
        getPreviousChapter = GetPreviousChapterUseCase(getReadNavigationSuggestionsModel)
    }

    @Test
    fun `GIVEN a chapter in the selected plan WHEN asking for the next one THEN returns the following chapter`() =
        runTest {
            // When
            val next = getNextChapter(
                bookId = BookId.GEN,
                chapterNumber = 2,
                shouldForceCanonOrder = false,
            )

            // Then
            assertEquals(
                expected = ReadNavigationSuggestionModel(
                    bookId = BookId.GEN,
                    chapterNumber = 3,
                ),
                actual = next,
            )
        }

    @Test
    fun `GIVEN a chapter in the canon order WHEN asking for the previous one THEN returns the preceding chapter`() =
        runTest {
            // When
            val previous = getPreviousChapter(
                bookId = BookId.EXO,
                chapterNumber = 2,
                shouldForceCanonOrder = true,
            )

            // Then
            assertEquals(
                expected = ReadNavigationSuggestionModel(
                    bookId = BookId.EXO,
                    chapterNumber = 1,
                ),
                actual = previous,
            )
        }
}
